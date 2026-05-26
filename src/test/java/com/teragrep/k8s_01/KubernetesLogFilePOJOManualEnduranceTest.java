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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.List;

@EnabledIfSystemProperty(
        named = "runManualEnduranceTest",
        matches = "true"
)
public class KubernetesLogFilePOJOManualEnduranceTest {

    @Test
    public void testEnduranceMini() {
        byte[] record = "2026-05-08T12:12:12.987654321+03:00 stdout F payload".getBytes(StandardCharsets.UTF_8);
        runEndurance("mini", record);
    }

    @Test
    public void testEnduranceSmall() {
        String payload = "X".repeat(1024);
        byte[] record = ("2026-05-08T12:12:12.987654321+03:00 stdout F payload " + payload).getBytes(StandardCharsets.UTF_8);
        runEndurance("small", record);
    }

    @Test
    public void testEnduranceBig() {
        String payload = "X".repeat(512*1024);
        byte[] record = ("2026-05-08T12:12:12.987654321+03:00 stdout F payload " + payload).getBytes(StandardCharsets.UTF_8);
        runEndurance("big",  record);
    }

    private void runEndurance(String name, byte[] record) {
        float payloadSize = (float) record.length;
        System.out.println("Running <" + name + "> with <" + payloadSize + "> record size");
        final boolean[] run = {true};
        new Thread(() -> {
            try {
                Thread.sleep(60*1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            run[0] = false;
        }).start();

        int rounds = 0;
        Instant start = Instant.now();
        while(run[0]) {
            KubernetesLogFilePOJO log = new ByteRecord(record).toKubePOJO();
            Assertions.assertNotNull(log.payloadString());
            rounds++;
        }
        Instant end = Instant.now();
        long duration = end.toEpochMilli() - start.toEpochMilli();
        DecimalFormat df = new DecimalFormat("#.##");
        float totalSizeMB = (payloadSize * rounds)/1024f/1024f;
        System.out.println("Ran <"+name+"> for <"+String.format("%,d", rounds)+"> rounds in " + (duration/1000f) + " seconds (" + df.format((float)rounds/duration) + " kEPS, event size " + payloadSize + ", total of " + df.format(totalSizeMB) + " MB, " + df.format(totalSizeMB/(duration/1000f)) +" MB/s)");
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            System.out.println("GC Name: " + gcBean.getName() + ", count: " + gcBean.getCollectionCount() + ", time: " + gcBean.getCollectionTime() + " ms");
        }
        System.out.println("=========");
    }
}
