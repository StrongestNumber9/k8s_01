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

import java.util.Arrays;

public class ByteRecord {
    private final byte[] record;
    ByteRecord(byte[] record) {
        this.record=record;
    }

    public KubernetesLogFilePOJOImpl toKubePOJO() {
        int[] spaceOffsets = new int[4];
        int currentSpace = 0;
        for (int i=0; i<record.length && currentSpace<3; i++) {
            if(record[i]==' ') {
                spaceOffsets[currentSpace]=i;
                currentSpace++;
            }
        }
        if(currentSpace != 3) {
            throw new RuntimeException("Record did not populate all expected fields");
        }
        return new KubernetesLogFilePOJOImpl(
                Arrays.copyOfRange(record, 0, spaceOffsets[0]), // timestamp
                Arrays.copyOfRange(record, spaceOffsets[0]+1, spaceOffsets[1]), // Stream
                Arrays.copyOfRange(record, spaceOffsets[1]+1, spaceOffsets[2]), // Partial
                Arrays.copyOfRange(record, spaceOffsets[2]+1, record.length) // payload
        );
    }
}
