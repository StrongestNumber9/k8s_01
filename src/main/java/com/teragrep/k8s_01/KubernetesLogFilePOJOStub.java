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

import java.util.List;

public class KubernetesLogFilePOJOStub implements KubernetesLogFilePOJO {
    public KubernetesLogFilePOJOStub append(KubernetesPayloadPOJO kubernetesPayloadPOJO) {
        throw new UnsupportedOperationException("Stub does not support append");
    }

    public String timestamp() {
        throw new UnsupportedOperationException("Stub does not support timestamp");
    }

    public String stream() {
        throw new UnsupportedOperationException("Stub does not support stream");
    }

    public boolean isPartial() {
        throw new UnsupportedOperationException("Stub does not support partial");
    }


    public KubernetesPayloadPOJO payload() {
        throw new UnsupportedOperationException("Stub does not support payload");
    }

    public List<KubernetesPayloadPOJO> payloads() {
        throw new UnsupportedOperationException("Stub does not support payloads");
    }

    public String payloadString() {
        throw new UnsupportedOperationException("Stub does not support payloadString");
    }

    @Override
    public String toString() {
        return "KubernetesLogFilePOJOStub{}";
    }

    @Override
    public boolean isStub() {
        return false;
    }
}
