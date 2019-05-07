package com.squareup.testing.mockito;

import com.squareup.common.reflect.ReflectUtils;
import java.util.Set;
import org.mockito.internal.junit.MismatchReportingTestListener;
import org.mockito.internal.progress.MockingProgress;
import org.mockito.listeners.MockitoListener;

import static org.mockito.internal.progress.ThreadSafeMockingProgress.mockingProgress;

public class MockitoListenerHelper {
  public static void removeTestListener(Class clazz) {
    MockingProgress mockingProgress = mockingProgress();
    Set<MockitoListener> listeners = ReflectUtils.getFieldValue(mockingProgress, "listeners");
    for (MockitoListener listener : listeners) {
      if (listener.getClass().equals(clazz)) {
        mockingProgress.removeListener(listener);
      }
    }
  }

  public static void removeMismatchReportingTestListener() {
    removeTestListener(MismatchReportingTestListener.class);
  }
}
