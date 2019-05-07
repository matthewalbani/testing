# testing/sharding/src/test/java/BUILD

junit_tests(
  name = 'test',
  sources = rglobs('*Test.java'),
  cwd = 'testing/sharding',
  dependencies = [
    ':lib',
    '3rdparty:com.google.code.findbugs.jsr305',
    '3rdparty:com.google.guava.guava',
    '3rdparty:com.google.inject.guice',
    '3rdparty:io.cucumber.cucumber-core',
    '3rdparty:io.cucumber.cucumber-guice',
    '3rdparty:io.cucumber.cucumber-java',
    '3rdparty:io.cucumber.cucumber-junit',
    '3rdparty:junit.junit',
    '3rdparty:org.assertj.assertj-core',
    '3rdparty:org.seleniumhq.selenium.selenium-java',
    'common/src/main/java:lib',
    'testing-support/src/main/java:lib',
    'testing/sharding/src/main/java:lib',
  ],
  extra_jvm_options = [
    '-Xms512m',
    '-Xmx1500m',
    '-XX:CompileThreshold=1000',
    '-XX:+UseCompressedOops',
  ],
)

java_library(
  name = 'lib',
  sources = rglobs(
    '*.java',
    exclude = [rglobs('*Test.java')],
  ),
  dependencies = [
    '3rdparty:com.google.code.findbugs.jsr305',
    '3rdparty:io.cucumber.cucumber-core',
    '3rdparty:io.cucumber.cucumber-guice',
    '3rdparty:io.cucumber.cucumber-java',
    '3rdparty:io.cucumber.cucumber-junit',
    '3rdparty:junit.junit',
    'testing/sharding/src/main/java:lib',
    'testing/sharding/src/test/resources:resources',
  ],
  provides = artifact(
    name = 'sharding-test',
    org = 'com.squareup.testing',
    repo = square,
  ),  # see squarepants/plugin/repo/register.py
)
