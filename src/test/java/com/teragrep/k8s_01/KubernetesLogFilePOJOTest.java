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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

public class KubernetesLogFilePOJOTest {
    @Test
    public void testTimestampFormat() {
        String record = "2026-05-08T13:18:22.542002411+03:00 stdout F Timestamp Test";
        KubernetesLogFilePOJO log = new ByteRecord(record.getBytes()).toKubePOJO();
        Assertions.assertEquals("2026-05-08T13:18:22.542002411+03:00", log.timestamp());
    }

    @Test
    public void testTimestampParsesToInstant() {
        String record = "2026-05-08T13:18:22.542002411+03:00 stdout F Timestamp Test";
        KubernetesLogFilePOJO log = new ByteRecord(record.getBytes()).toKubePOJO();
        Instant timestamp = Instant.parse(log.timestamp());
        Assertions.assertEquals("2026-05-08T10:18:22.542002411Z", timestamp.toString());
    }

    @Test
    public void testStream() {
        String record = "2026-05-08T13:18:22.542002411+03:00 stdout F Stream Test";
        KubernetesLogFilePOJO log = new ByteRecord(record.getBytes()).toKubePOJO();
        Assertions.assertEquals("stdout", log.stream());
    }

    @Test
    public void testIsPartialFalse() {
        String record = "2026-05-08T13:18:22.542002411+03:00 stdout F Partial Test / False";
        KubernetesLogFilePOJO log = new ByteRecord(record.getBytes()).toKubePOJO();
        Assertions.assertFalse(log.isPartial());
    }

    @Test
    public void testIsPartialTrue() {
        String record = "2026-05-08T13:18:22.542002411+03:00 stdout P Partial Test / True";
        KubernetesLogFilePOJO log = new ByteRecord(record.getBytes()).toKubePOJO();
        Assertions.assertTrue(log.isPartial());
    }

    @Test
    public void testPayloadFragment() {
        String record = "2026-05-08T13:18:22.542002411+03:00 stdout F Log Test";
        KubernetesLogFilePOJO log = new ByteRecord(record.getBytes()).toKubePOJO();
        Assertions.assertEquals("Log Test", log.payload().toString());
    }

    @Test
    public void testStub() {
        String record = "2026-05-08T13:18:22.542002411+03:00 stdout F Stub Test";
        KubernetesLogFilePOJO log = new ByteRecord(record.getBytes()).toKubePOJO();
        Assertions.assertFalse(log.isStub());
    }

    @Test
    public void testAppend() {
        String record_start = "2026-05-08T11:11:11.123456789+03:00 stdout P Start message";
        String record_middle = "2026-05-08T12:12:12.234567890+03:00 stdout P , middle here";
        String record_end = "2026-05-08T12:12:12.987654321+03:00 stdout F , end here";
        KubernetesLogFilePOJO start = new ByteRecord(record_start.getBytes()).toKubePOJO();
        KubernetesLogFilePOJO middle = new ByteRecord(record_middle.getBytes()).toKubePOJO();
        KubernetesLogFilePOJO end = new ByteRecord(record_end.getBytes()).toKubePOJO();
        KubernetesLogFilePOJO combined = start.append(middle.payload()).append(end.payload());
        // Timestamp should not change when appending

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        List<KubernetesPayloadPOJO> payloads = combined.payloads();
        for(KubernetesPayloadPOJO payload : payloads) {
            byteArrayOutputStream.writeBytes(payload.payload());
        }
        Assertions.assertEquals(start.timestamp(), combined.timestamp());
        Assertions.assertNotEquals(end.timestamp(), combined.timestamp());
        Assertions.assertEquals("Start message, middle here, end here", byteArrayOutputStream.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void testInvalidRecored() {
        String record = "2026-05-08T13:18:22.542002411+03:00 stdou";
        Assertions.assertThrowsExactly(RuntimeException.class, () -> new ByteRecord(record.getBytes()).toKubePOJO());
    }
}
