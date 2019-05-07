# testing/acceptance/BUILD

alias('lib', 'testing/acceptance/src/main/java:lib')

# Maven has never run these tests in CI, ever since the inception of this module. A terse comment
# in the original pom.xml indicates that they are run in a "special spot" at some other time;
# cheister@ clarifies that this was in jenkins here:
#
#   https://jenkins.corp.squareup.com/job/testing-acceptance/
#
# However, that job hasn't been run in a long time (as of today 2015-12-15) so he says it should be
# fine to just not run them at all.
target(name='test',
  dependencies = [
#    'testing/acceptance/src/test/java:test'
  ],
  tags = ['ci'],
)
