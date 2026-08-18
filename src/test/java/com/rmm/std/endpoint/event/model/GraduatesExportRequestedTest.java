package com.rmm.std.endpoint.event.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class GraduatesExportRequestedTest {

  @Test
  void maxConsumerDuration_isSixtySeconds() {
    assertEquals(Duration.ofSeconds(60), new GraduatesExportRequested().maxConsumerDuration());
  }

  @Test
  void maxConsumerBackoffBetweenRetries_isThirtySeconds() {
    assertEquals(
        Duration.ofSeconds(30), new GraduatesExportRequested().maxConsumerBackoffBetweenRetries());
  }
}
