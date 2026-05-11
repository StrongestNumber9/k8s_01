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

public class KubernetesLogFilePOJO {
    private final String record;
    public KubernetesLogFilePOJO(String record) {
        this.record = record;
    }

    // <timestamp> <stream> <F/P> <message>.
    public String timestamp() {
        return record.split(" ", 4)[0];
    }
    public String stream() {
        return record.split(" ", 4)[1];
    }

    public boolean partial() {
        return record.split(" ", 4)[2].equalsIgnoreCase("P");
    }

    public String log() {
        return record.split(" ", 4)[3];
    }
}
