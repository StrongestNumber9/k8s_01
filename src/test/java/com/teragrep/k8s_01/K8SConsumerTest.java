/*
   Kubernetes log forwarder k8s_01
   Copyright (C) 2023  Suomen Kanuuna Oy

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.teragrep.k8s_01;
import com.teragrep.k8s_01.config.AppConfig;
import com.teragrep.k8s_01.fakes.FakeKubernetesCachingAPIClientImpl;
import com.teragrep.net_01.channel.socket.PlainFactory;
import com.teragrep.net_01.eventloop.EventLoop;
import com.teragrep.net_01.eventloop.EventLoopFactory;
import com.teragrep.net_01.server.ServerFactory;
import com.teragrep.rlo_06.RFC5424Frame;
import com.teragrep.rlo_12.DirectoryEventWatcher;
import com.teragrep.rlo_13.StatefulFileReader;
import com.teragrep.rlp_03.frame.FrameDelegationClockFactory;
import com.teragrep.rlp_03.frame.delegate.DefaultFrameDelegate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.teragrep.k8s_01.KubernetesLogReader.gson;

public class K8SConsumerTest {
    private EventLoop eventLoop;
    private Thread eventLoopThread;
    private ExecutorService executorService;
    private final List<String> messageList = new LinkedList<>();

    @BeforeEach
    public void init() {
        EventLoopFactory eventLoopFactory = new EventLoopFactory();
        Assertions.assertAll(() -> eventLoop = eventLoopFactory.create());
        eventLoopThread = new Thread(eventLoop);
        eventLoopThread.start();
        executorService = Executors.newSingleThreadExecutor();
        ServerFactory serverFactory = new ServerFactory(
                eventLoop,
                executorService,
                new PlainFactory(),
                new FrameDelegationClockFactory(() -> new DefaultFrameDelegate((frame) -> {
                    RFC5424Frame rfc5424Frame = new RFC5424Frame(false);
                    rfc5424Frame.load(new ByteArrayInputStream(frame.relpFrame().payload().toBytes()));
                    rfc5424Frame.next();
                    messageList.add(rfc5424Frame.msg.toString());
                }))
        );
        Assertions.assertAll(() -> serverFactory.create(1601));
    }

    @AfterEach
    public void cleanup() {
        eventLoop.stop();
        executorService.shutdown();
        Assertions.assertAll(eventLoopThread::join);
        messageList.clear();
    }

    @Test
    public void testConsumer() {
        InputStreamReader isr = Assertions.assertDoesNotThrow(() -> new InputStreamReader(Files.newInputStream(Paths.get("src/test/resources/config.json")), StandardCharsets.UTF_8));
        AppConfig appConfig = gson.fromJson(
                isr,
                AppConfig.class
        );
        KubernetesCachingAPIClient kubernetesCachingAPIClient = new FakeKubernetesCachingAPIClientImpl();
        PrometheusMetrics prometheusMetrics = new PrometheusMetrics(appConfig.getMetrics().getPort());
        BlockingQueue<RelpOutput> relpOutputPool = new LinkedBlockingDeque<>(2);
        Assertions.assertDoesNotThrow(() -> relpOutputPool.put(new RelpOutput(appConfig.getRelp(), 0, prometheusMetrics.getRegistry())));
        K8SConsumerSupplier consumerSupplier = new K8SConsumerSupplier(appConfig, kubernetesCachingAPIClient, relpOutputPool, "");
        StatefulFileReader statefulFileReader = new StatefulFileReader(
                Paths.get("target"),
                consumerSupplier
        );
        DirectoryEventWatcher dew = Assertions.assertDoesNotThrow(() -> new DirectoryEventWatcher(
                Paths.get(appConfig.getKubernetes().getLogdir()),
                false,
                Pattern.compile("example-input_teragrep_.*"),
                statefulFileReader,
                500,
                TimeUnit.MILLISECONDS,
                appConfig.getKubernetes().getMaxLogReadingThreads()
            )
        );
        dew.start();
        Assertions.assertDoesNotThrow(() -> Thread.sleep(1000));
        Assertions.assertEquals(5, messageList.size());
        Assertions.assertEquals("Start of log", messageList.get(0));
        Assertions.assertEquals("Big message incoming", messageList.get(1));
        Assertions.assertEquals("Big partial message start glue one glue two glue end", messageList.get(2));
        Assertions.assertEquals("Big message sent", messageList.get(3));
        Assertions.assertEquals("End of log", messageList.get(4));
    }
}
