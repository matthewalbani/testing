# testing/sharding/src/main/java/BUILD

java_library(name='lib',
  sources=rglobs('*.java'),
  dependencies=[
    '3rdparty:com.google.code.findbugs.jsr305',
    '3rdparty:com.google.guava.guava',
    '3rdparty:com.google.inject.guice',
    '3rdparty:io.cucumber.cucumber-junit',
    '3rdparty:junit.junit',
    '3rdparty:org.reflections.reflections',
    '3rdparty:org.seleniumhq.selenium.selenium-java',
    'common/src/main/java:lib',
    'core/common/src/main/java:lib',
    'testing-support/src/main/java:lib',
  ],
  provides=artifact(
    org='com.squareup.testing',
    name='sharding',
    repo=square,
  ), # see squarepants/plugin/repo/register.py
)
