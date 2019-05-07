# testing/base/src/test/java/BUILD

junit_tests(name='test',
  sources=rglobs('*Test.java'),
  cwd='testing/base',
  dependencies=[
    '3rdparty:com.google.guava.guava',
    '3rdparty:org.apache.logging.log4j.log4j-api',
    '3rdparty:commons-io.commons-io',
    '3rdparty:junit.junit',
    '3rdparty:joda-time.joda-time',
    '3rdparty:com.google.code.findbugs.jsr305',
    'core/common/src/main/java:lib',
    'core/testing/src/main/java:lib',
    'testing-support/src/main/java:lib',
    'testing/base/src/main/java:lib',
  ],
)
