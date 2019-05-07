# testing/acceptance/src/test/java/BUILD

junit_tests(name='test',
  sources=rglobs('*Test.java'),
  cwd='testing/acceptance',
  dependencies=[
    '3rdparty:com.google.code.findbugs.jsr305',
    '3rdparty:com.google.guava.guava',
    '3rdparty:junit.junit',
    '3rdparty:org.apache.logging.log4j.log4j-api',
    '3rdparty:org.apache.logging.log4j.log4j-core',
    '3rdparty:org.assertj.assertj-core',
    '3rdparty:org.mockito.mockito-core',
    'common/config/src/main/java:lib',
    'common/src/main/java:lib',
    'logging/src/main/java:lib',
    'testing-support/src/main/java:lib',
    'testing/acceptance/src/main/java:lib',
    'webservice/client/src/main/java:lib',
  ],
)
