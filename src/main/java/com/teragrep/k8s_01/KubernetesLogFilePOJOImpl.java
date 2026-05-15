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

import java.util.Objects;

public class KubernetesLogFilePOJOImpl implements KubernetesLogFilePOJO {
    private final String timestamp;
    private final String stream;
    private final boolean partial;
    private final StringBuilder log = new StringBuilder();

    public KubernetesLogFilePOJOImpl(String record) {
        String[] split =  record.split(" ", 4);
        this(split[0], split[1], split[2].equalsIgnoreCase("P"), new StringBuilder().append(split[3].trim()));
    }

    public KubernetesLogFilePOJOImpl(String timestamp, String stream, boolean partial, StringBuilder log) {
        this.timestamp = timestamp;
        this.stream = stream;
        this.partial = partial;
        this.log.append(log);
    }

    public KubernetesLogFilePOJOImpl append(String record) {
        return new KubernetesLogFilePOJOImpl(timestamp, stream, partial, log.append(record));
    }

    public String timestamp() {
        return timestamp;
    }

    public String stream() {
        return stream;
    }

    public boolean partial() {
        return partial;
    }

    public String log() {
        return log.toString();
    }

    @Override
    public boolean stub() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        KubernetesLogFilePOJOImpl that = (KubernetesLogFilePOJOImpl) o;
        return partial == that.partial && Objects.equals(timestamp, that.timestamp) && Objects.equals(stream, that.stream) && Objects.equals(log, that.log);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, stream, partial, log);
    }

    @Override
    public String toString() {
        return "KubernetesLogFilePOJOImpl{" +
                "timestamp='" + timestamp + '\'' +
                ", stream='" + stream + '\'' +
                ", partial=" + partial +
                ", log='" + log + '\'' +
                '}';
    }
}