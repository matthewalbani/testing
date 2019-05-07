package com.squareup.testing.idempotence;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class IdempotentMethodInterceptor implements MethodInterceptor {
  @Override public Object invoke(MethodInvocation methodInvocation) throws Throwable {
    methodInvocation.proceed();
    return methodInvocation.proceed();
  }
}
