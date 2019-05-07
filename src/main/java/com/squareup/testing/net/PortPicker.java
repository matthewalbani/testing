package com.squareup.testing.net;

import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicInteger;

/** Utility class that helps us pick an available IP port. */
public class PortPicker {
  private static final int MAX_PORT = 2048;
  private static final AtomicInteger lastPort = new AtomicInteger(1024);

  /**
   * Returns an available IP port.
   *
   * <p>Note that the implementation has a race. There is no guarantee that the returned port
   * will not be used by any other process before a caller actually uses it.</p>
   */
  public static int pickPort() {
    int port;
    while ((port = lastPort.incrementAndGet()) < MAX_PORT) {
      if (isPortAvailable(port)) {
        return port;
      }
    }
    throw new RuntimeException("Failed to find a free port");
  }

  private static boolean isPortAvailable(int port) {
    ServerSocket socket = null;
    try {
      try {
        socket = new ServerSocket(port);
        socket.setReuseAddress(true);
        return true;
      } finally {
        if (socket != null) {
          socket.close();
        }
      }
    } catch (Exception ignored) {
    }
    return false;
  }
}
