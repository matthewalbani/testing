# testing/base/src/main/java/BUILD

java_library(name='lib',
  sources=rglobs('*.java'),
  dependencies=[
    '3rdparty:com.google.code.findbugs.jsr305',
    '3rdparty:com.google.guava.guava',
    '3rdparty:commons-io.commons-io',
    '3rdparty:joda-time.joda-time',
    '3rdparty:junit.junit',
    'core/common/src/main/java:lib',
    'core/testing/src/main/java:lib',
    'logging/testing/src/main/java:lib',
  ],
  provides=artifact(
    org='com.squareup',
    name='testing-base',
    repo=square,
  ), # see squarepants/plugin/repo/register.py
)
