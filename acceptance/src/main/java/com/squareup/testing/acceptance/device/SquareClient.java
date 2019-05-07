package com.squareup.testing.acceptance.device;

import com.google.common.collect.ImmutableMap;
import com.squareup.testing.acceptance.IntegrationHelper;
import com.squareup.testing.acceptance.api.LogIn;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.entity.ContentType;
import org.bouncycastle.jcajce.provider.digest.SHA1;

public class SquareClient extends Device {
  private final String product;
  private final String platform;
  private final String version;
  private final String userAgent;
  private final String deviceId;

  private String sessionToken;
  private String userToken;

  public SquareClient(String product, String platform, String version,
      IntegrationHelper integrationHelper) {
    super(new HttpNetworkStack(), integrationHelper);

    this.product = product;
    this.platform = platform;
    this.version = version;
    userAgent = getUserAgent(product, platform, version);
    try {
      deviceId = Base64.encodeBase64URLSafeString(
          SHA1.Digest.getInstance("SHA1").digest(getDeviceName().getBytes("UTF-8")));
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public void logIn(String email, String password) {
    LogIn.Response response = perform(new LogIn(email, password));
    sessionToken = response.session_token;
    userToken = response.user_token;
  }

  @Override public Map<String, String> getHeaders() {
    ImmutableMap.Builder<String, String> builder = ImmutableMap.<String, String>builder()
        .put("User-Agent", userAgent)
        .put("X-Device-ID", deviceId)
        .put("Accept", ContentType.APPLICATION_JSON.getMimeType());
    if (sessionToken != null) {
      builder.put("Authorization", "Session " + sessionToken);
    }
    return builder.build();
  }

  public String getDeviceName() {
    return "my " + product + " version " + version;
  }

  public static String getUserAgent(String product, String platform, String version) {
    String build = "367";
    String square_version =
        "square-" + version; // todo: this probably isn't right, what does Android send?

    if (platform.equals("Android")) {
      return "Square/1441bb24 (Android 2.2.2; HTC google Nexus One) Version/" + square_version;
    } else if (platform.equals("iPad")) {
      return "Mozilla/5.0 (iPad; CPU iPhone OS 3.2.1 like Mac OS X; en-us) Version/"
          + version + " com.squareup." + product + "/" + build;
    } else if (platform.equals("iPhone")) {
      return "Mozilla/5.0 (iPhone 3G; CPU iPhone OS 3.1.3 like Mac OS X; en-us) Version/"
          + version + " com.squareup." + product + "/" + build;
    } else if (platform.equals("iPod")) {
      return "Mozilla/5.0 (iPod touch 3G; CPU iPhone OS 4.1 like Mac OS X; en-us) Version/"
          + version + " com.squareup." + product + "/" + build;
    } else {
      throw new RuntimeException("unknown platform " + platform);
    }
  }
}
