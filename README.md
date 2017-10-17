
[comment]: # (Start Badges)

[![Build Status](https://travis-ci.org/frees-io/freestyle-rpc-examples.svg?branch=master)](https://travis-ci.org/frees-io/freestyle-rpc-examples) [![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://raw.githubusercontent.com/frees-io/freestyle-rpc/master/LICENSE) [![Join the chat at https://gitter.im/47deg/freestyle](https://badges.gitter.im/47deg/freestyle.svg)](https://gitter.im/47deg/freestyle?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[comment]: # (End Badges)

# freestyle-rpc-examples

This repo brings a simple example using [freestyle-rpc](https://github.com/frees-io/freestyle-rpc), related to the Route Guide Demo (using scala annotations for service definitions), which is based on the example [grpc-java](https://github.com/grpc/grpc-java/tree/6ea2b8aacb0a193ac727e061bc228b40121460e3/examples/src/main/java/io/grpc/examples/routeguide).

## Running the Example

Run server (interpreted to `scala.concurrent.Future` in this case):

```bash
sbt runServer
```

Run client interpreting to `scala.concurrent.Future`:

```bash
sbt runClientF
```

Run client interpreting to `monix.eval.Task`:

```bash
sbt runClientT
```

## Generating the proto file

```bash
sbt demo-routeguide/protoGen
```

The previous command will overwrite [this proto file](routeguide/src/main/proto/service.proto).

[comment]: # (Start Copyright)
# Copyright

Freestyle is designed and developed by 47 Degrees

Copyright (C) 2017 47 Degrees. <http://47deg.com>

[comment]: # (End Copyright)
