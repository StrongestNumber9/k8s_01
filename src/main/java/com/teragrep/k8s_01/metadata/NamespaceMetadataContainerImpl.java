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

package com.teragrep.k8s_01.metadata;

import io.kubernetes.client.openapi.models.V1Namespace;
import java.util.Map;

/* POJO for storing V1Namespace information instead of the full object */
public class NamespaceMetadataContainerImpl implements NamespaceMetadataContainer {
    private final String uid;
    private final Map<String, String> labels;
    public NamespaceMetadataContainerImpl(V1Namespace namespace) {
        if(namespace.getMetadata() == null) {
            throw new RuntimeException("Namespace metadata is empty, can't continue.");
        }
        uid = namespace.getMetadata().getUid();
        labels = namespace.getMetadata().getLabels();
    }

    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public Map<String, String> getLabels() {
        return labels;
    }
}
