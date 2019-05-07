package com.squareup.testing.metrics;

import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistryListener;

public class TestMetricsCollector<T extends Metric> implements MetricsRegistryListener {
  private T metric;
  private final MetricName metricName;

  public TestMetricsCollector(MetricName metricName) {
    this.metricName = metricName;
  }

  public T getMetric() {
    return metric;
  }

  @SuppressWarnings("unchecked")
  @Override public void onMetricAdded(MetricName metricName, Metric metric) {
    if (this.metricName.equals(metricName)) {
      this.metric = (T) metric;
    }
  }

  @Override public void onMetricRemoved(MetricName metricName) {
  }
}
