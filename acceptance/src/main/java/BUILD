# testing/acceptance/src/main/java/BUILD

java_library(name='lib',
  sources=rglobs('*.java'),
  dependencies=[
    '3rdparty:com.google.code.findbugs.jsr305',
    '3rdparty:com.google.code.gson.gson',
    '3rdparty:com.google.guava.guava',
    '3rdparty:commons-codec.commons-codec',
    '3rdparty:commons-io.commons-io',
    '3rdparty:joda-time.joda-time',
    '3rdparty:junit.junit',
    '3rdparty:org.apache.httpcomponents.httpclient',
    '3rdparty:org.apache.httpcomponents.httpcore',
    '3rdparty:org.bouncycastle.bcprov-jdk15on',
    '3rdparty:org.yaml.snakeyaml',
    'common/locale/src/main/java:lib',
    'common/config/src/main/java:lib',
    'common/src/main/java:lib',
    'logging/src/main/java:lib',
    'webservice/client/src/main/java:lib',
  ],
  provides=artifact(
    org='com.squareup',
    name='testing-acceptance',
    repo=square,
  ), # see squarepants/plugin/repo/register.py
)
