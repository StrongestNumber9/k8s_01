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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class KubernetesLogFilePOJOImpl implements KubernetesLogFilePOJO {
    private final byte[] timestamp;
    private final byte[] stream;
    private final byte[] partial;
    private final byte[] log;
    private final List<byte[]> logs;
    private int offset = 0;

    public KubernetesLogFilePOJOImpl(byte[] record) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(record)));
        this.timestamp = readRecord(bufferedReader);
        this.stream = readRecord(bufferedReader);
        this.partial = readRecord(bufferedReader);
        this.log = readRest(bufferedReader, record.length-offset);
        this.logs = new ArrayList<>();
        this.logs.add(log);
        try {
            bufferedReader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] readRecord(BufferedReader bufferedReader) {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            int ret;
            while ((ret = bufferedReader.read()) != -1) {
                final char c = (char) ret;
                if (c == ' ') {
                    break;
                }
                byteArrayOutputStream.write(c);
            }
        } catch(IOException _) {
        }
        offset += byteArrayOutputStream.size();
        return byteArrayOutputStream.toByteArray();
    }

    private byte[] readRest(BufferedReader bufferedReader, int maxSize) {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            final char[] buffer = new char[maxSize];
            int charsRead;
            while ((charsRead = bufferedReader.read(buffer)) != -1) {
                byteArrayOutputStream.write(StandardCharsets.UTF_8.encode(CharBuffer.wrap(buffer)).array(), 0, charsRead);
            }
        } catch(IOException _) {
        }
        return byteArrayOutputStream.toByteArray();
    }

    public KubernetesLogFilePOJOImpl(byte[] timestamp, byte[] stream, byte[] partial, byte[] log, List<byte[]> logs) {
        this.timestamp = timestamp;
        this.stream = stream;
        this.partial = partial;
        this.log = log;
        this.logs = logs;
        logs.add(log);
    }

    public KubernetesLogFilePOJO append(byte[] log) {
        return new KubernetesLogFilePOJOImpl(timestamp, stream, partial, log, logs);
    }

    public String timestamp() {
        return new String(timestamp, StandardCharsets.UTF_8);
    }

    public String stream() {
        return new String(stream, StandardCharsets.UTF_8);
    }

    public boolean partial() {
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
    public boolean stub() {
        return false;
    }
}