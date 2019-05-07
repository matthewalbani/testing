package com.squareup.testing.errorprone;

import com.google.common.base.Throwables;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.annotation.Nullable;
import org.mockito.Mockito;

/**
 * Tools for ignoring {@link Future}s so errorprone doesn't nag you in tests.
 */
public final class FutureConsumer {
  /**
   * <p>Ignore a {@link Future}, usually because it's a stray from {@link Mockito#verify(Object)} or
   * similar.
   * </p>
   *
   * <p> Say you verify some RPC stub that returns a future
   *   <pre>
   *   verify(someService).call(arg1, arg2);
   *   </pre>
   * ...all is well except that errorprone complains about FutureReturnValueIgnored.  (Interestingly
   * there is an exception for Mockito in the similar ReturnValueIgnored, but there is no such
   * exception for FutureReturnValueIgnored presumably because it'd be a huge pain.)  No problem,
   * just wrap it in ignore().
   * </p>
   */
  public static <T> void ignore(@Nullable Future<T> nullableFuture) {
    Optional.ofNullable(nullableFuture).ifPresent(future -> {
      try {
        future.get();
      } catch (ExecutionException|InterruptedException  e) {
        Throwables.throwIfUnchecked(e);
        throw new RuntimeException(e);
      }
    });
  }

  private FutureConsumer() {
    throw new IllegalStateException("no");
  }
}
