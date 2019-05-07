package com.squareup.testing.net;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.squareup.logging.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.io.IOUtils;

import static com.google.common.collect.Lists.newArrayList;

/** Simple test proxy */
public class TestProxy implements Runnable {
  private static final Logger logger = Logger.getLogger(TestProxy.class);

  public interface LoadBalancer {
    int selectBackendPort(int[] backendPorts);
  }

  public static class RoundRobinLoadBalancer implements LoadBalancer {
    private final AtomicInteger nextPort = new AtomicInteger(0);

    @Override public int selectBackendPort(int[] backendPorts) {
      return backendPorts[nextPort.getAndIncrement() % backendPorts.length];
    }
  }

  public static class RandomLoadBalancer implements LoadBalancer {
    private static final Random RANDOM = new Random();

    @Override public int selectBackendPort(int[] backendPorts) {
      return backendPorts[RANDOM.nextInt(backendPorts.length)];
    }
  }

  private final LoadBalancer loadBalancer;
  private final ServerSocket listenSocket;
  private int[] backendPorts;
  private final AtomicBoolean running = new AtomicBoolean(true);
  private final List<Processor> processors = new CopyOnWriteArrayList<Processor>();
  private final Thread acceptorThread;

  public TestProxy(int listenPort, LoadBalancer loadBalancer) throws IOException {
    this.listenSocket = new ServerSocket(listenPort);
    this.listenSocket.setReuseAddress(true);
    this.acceptorThread = new Thread(this);
    this.loadBalancer = loadBalancer;
  }

  public void start() {
    acceptorThread.start();
  }

  public synchronized void setBackends(int[] backendPorts) {
    this.backendPorts = backendPorts;
  }

  @Override public void run() {
    while (running.get()) {
      try {
        Socket socket = listenSocket.accept();
        logger.info("Accepted connection from %s:%d", socket.getInetAddress(), socket.getPort());
        int backendPort = loadBalancer.selectBackendPort(backendPorts);
        logger.info("Routing to %d", backendPort);
        processors.add(new Processor(socket, new Socket("127.0.0.1", backendPort)));
      } catch (IOException e) {
        // Shutting down
      }
    }
  }

  public void stop() throws IOException {
    running.set(false);
    listenSocket.close();

    for (Processor processor : processors) {
      processor.stop();
    }
  }

  class Processor {
    private final Thread requestThread;
    private final Thread responseThread;
    private final InputStream clientIn;
    private final OutputStream clientOut;
    private final InputStream serverIn;
    private final OutputStream serverOut;

    Processor(Socket clientSocket, Socket backEnd) throws IOException {
      clientIn = clientSocket.getInputStream();
      serverOut = backEnd.getOutputStream();
      serverIn = backEnd.getInputStream();
      clientOut = clientSocket.getOutputStream();

      this.requestThread = new Thread(new Runnable() {
        @Override public void run() {
          forwardRequests();
        }
      });
      this.requestThread.start();

      this.responseThread = new Thread(new Runnable() {
        @Override public void run() {
          forwardResponses();
        }
      });
      this.responseThread.start();
    }

    void stop() {
      try {
        clientOut.close();
      } catch (IOException ignored) { /* Ok */ }

      try {
        serverOut.close();
      } catch (IOException ignored) { /* Ok */ }

      processors.remove(this);
    }

    void forwardRequests() {
      try {
        IOUtils.copy(clientIn, serverOut);
      } catch (IOException e) {
        // Shutdown
      }
    }

    void forwardResponses() {
      try {
        IOUtils.copy(serverIn, clientOut);
      } catch (IOException e) {
        // Shutdown
      }
    }
  }

  public static class Options {
    @Parameter(names = {"-p", "--port"}) int port = 9090;
    @Parameter(names = {"-b", "--backend"}) List<Integer> backends = newArrayList();
  }

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    new JCommander(options).parse(args);

    int[] backends = new int[options.backends.size()];
    for (int i = 0; i < backends.length; i++) {
      backends[i] = options.backends.get(i);
    }

    TestProxy proxy = new TestProxy(options.port, new RandomLoadBalancer());
    proxy.setBackends(backends);
    proxy.run();
  }
}
