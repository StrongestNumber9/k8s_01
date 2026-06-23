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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class KubernetesLogFilePOJOImpl implements KubernetesLogFilePOJO {
    private final byte[] timestamp;
    private final byte[] stream;
    private final byte[] partial;
    private final List<KubernetesPayloadPOJO> payloads;

    public KubernetesLogFilePOJOImpl(byte[] timestamp, byte[] stream, byte[] partial, KubernetesPayloadPOJO payload) {
        this(timestamp, stream, partial, Collections.singletonList(payload));
    }

    public KubernetesLogFilePOJOImpl(byte[] timestamp, byte[] stream, byte[] partial, List<KubernetesPayloadPOJO> payloads) {
        this.timestamp = timestamp;
        this.stream = stream;
        this.partial = partial;
        this.payloads = payloads;
    }

    @Override
    public KubernetesLogFilePOJO append(List<KubernetesPayloadPOJO> kubernetesPayloadPOJOs) {
        List<KubernetesPayloadPOJO> newPayloads = new ArrayList<>(this.payloads);
        newPayloads.addAll(kubernetesPayloadPOJOs);
        return new KubernetesLogFilePOJOImpl(timestamp, stream, partial, newPayloads);
    }

    @Override
    public String timestamp() {
        return new String(timestamp, StandardCharsets.UTF_8);
    }

    @Override
    public String stream() {
        return new String(stream, StandardCharsets.UTF_8);
    }

    @Override
    public boolean isPartial() {
        return new String(partial, StandardCharsets.UTF_8).equalsIgnoreCase("P");
    }

    @Override
    public List<KubernetesPayloadPOJO> payloads() {
        return payloads;
    }

    @Override
    public boolean isStub() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        KubernetesLogFilePOJOImpl that = (KubernetesLogFilePOJOImpl) o;
        return Objects.deepEquals(timestamp, that.timestamp) && Objects.deepEquals(stream, that.stream) && Objects.deepEquals(partial, that.partial) && Objects.equals(payloads, that.payloads);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(timestamp), Arrays.hashCode(stream), Arrays.hashCode(partial), payloads);
    }
}