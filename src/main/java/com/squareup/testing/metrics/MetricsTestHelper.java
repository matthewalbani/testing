package com.squareup.testing.metrics;

import com.squareup.common.metrics.SquareMetrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import java.util.Objects;
import java.util.Optional;
import javax.inject.Inject;

public class MetricsTestHelper {
  @Inject SquareMetrics metrics;

  public Gauge<Long> findGauge(MetricName name) {
    return (Gauge<Long>) getMetric(name);
  }

  public void resetCounters() {
    metrics.getMetricsRegistry().allMetrics().values().stream()
        .filter(metric -> metric instanceof Counter)
        .map(metric -> (Counter) metric)
        .forEach(Counter::clear);
  }

  public long gaugeValue(MetricName name) {
    return findGauge(name).value();
  }

  public long counterValue(Class clazz, String name) {
    return counterValue(new MetricName(clazz, name));
  }

  public long counterValue(MetricName name) {
    return ((Counter) getMetric(name)).count();
  }

  public long counterValueOrZero(MetricName name) {
    return maybeGetMetric(name)
        .map(metric -> ((Counter) getMetric(name)).count())
        .orElse(0L);
  }

  public Metric getMetric(MetricName name) {
    return maybeGetMetric(name).orElseThrow(
        () -> new NullPointerException(String.format("%s not found", name)));
  }

  public Optional<Metric> maybeGetMetric(MetricName name) {
    Metric metric = metrics.getMetricsRegistry().allMetrics().get(name);
    return Optional.ofNullable(metric);
  }
}
