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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class KubernetesLogFilePOJOImpl implements KubernetesLogFilePOJO {
    private final Fragment timestamp;
    private final Fragment stream;
    private final Fragment partial;
    private final Fragment log;
    private final List<Fragment> logs;

    public KubernetesLogFilePOJOImpl(byte[] record) {
        this.timestamp = new Fragment(64, new SpaceDelimiterFunction());
        this.stream = new Fragment(64, new SpaceDelimiterFunction());
        this.partial = new Fragment(64, new SpaceDelimiterFunction());
        this.log = new Fragment(256*1024, new LogReaderFunction());
        this.logs = new ArrayList<>();
        ByteStream byteStream = new ByteStream(new ByteArrayInputStream(record));
        Consumer<ByteStream> streamConsumer = timestamp.andThen(
                stream.andThen(
                        partial.andThen(
                                log
                        )
                )
        );
        this.logs.add(log);
        byteStream.next();
        streamConsumer.accept(byteStream);
    }

    public KubernetesLogFilePOJOImpl(Fragment timestamp, Fragment stream, Fragment partial, Fragment log, List<Fragment> logs) {
        this.timestamp = timestamp;
        this.stream = stream;
        this.partial = partial;
        this.log = log;
        this.logs = logs;
        logs.add(log);
    }


    public KubernetesLogFilePOJO append(Fragment log) {
        return new KubernetesLogFilePOJOImpl(timestamp, stream, partial, log, logs);
    }

    public String timestamp() {
        return timestamp.toString();
    }

    public String stream() {
        return stream.toString();
    }

    public boolean partial() {
        return partial.toString().equalsIgnoreCase("P");
    }

    public Fragment payloadFragment() {
        return log;
    }

    public String payload() {
        StringBuilder stringBuilder = new StringBuilder();
        for(Fragment fragment : logs) {
            stringBuilder.append(fragment.toString());
        }
        return stringBuilder.toString();
    }

    @Override
    public boolean stub() {
        return false;
    }
}