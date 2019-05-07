# testing/base/scenario/src/main/java/BUILD

java_library(name='lib',
  sources=rglobs('*.java'),
  dependencies=[
    '3rdparty:com.google.code.findbugs.jsr305',
    '3rdparty:com.google.guava.guava',
    'core/common/src/main/java:lib',
    'integration/hibernate/src/main/java:lib',
    'integration/hibernate/testing/src/main/java:lib',
  ],
  provides=artifact(
    org='com.squareup',
    name='testing-base-scenario',
    repo=square,
  ), # see squarepants/plugin/repo/register.py
)
