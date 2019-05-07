# testing/testing-wire/src/main/proto/BUILD

sake_wire_library(name='wire_proto',
  sources = rglobs('*.proto'),
  proto_files = [
    'squareup/testing/wire/example.proto',
  ],
  includes = [
    'squareup.testing.wire.Data',
		'squareup.testing.wire.DataWithByteString',
  ],
  dependencies = [
    ':path',
    ':proto-sources',
    '3rdparty:com.squareup.wire.wire-runtime'
  ],
  provides=artifact(org='com.squareup.testing',
                    name='testing-wire',
                    repo=square,),
)

wire_proto_path(name='path',
  sources=rglobs('*.proto'),
)

resources(name='proto-sources',
  sources=rglobs('*.proto'),
)
