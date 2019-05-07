# testing/base/scenario/BUILD

# Default target that is invoked if you do not specify a goal
alias('scenario', ':lib')

alias('lib', 'testing/base/scenario/src/main/java:lib')

target(name='test',
  dependencies=[
    'testing/base/scenario/src/test/java:test'
  ],
  tags=[
    'ci'
  ],
)
