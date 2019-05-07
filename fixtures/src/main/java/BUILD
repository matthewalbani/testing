# testing/fixtures/src/main/java/BUILD

java_library(name='lib',
  sources=rglobs('*.java'),
  dependencies=[
    '3rdparty:com.google.code.findbugs.jsr305',
    '3rdparty:com.google.guava.guava',
    '3rdparty:com.google.protobuf.protobuf-java',
    '3rdparty:junit.junit',
    '3rdparty:org.apache.commons.commons-lang3',
    '3rdparty:org.jooq.jooq',
    'common/src/main/java:lib',
    'integration/hibernate/src/main/java:lib',
  ],
  provides=artifact(
    org='com.squareup.testing',
    name='fixtures',
    repo=square,
  ), # see squarepants/plugin/repo/register.py
)
