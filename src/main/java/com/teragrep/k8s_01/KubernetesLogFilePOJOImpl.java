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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KubernetesLogFilePOJOImpl implements KubernetesLogFilePOJO {
    private final byte[] timestamp;
    private final byte[] stream;
    private final byte[] partial;
    private final byte[] log;
    private final List<byte[]> logs;

    public KubernetesLogFilePOJOImpl(byte[] timestamp, byte[] stream, byte[] partial, byte[] log) {
        this(timestamp, stream, partial, log, new ArrayList<>(Collections.singletonList(log)));
    }

    public KubernetesLogFilePOJOImpl(byte[] timestamp, byte[] stream, byte[] partial, byte[] log, List<byte[]> logs) {
        this.timestamp = timestamp;
        this.stream = stream;
        this.partial = partial;
        this.log = log;
        this.logs = logs;
    }

    public KubernetesLogFilePOJO append(byte[] newLog) {
        this.logs.add(newLog);
        return new KubernetesLogFilePOJOImpl(timestamp, stream, partial, newLog, logs);
    }

    public String timestamp() {
        return new String(timestamp, StandardCharsets.UTF_8);
    }

    public String stream() {
        return new String(stream, StandardCharsets.UTF_8);
    }

    public boolean isPartial() {
        return new String(partial, StandardCharsets.UTF_8).equalsIgnoreCase("P");
    }

    public byte[] payload() {
        return log;
    }

    public String payloadString() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for(byte[] bytes : logs) {
            byteArrayOutputStream.writeBytes(bytes);
        }
        return byteArrayOutputStream.toString(StandardCharsets.UTF_8);
    }

    @Override
    public boolean isStub() {
        return false;
    }
}