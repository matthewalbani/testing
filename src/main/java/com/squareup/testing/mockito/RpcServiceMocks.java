package com.squareup.testing.mockito;

import com.google.inject.Guice;
import com.google.protobuf.Message;
import com.squareup.protobuf.rpc.RpcController;
import com.squareup.protobuf.rpc.RpcException;
import com.squareup.protobuf.rpc.RpcFuture;
import com.squareup.protobuf.rpc.RpcFutures;
import com.squareup.protobuf.rpc.RpcProtos;
import com.squareup.service.framework.validation.BuiltInValidationsModule;
import com.squareup.service.framework.validation.RootMessageValidator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkState;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

/**
 * Collection of functions that allow for mocking instances or methods of RPC services.
 */
public class RpcServiceMocks {
  private RpcServiceMocks() {}

  private static final RootMessageValidator validator;

  static {
    validator = Guice.createInjector(new BuiltInValidationsModule())
        .getInstance(RootMessageValidator.class);
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * A builder for various ways to mock out a given API. Use this if you want to parameterize how
   * proto validation is done.
   */
  public static class Builder {
    private boolean withRequestValidation = true;
    private boolean withResponseValidation = true;

    private Builder() {}

    public Builder withRequestValidation(boolean withRequestValidation) {
      this.withRequestValidation = withRequestValidation;
      return this;
    }

    public Builder withResponseValidation(boolean withResponseValidation) {
      this.withResponseValidation = withResponseValidation;
      return this;
    }

    public <RequestT extends Message, ResponseT extends Message> void mock(
        BiFunction<RpcController, RequestT, RpcFuture<ResponseT>> apiMethod,
        final Function<RequestT, ResponseT> delegateMethod) {
      mock(apiMethod, Optional.empty(), delegateMethod);
    }

    public <RequestT extends Message, ResponseT extends Message> void mock(
        BiFunction<RpcController, RequestT, RpcFuture<ResponseT>> apiMethod,
        RequestT expectedRequest,
        final Function<RequestT, ResponseT> delegateMethod) {
      mock(apiMethod, Optional.of(expectedRequest), delegateMethod);
    }

    private <RequestT extends Message, ResponseT extends Message> void mock(
        BiFunction<RpcController, RequestT, RpcFuture<ResponseT>> apiMethod,
        Optional<RequestT> expectedRequestOptional,
        final Function<RequestT, ResponseT> delegateMethod) {
      when(apiMethod.apply(nullable(RpcController.class), any())).thenAnswer(invocation -> {
        RequestT request = invocation.getArgument(1);
        if (withRequestValidation) {
          checkState(request != null, "Request is null! If you have already mocked this method,"
              + " you may need to call Mockito.reset() on the mocked class before doing so again.");
          validator.validateAndThrowIfErrorsPresent(request);
        }
        expectedRequestOptional.ifPresent(expectedRequest ->
            assertThat(expectedRequest).isEqualTo(request));

        ResponseT response = delegateMethod.apply(request);
        if (withResponseValidation) {
          validator.validateAndThrowIfErrorsPresent(response);
        }
        return RpcFutures.immediateFuture(response);
      });
    }
  }

  /**
   * Mocks a particular method on a mock instance to be delegated to a given method. The delegate's
   * method should have signature of foo(RequestT) -> ResponseT. This performs request and response
   * validation with the {@link RootMessageValidator}, performing common validations like proto
   * validators.
   *
   * N.B. if mocking a method multiple times, Mockito.reset() must be called on the mock prior
   * to subsequent mock operations, or there will be a {@link NullPointerException}.
   *
   * Example usage:
   *
   * {@code RosterService.Api mockApi = Mockito.mock(RosterService.Api.class);
   * RpcServiceMocks.mockMethod(mockApi::lookup, delegate::lookup); }
   *
   * This allows callers to mock up particular methods in an RPC interface, without extending the
   * RPC interface itself or using spys. This allows "smarter" functionality to be delegated to a
   * "delegate" method, which itself could retain state or have fancier logic that would be
   * necessary for testing.
   */
  public static <RequestT extends Message, ResponseT extends Message>
  void mockMethod(
      BiFunction<RpcController, RequestT, RpcFuture<ResponseT>> apiMethod,
      final Function<RequestT, ResponseT> delegateMethod) {
    builder()
        .withRequestValidation(true)
        .withResponseValidation(true)
        .mock(apiMethod, delegateMethod);
  }

  /**
   * Similar to {@link #mockMethod(BiFunction, Function)}, but does not invoke any validation via
   * {@link RootMessageValidator}.
   *
   * Prefer using {@link #mockMethod(BiFunction, Function)} to get more validation.
   *
   * Same as using the {@link Builder} with neither request and response validation performed.
   */
  public static <RequestT extends Message, ResponseT extends Message> void mockMethodWithoutValidation(
      BiFunction<RpcController, RequestT, RpcFuture<ResponseT>> apiMethod,
      final Function<RequestT, ResponseT> delegateMethod) {
    builder()
        .withRequestValidation(false)
        .withResponseValidation(false)
        .mock(apiMethod, delegateMethod);
  }

  /**
   * Mocks a particular method on a mock instance to be delegated to a given method. The delegate's
   * method should have signature of foo(RequestT) -> RpcFuture<ResponseT>.
   *
   * Example usage:
   *
   * {@code RosterService.Api mockApi = Mockito.mock(RosterService.Api.class);
   * RpcServiceMocks.mockMethodWithoutValidation(mockApi::lookup, delegate::lookup); }
   *
   * This allows callers to mock up particular methods in an RPC interface, without extending the
   * RPC interface itself or using spys. This allows "smarter" functionality to be delegated to a
   * "delegate" method, which itself could retain state or have fancier logic that would be
   * necessary for testing.
   */
  public static <RequestT, ResponseT> void mockMethodWithFuture(
      BiFunction<RpcController, RequestT, RpcFuture<ResponseT>> apiMethod,
      final Function<RequestT, RpcFuture<ResponseT>> function) {
    when(apiMethod.apply(nullable(RpcController.class), any())).thenAnswer(invocation -> {
      RequestT request = invocation.getArgument(1);
      return function.apply(request);
    });
  }

  /**
   * Mocks a particular method on a mock instance to throw an RpcException
   * when checkedGet is called on the future
   */
  public static <RequestT, ResponseT> void throwOnFuture(
      BiFunction<RpcController, RequestT, RpcFuture<ResponseT>> apiMethod) {
    throwOnFuture(apiMethod, new RpcException(RpcProtos.ResponseCode.INTERNAL_ERROR));
  }

  public static <RequestT, ResponseT> void throwOnFuture(
      BiFunction<RpcController, RequestT, RpcFuture<ResponseT>> apiMethod, Throwable failure) {
    when(apiMethod.apply(nullable(RpcController.class), any())).thenReturn(
        RpcFutures.immediateFailedFuture(failure));
  }
}
