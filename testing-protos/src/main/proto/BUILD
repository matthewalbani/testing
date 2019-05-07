# testing/testing-protos/src/main/proto/BUILD

java_protobuf_library(name='proto',
  sources=rglobs('*.proto'),
  dependencies=[
    ':proto-sources',
    '3rdparty:io.grpc.grpc-protobuf',
    '3rdparty:io.grpc.grpc-stub',
    '3rdparty:javax.annotation.javax.annotation-api',
    'shared-protos/json/src/main/proto',
    'shared-protos/protobuf/src/main/java:lib',
    'common-protobuf/src/main/java:lib',
    'shared-protos/common/src/main/proto',
    'multipass/multipass-protos/src/main/proto',
    'sake/sake-protos/src/main/proto',
  ],
  provides=artifact(
    org='com.squareup.testing',
    name='testing-proto',
    repo=square,
  ), # see squarepants/plugin/repo/register.py
)

resources(name='proto-sources',
  sources=rglobs('*.proto'),
)

wire_proto_path(name='path',
  sources=rglobs('*.proto'),
  dependencies=[
    'shared-protos/common/src/main/proto:path',
    'multipass/multipass-protos/src/main/proto:path',
    'sake/sake-protos/src/main/proto:path'
  ],
)
