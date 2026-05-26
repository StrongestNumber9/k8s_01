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
        int rounds = 1_000_000;
        byte[] record = "2026-05-08T11:11:11.123456789+03:00 stdout F Start message".getBytes(StandardCharsets.UTF_8);
        Instant start = Instant.now();
        for(int i = 0; i < rounds; i++) {
            KubernetesLogFilePOJO log = new ByteRecord(record).toKubePOJO();
            Assertions.assertNotNull(log.payloadString());
        }
        Instant end = Instant.now();
        long duration = end.toEpochMilli() - start.toEpochMilli();
        printStats("small events", rounds, duration, record.length);
    }

    @Test
    public void testParseBig() {
        int rounds = 100_000;
        String payload = "Start message" + "X".repeat(10*1024);
        byte[] record = ("2026-05-08T11:11:11.123456789+03:00 stdout F " + payload).getBytes(StandardCharsets.UTF_8);
        Instant start = Instant.now();
        for(int i = 0; i < rounds; i++) {
            KubernetesLogFilePOJO log = new ByteRecord(record).toKubePOJO();
            Assertions.assertNotNull(log.payloadString());
        }
        Instant end = Instant.now();
        long duration = end.toEpochMilli() - start.toEpochMilli();
        printStats("big events", rounds, duration, record.length);
    }

    @Test
    public void testParseVeryBig() {
        int rounds = 10_000;
        String payload = "Start message" + "X".repeat(100*1024);
        byte[] record = ("2026-05-08T11:11:11.123456789+03:00 stdout F " + payload).getBytes(StandardCharsets.UTF_8);
        Instant start = Instant.now();
        for(int i = 0; i < rounds; i++) {
            KubernetesLogFilePOJO log = new ByteRecord(record).toKubePOJO();
            Assertions.assertNotNull(log.payloadString());
        }
        Instant end = Instant.now();
        long duration = end.toEpochMilli() - start.toEpochMilli();
        printStats("very big events", rounds, duration, record.length);
    }

    @Test
    public void testAppendSmall() {
        int rounds = 1_000_000;
        byte[] first = "2026-05-08T11:11:11.123456789+03:00 stdout P Start message".getBytes(StandardCharsets.UTF_8);
        byte[] second = "2026-05-08T12:12:12.987654321+03:00 stdout F , end here".getBytes(StandardCharsets.UTF_8);
        Instant start = Instant.now();
        for(int i = 0; i < rounds; i++) {
            KubernetesLogFilePOJO log = new ByteRecord(first).toKubePOJO();
            KubernetesLogFilePOJO append = new ByteRecord(second).toKubePOJO();
            KubernetesLogFilePOJO combined = log.append(append.payload());
            Assertions.assertNotNull(combined.payloadString());
        }
        Instant end = Instant.now();
        long duration = end.toEpochMilli() - start.toEpochMilli();
        printStats("append small", rounds, duration, first.length + second.length);
    }

    @Test
    public void testAppendBig() {
        int rounds = 100_000;
        String payload = "X".repeat(10*1024);
        byte[] first = ("2026-05-08T11:11:11.123456789+03:00 stdout P "+payload).getBytes(StandardCharsets.UTF_8);
        byte[] second = ("2026-05-08T12:12:12.987654321+03:00 stdout F "+payload).getBytes(StandardCharsets.UTF_8);
        Instant start = Instant.now();
        for(int i = 0; i < rounds; i++) {
            KubernetesLogFilePOJO log = new ByteRecord(first).toKubePOJO();
            KubernetesLogFilePOJO append = new ByteRecord(second).toKubePOJO();
            KubernetesLogFilePOJO combined = log.append(append.payload());
            Assertions.assertNotNull(combined.payloadString());
        }
        Instant end = Instant.now();
        long duration = end.toEpochMilli() - start.toEpochMilli();
        printStats("append big", rounds, duration, first.length + second.length);
    }

    private void printStats(String name, int rounds, float duration, int recordSize) {
        DecimalFormat df = new DecimalFormat("#.##");
        float totalSizeMB = ((float) recordSize * rounds)/1024f/1024f;
        System.out.println("Ran <"+name+"> for <"+String.format("%,d", rounds)+"> rounds in " + duration + " milliseconds (" + df.format(rounds/duration) + " kEPS, event size " + recordSize + ", total of " + df.format(totalSizeMB) + " MB, " + df.format(totalSizeMB/(duration/1000f)) +" MB/s))");
    }
}
