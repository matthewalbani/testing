# testing/sharding/BUILD

# Default target that is invoked if you do not specify a goal
alias('sharding', ':lib')

alias('lib', 'testing/sharding/src/main/java:lib')

target(name='test',
  dependencies=[
    'testing/sharding/src/test/java:test'
  ],
  tags=[
    'ci'
  ],
)
