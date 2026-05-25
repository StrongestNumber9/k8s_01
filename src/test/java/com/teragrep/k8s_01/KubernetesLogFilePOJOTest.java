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

import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class KubernetesLogFilePOJOTest {
    @Test
    public void testTimestampFormat() {
        String record = "2026-05-08T13:18:22.542002411+03:00 stdout F Timestamp Test";
        KubernetesLogFilePOJO log = new KubernetesLogFilePOJOImpl(record.getBytes(StandardCharsets.UTF_8));
        Assertions.assertEquals("2026-05-08T13:18:22.542002411+03:00", log.timestamp());
    }

    @Test
    public void testTimestampParsesToInstant() {
        String record = "2026-05-08T13:18:22.542002411+03:00 stdout F Timestamp Test";
        KubernetesLogFilePOJO log = new KubernetesLogFilePOJOImpl(record.getBytes(StandardCharsets.UTF_8));
        Instant timestamp = Instant.parse(log.timestamp());
        Assertions.assertEquals("2026-05-08T10:18:22.542002411Z", timestamp.toString());
    }

    @Test
    public void testStream() {
        String record = "2026-05-08T13:18:22.542002411+03:00 stdout F Stream Test";
        KubernetesLogFilePOJO log = new KubernetesLogFilePOJOImpl(record.getBytes(StandardCharsets.UTF_8));
        Assertions.assertEquals("stdout", log.stream());
    }

    @Test
    public void testPartialFalse() {
        String record = "2026-05-08T13:18:22.542002411+03:00 stdout F Partial Test / False";
        KubernetesLogFilePOJO log = new KubernetesLogFilePOJOImpl(record.getBytes(StandardCharsets.UTF_8));
        Assertions.assertFalse(log.partial());
    }

    @Test
    public void testPartialTrue() {
        String record = "2026-05-08T13:18:22.542002411+03:00 stdout P Partial Test / True";
        KubernetesLogFilePOJO log = new KubernetesLogFilePOJOImpl(record.getBytes(StandardCharsets.UTF_8));
        Assertions.assertTrue(log.partial());

    }

    @Test
    public void testPayloadFragment() {
        String record = "2026-05-08T13:18:22.542002411+03:00 stdout F Log Test";
        KubernetesLogFilePOJO log = new KubernetesLogFilePOJOImpl(record.getBytes(StandardCharsets.UTF_8));
        Assertions.assertEquals("Log Test", log.payloadString());
    }

    @Test
    public void testStub() {
        String record = "2026-05-08T13:18:22.542002411+03:00 stdout F Stub Test";
        KubernetesLogFilePOJO log = new KubernetesLogFilePOJOImpl(record.getBytes(StandardCharsets.UTF_8));
        Assertions.assertFalse(log.stub());
    }

    @Test
    public void testAppend() {
        String record_start = "2026-05-08T11:11:11.123456789+03:00 stdout P Start message";
        String record_end = "2026-05-08T12:12:12.987654321+03:00 stdout F , end here";
        KubernetesLogFilePOJO log = new KubernetesLogFilePOJOImpl(record_start.getBytes(StandardCharsets.UTF_8));
        KubernetesLogFilePOJO append = new KubernetesLogFilePOJOImpl(record_end.getBytes(StandardCharsets.UTF_8));
        KubernetesLogFilePOJO combined = log.append(append.payload());
        // Timestamp should not change when appending
        Assertions.assertEquals(log.timestamp(), combined.timestamp());
        Assertions.assertNotEquals(append.timestamp(), combined.timestamp());
        Assertions.assertEquals("Start message, end here", combined.payloadString());
    }
}
