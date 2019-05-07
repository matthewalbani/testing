# testing/base/BUILD

# Default target that is invoked if you do not specify a goal
alias('base', ':lib')

alias('lib', 'testing/base/src/main/java:lib')

target(name='test',
  dependencies=[
    'testing/base/src/test/java:test'
  ],
  tags=[
    'ci'
  ],
)
