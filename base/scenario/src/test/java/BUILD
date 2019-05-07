# testing/base/scenario/src/test/java/BUILD

junit_tests(name='test',
  sources=rglobs('*Test.java'),
  cwd='testing/base/scenario',
  dependencies=[
    '3rdparty:com.google.code.findbugs.jsr305',
    '3rdparty:com.google.guava.guava',
    '3rdparty:junit.junit',
    '3rdparty:org.assertj.assertj-core',
    '3rdparty:org.hamcrest.hamcrest-core',
    '3rdparty:org.hibernate.javax.persistence.hibernate-jpa-2.1-api',
    '3rdparty:org.mockito.mockito-core',
    'common/src/main/java:lib',
    'core/common/src/main/java:lib',
    'integration/hibernate/src/main/java:lib',
    'integration/hibernate/testing/src/main/java:lib',
    'testing-support/src/main/java:lib',
    'testing/base/scenario/src/main/java:lib',
  ],
)
