# testing/web/src/main/java/BUILD

java_library(name='lib',
  sources=rglobs('*.java'),
  dependencies=[
    '3rdparty:com.google.code.findbugs.jsr305',
    '3rdparty:com.google.guava.guava',
    '3rdparty:com.google.inject.guice',
    '3rdparty:javax.inject.javax.inject',
    '3rdparty:org.seleniumhq.selenium.selenium-api',
    '3rdparty:org.seleniumhq.selenium.selenium-chrome-driver',
    '3rdparty:org.seleniumhq.selenium.selenium-firefox-driver',
    '3rdparty:org.seleniumhq.selenium.selenium-remote-driver',
    '3rdparty:org.seleniumhq.selenium.selenium-java',
    '3rdparty:org.seleniumhq.selenium.selenium-support',
    'common/src/main/java:lib',
  ],
  provides=artifact(
    org='com.squareup',
    name='testing-web',
    repo=square,
  ), # see squarepants/plugin/repo/register.py
)
