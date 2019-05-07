package com.squareup.testing.integration;

import com.google.common.io.ByteStreams;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.JsonFormat;
import com.google.protobuf.Message;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Collection;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

public class ProtoJsonHttpClient {

  private static final Logger logger = Logger.getLogger(ProtoJsonHttpClient.class.getCanonicalName());

  public <T> T get(
      URI uri,
      String sessionToken,
      Class<T> responseClazz)
      throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    return execute(new HttpGet(uri), sessionToken, responseClazz, null);
  }

  public <T> T post(
      URI uri,
      Message requestProto,
      @Nullable String sessionToken,
      Class<T> responseClazz)
      throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    return post(uri, requestProto, sessionToken, responseClazz, null);
  }

  public <T> T post(
      URI uri,
      Message requestProto,
      String username, String password,
      Class<T> responseClazz)
      throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    return post(uri, requestProto, username, password, responseClazz, null);
  }

  public <T> T post(
      URI uri,
      Message requestProto,
      @Nullable String sessionToken,
      Collection<Header> headers,
      Class<T> responseClazz,
      ExtensionRegistry extensionRegistry)
      throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    HttpPost request = new HttpPost(uri);
    for (Header header : headers) {
      request.addHeader(header);
    }
    request.setEntity(new StringEntity(JsonFormat.printToString(requestProto)));
    return execute(request, sessionToken, responseClazz, extensionRegistry);
  }

  public <T> T post(
      URI uri,
      Message requestProto,
      @Nullable String sessionToken,
      Class<T> responseClazz,
      ExtensionRegistry extensionRegistry)
      throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    HttpPost request = new HttpPost(uri);
    request.setEntity(new StringEntity(JsonFormat.printToString(requestProto)));
    return execute(request, sessionToken, responseClazz, extensionRegistry);
  }

  public <T> T post(
      URI uri,
      Message requestProto,
      String username, String password,
      Class<T> responseClazz,
      ExtensionRegistry extensionRegistry)
      throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    HttpPost request = new HttpPost(uri);
    request.setEntity(new StringEntity(JsonFormat.printToString(requestProto)));
    return execute(request, username, password, responseClazz, extensionRegistry);
  }

  public <T> T put(
      URI uri,
      Message requestProto,
      @Nullable String sessionToken,
      Class<T> responseClazz)
      throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    HttpPut request = new HttpPut(uri);
    request.setEntity(new StringEntity(JsonFormat.printToString(requestProto)));
    return execute(request, sessionToken, responseClazz, null);
  }

  private <T> T execute(
      HttpUriRequest request,
      @Nullable String sessionToken,
      Class<T> responseClazz,
      ExtensionRegistry extensionRegistry)
      throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    if (sessionToken != null) {
      request.addHeader("Authorization", "Session " + sessionToken);
    }

    return execute(request, responseClazz, extensionRegistry);
  }

  private <T> T execute(
      HttpUriRequest request,
      String username, String password,
      Class<T> responseClazz,
      ExtensionRegistry extensionRegistry)
      throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {

    request.addHeader(
        "Authorization",
        "Basic " + Base64.encodeBase64String(
            String.format("%s:%s", username, password).getBytes()));

    return execute(request, responseClazz, extensionRegistry);
  }

  private <T> T execute(
      HttpUriRequest request,
      Class<T> responseClazz,
      ExtensionRegistry extensionRegistry)
      throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    request.addHeader("Accept", MediaType.APPLICATION_JSON);
    request.addHeader("Content-Type", MediaType.APPLICATION_JSON);

    Message.Builder builder = (Message.Builder) responseClazz.getMethod("newBuilder").invoke(null);

    try (CloseableHttpResponse response = HttpClients.createDefault().execute(request)) {
      String content =
          new String(ByteStreams.toByteArray(response.getEntity().getContent()), "UTF-8");
      if (response.getStatusLine().getStatusCode() != 200) {
        throw new UnexpectedStatusCodeException(response, content);
      }

      logger.info(request.getURI().toString() + " response: " + content);

      if (extensionRegistry != null) {
        JsonFormat.merge(content, extensionRegistry, builder);
      } else {
        JsonFormat.merge(content, builder);
      }
    }
    return (T) builder.build();
  }
}
