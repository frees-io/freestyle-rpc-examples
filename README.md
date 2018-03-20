
[comment]: # (Start Badges)

[![Build Status](https://travis-ci.org/frees-io/freestyle-rpc-examples.svg?branch=master)](https://travis-ci.org/frees-io/freestyle-rpc-examples) [![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://raw.githubusercontent.com/frees-io/freestyle-rpc/master/LICENSE) [![Join the chat at https://gitter.im/47deg/freestyle](https://badges.gitter.im/47deg/freestyle.svg)](https://gitter.im/47deg/freestyle?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[comment]: # (End Badges)

# freestyle-rpc-examples

This repo shows a simple example using [freestyle-rpc](https://github.com/frees-io/freestyle-rpc), based on the Route Guide Demo (using Scala annotations for service definitions),
from [this example in grpc-java](https://github.com/grpc/grpc-java/tree/v1.10.x/examples/src/main/java/io/grpc/examples/routeguide).

## Running the Example

Run server (interpreted to `cats.effect.IO` in this case):

```bash
sbt runServer
```

Run client interpreting to `cats.effect.IO`:

```bash
sbt runClientIO
```

Run client interpreting to `monix.eval.Task`:

```bash
sbt runClientTask
```

## Generating the IDL files

```bash
sbt demo-routeguide/idlGen
```

The previous command will overwrite [this proto file](routeguide/src/main/resources/proto/service.proto).

(It will also generate [this Avro file](routeguide/src/main/resources/avro/service.avpr) which will contain the messages but no RPC services since ours are annotated with `Protobuf`.)

[comment]: # (Start Copyright)
# Copyright

Freestyle is designed and developed by 47 Degrees

Copyright (C) 2017 47 Degrees. <http://47deg.com>

[comment]: # (End Copyright)
