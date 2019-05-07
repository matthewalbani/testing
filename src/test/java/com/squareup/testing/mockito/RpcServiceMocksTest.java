package com.squareup.testing.mockito;

import com.squareup.common.Errors;
import com.squareup.protobuf.rpc.RpcController;
import com.squareup.protobuf.rpc.RpcException;
import com.squareup.protobuf.rpc.RpcFutures;
import com.squareup.protos.testing.Example;
import com.squareup.protos.testing.Example.SendDataRequest;
import com.squareup.protos.testing.Example.SendDataResponse;
import org.junit.Before;
import org.junit.ComparisonFailure;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RpcServiceMocksTest {
  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock private Example.ExampleService.Api exampleServiceApi;
  @Mock private RpcController rpcController;

  private SendDataRequest validRequest;
  private SendDataResponse validResponse;
  private SendDataRequest invalidRequest;
  private SendDataResponse invalidResponse;

  @Before
  public void setUp() {
    validRequest = SendDataRequest.newBuilder()
        .setData(Example.Data.getDefaultInstance())
        .build();
    validResponse = SendDataResponse.newBuilder()
        .setData(Example.Data.getDefaultInstance())
        .build();
    invalidRequest = validRequest.toBuilder()
        // Data is a required field
        .clearData()
        .build();
    invalidResponse = validResponse.toBuilder()
        // Data is a required field
        .clearData()
        .build();
  }

  @Test
  public void testValidation_failsRequestWithValidation() {
    RpcServiceMocks.mockMethod(exampleServiceApi::sendData,
        request -> validResponse);
    assertThatThrownBy(() -> sendData(invalidRequest))
        .isInstanceOf(Errors.ErrorsPresentException.class);
  }

  @Test
  public void testValidation_noValidationOnRequest() {
    // Without validation, it passes
    RpcServiceMocks.mockMethodWithoutValidation(exampleServiceApi::sendData,
        request -> validResponse);
    assertThat(sendData(invalidRequest)).isEqualTo(validResponse);
  }

  @Test
  public void testValidation_failsResponseWithValidation() {
    RpcServiceMocks.mockMethod(exampleServiceApi::sendData,
        request -> invalidResponse);
    assertThatThrownBy(() -> sendData(validRequest))
        .isInstanceOf(Errors.ErrorsPresentException.class);
  }

  @Test
  public void testValidation_noValidationOnresponse() {
    // Without validation, it passes
    RpcServiceMocks.mockMethodWithoutValidation(exampleServiceApi::sendData,
        request -> invalidResponse);
    assertThat(sendData(validRequest)).isEqualTo(invalidResponse);
  }

  @Test
  public void testMockMethod() {
    RpcServiceMocks.mockMethod(exampleServiceApi::sendData, request -> validResponse);
    assertThat(sendData(validRequest)).isEqualTo(validResponse);
  }

  @Test
  public void testMockMethodWithExpectedRepsonse() {
    RpcServiceMocks.builder().mock(
        exampleServiceApi::sendData, validRequest, request -> validResponse);
    assertThat(sendData(validRequest)).isEqualTo(validResponse);
  }

  @Test
  public void testMockMethodWithExpectedResponse_failsForDifferentResponse() {
    RpcServiceMocks.builder().mock(exampleServiceApi::sendData,
        SendDataRequest.newBuilder().setData(Example.Data.newBuilder().setInt(123)).build(),
        request -> validResponse);
    assertThatThrownBy(() -> sendData(validRequest)).isInstanceOf(ComparisonFailure.class);
  }

  @Test
  public void testMockMethodWithFuture() {
    RpcServiceMocks.mockMethodWithFuture(exampleServiceApi::sendData,
        request -> RpcFutures.immediateFuture(validResponse));
    assertThat(sendData(validRequest)).isEqualTo(validResponse);
  }

  @Test
  public void testMockMethodWithFuture_throwsException() {
    RpcServiceMocks.mockMethodWithFuture(exampleServiceApi::sendData,
        request -> RpcFutures.immediateFailedFuture(new IllegalArgumentException()));
    assertThatIllegalArgumentException().isThrownBy(() -> sendData(validRequest));
  }

  private SendDataResponse sendData(SendDataRequest request) {
    try {
      return exampleServiceApi.sendData(rpcController, request).checkedGet();
    } catch (RpcException e) {
      throw new RuntimeException(e);
    }
  }
}
