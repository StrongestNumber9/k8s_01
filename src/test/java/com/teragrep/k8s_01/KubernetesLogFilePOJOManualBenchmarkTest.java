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
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.Instant;

@EnabledIfSystemProperty(
        named = "runManualBenchmarkTest",
        matches = "true"
)
public class KubernetesLogFilePOJOManualBenchmarkTest {
    @Test
    public void testParseSmall() {
        float rounds = 1_000_000;
        byte[] record = "2026-05-08T11:11:11.123456789+03:00 stdout F Start message".getBytes(StandardCharsets.UTF_8);
        Instant start = Instant.now();
        for(int i = 0; i < rounds; i++) {
            KubernetesLogFilePOJO log = new KubernetesLogFilePOJOImpl(record);
            Assertions.assertNotNull(log.payload());
        }
        Instant end = Instant.now();
        long duration = end.toEpochMilli() - start.toEpochMilli();
        printStats("small events", rounds, duration, record.length);
    }

    @Test
    public void testParseBig() {
        float rounds = 100_000;
        String payload = "Start message" + "X".repeat(10*1024);
        byte[] record = ("2026-05-08T11:11:11.123456789+03:00 stdout F " + payload).getBytes(StandardCharsets.UTF_8);
        Instant start = Instant.now();
        for(int i = 0; i < rounds; i++) {
            KubernetesLogFilePOJO log = new KubernetesLogFilePOJOImpl(record);
            Assertions.assertNotNull(log.payload());
        }
        Instant end = Instant.now();
        long duration = end.toEpochMilli() - start.toEpochMilli();
        printStats("big events", rounds, duration, record.length);
    }

    @Test
    public void testParseVeryBig() {
        float rounds = 10_000;
        String payload = "Start message" + "X".repeat(100*1024);
        byte[] record = ("2026-05-08T11:11:11.123456789+03:00 stdout F " + payload).getBytes(StandardCharsets.UTF_8);
        Instant start = Instant.now();
        for(int i = 0; i < rounds; i++) {
            KubernetesLogFilePOJO log = new KubernetesLogFilePOJOImpl(record);
            Assertions.assertNotNull(log.payload());
        }
        Instant end = Instant.now();
        long duration = end.toEpochMilli() - start.toEpochMilli();
        printStats("very big events", rounds, duration, record.length);
    }

    @Test
    public void testAppendSmall() {
        float rounds = 1_000_000;
        byte[] first = "2026-05-08T11:11:11.123456789+03:00 stdout P Start message".getBytes(StandardCharsets.UTF_8);
        byte[] second = "2026-05-08T12:12:12.987654321+03:00 stdout F , end here".getBytes(StandardCharsets.UTF_8);
        Instant start = Instant.now();
        for(int i = 0; i < rounds; i++) {
            KubernetesLogFilePOJO log = new KubernetesLogFilePOJOImpl(first);
            KubernetesLogFilePOJO append = new KubernetesLogFilePOJOImpl(second);
            KubernetesLogFilePOJO combined = log.append(append.payload());
            Assertions.assertNotNull(combined.payload());
        }
        Instant end = Instant.now();
        long duration = end.toEpochMilli() - start.toEpochMilli();
        printStats("append small", rounds, duration, first.length + second.length);
    }

    @Test
    public void testAppendBig() {
        float rounds = 100_000;
        String payload = "X".repeat(10*1024);
        byte[] first = ("2026-05-08T11:11:11.123456789+03:00 stdout P "+payload).getBytes(StandardCharsets.UTF_8);
        byte[] second = ("2026-05-08T12:12:12.987654321+03:00 stdout F "+payload).getBytes(StandardCharsets.UTF_8);
        Instant start = Instant.now();
        for(int i = 0; i < rounds; i++) {
            KubernetesLogFilePOJO log = new KubernetesLogFilePOJOImpl(first);
            KubernetesLogFilePOJO append = new KubernetesLogFilePOJOImpl(second);
            KubernetesLogFilePOJO combined = log.append(append.payload());
            Assertions.assertNotNull(combined.payload());
        }
        Instant end = Instant.now();
        long duration = end.toEpochMilli() - start.toEpochMilli();
        printStats("append big", rounds, duration, first.length + second.length);
    }

    private void printStats(String name, float rounds, float duration, int recordSize) {
        DecimalFormat df = new DecimalFormat("#.##");
        float totalSizeMB = (recordSize * rounds)/1024/1024;
        System.out.println("Ran <"+name+"> for <"+rounds+"> rounds in " + duration + " milliseconds (" + df.format((rounds/duration)*1000/1024) + " kEPS, event size " + recordSize + ", total of " + df.format(totalSizeMB) + " MB, " + df.format(totalSizeMB/duration*1000) +" MB/s))");
    }
}
