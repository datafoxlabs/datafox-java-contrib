/*
 * Copyright The DataFox Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.datafox.contrib.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;

class KafkaSpanExporterTest {

  private static final String TOPIC = "test-traces";

  @Test
  void exportsOtlpProtobufBatchWithProducerCallback() throws Exception {
    MockProducer<String, byte[]> producer = newProducer(false);
    KafkaSpanExporter exporter = KafkaSpanExporter.create(TOPIC, producer);

    CompletableResultCode result = exporter.export(Arrays.asList(span("first"), span("second")));

    assertThat(result.isDone()).isFalse();
    List<ProducerRecord<String, byte[]>> records = producer.history();
    assertThat(records).hasSize(1);
    ProducerRecord<String, byte[]> record = records.get(0);
    assertThat(record.topic()).isEqualTo(TOPIC);
    assertThat(record.key()).isNull();

    ExportTraceServiceRequest request = ExportTraceServiceRequest.parseFrom(record.value());
    assertThat(request.getResourceSpansList()).hasSize(1);
    assertThat(request.getResourceSpans(0).getScopeSpans(0).getSpansList()).hasSize(2);
    assertThat(request.getResourceSpans(0).getScopeSpans(0).getSpans(0).getName())
        .isEqualTo("first");
    assertThat(request.getResourceSpans(0).getScopeSpans(0).getSpans(1).getName())
        .isEqualTo("second");

    assertThat(producer.completeNext()).isTrue();
    assertThat(result.isDone()).isTrue();
    assertThat(result.isSuccess()).isTrue();
    exporter.shutdown();
  }

  @Test
  void failsExportWhenProducerCallbackFails() {
    MockProducer<String, byte[]> producer = newProducer(false);
    KafkaSpanExporter exporter = KafkaSpanExporter.create(TOPIC, producer);

    CompletableResultCode result = exporter.export(Collections.singletonList(span("failed")));
    assertThat(result.isDone()).isFalse();

    assertThat(producer.errorNext(new KafkaException("unavailable"))).isTrue();
    assertThat(result.isDone()).isTrue();
    assertThat(result.isSuccess()).isFalse();
    exporter.shutdown();
  }

  @Test
  void completesFlushAfterPendingSend() {
    MockProducer<String, byte[]> producer = newProducer(false);
    KafkaSpanExporter exporter = KafkaSpanExporter.create(TOPIC, producer);

    CompletableResultCode result = exporter.export(Collections.singletonList(span("flushed")));
    assertThat(exporter.flush().isSuccess()).isTrue();
    assertThat(result.isSuccess()).isTrue();
    exporter.shutdown();
  }

  @Test
  void skipsEmptyExportAndRejectsExportsAfterShutdown() {
    MockProducer<String, byte[]> producer = newProducer(true);
    KafkaSpanExporter exporter = KafkaSpanExporter.create(TOPIC, producer);

    assertThat(exporter.export(Collections.emptyList()).isSuccess()).isTrue();
    assertThat(producer.history()).isEmpty();
    assertThat(exporter.shutdown().isSuccess()).isTrue();
    assertThat(exporter.shutdown().isSuccess()).isTrue();
    assertThat(producer.closed()).isTrue();
    assertThat(exporter.export(Collections.singletonList(span("late"))).isSuccess()).isFalse();
    assertThat(exporter.flush().isSuccess()).isFalse();
  }

  @Test
  void rejectsInvalidConfiguration() {
    MockProducer<String, byte[]> producer = newProducer(true);
    assertThrows(IllegalArgumentException.class, () -> KafkaSpanExporter.create(" ", producer));
    assertThrows(IllegalArgumentException.class, () -> KafkaSpanExporter.create(TOPIC, null));
  }

  private static MockProducer<String, byte[]> newProducer(boolean autoComplete) {
    return new MockProducer<>(autoComplete, new StringSerializer(), new ByteArraySerializer());
  }

  private static SpanData span(String name) {
    return TestSpanData.builder()
        .setHasEnded(true)
        .setSpanContext(
            SpanContext.create(
                "0123456789abcdef0123456789abcdef",
                "0123456789abcdef",
                TraceFlags.getSampled(),
                TraceState.getDefault()))
        .setName(name)
        .setKind(SpanKind.SERVER)
        .setStartEpochNanos(TimeUnit.SECONDS.toNanos(1))
        .setEndEpochNanos(TimeUnit.SECONDS.toNanos(2))
        .setStatus(StatusData.ok())
        .setTotalRecordedEvents(0)
        .setTotalRecordedLinks(0)
        .build();
  }
}
