# testing/BUILD

# Default target that is invoked if you do not specify a goal
alias('testing', ':lib')

alias('lib', 'testing/src/main/java:lib')

target(name='test',
  dependencies=[
    'testing/src/test/java:test'
  ],
  tags=[
    'ci'
  ],
)
