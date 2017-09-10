
[comment]: # (Start Badges)

[![Build Status](https://travis-ci.org/frees-io/freestyle-rpc-examples.svg?branch=master)](https://travis-ci.org/frees-io/freestyle-rpc-examples) [![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://raw.githubusercontent.com/frees-io/freestyle-rpc/master/LICENSE) [![Join the chat at https://gitter.im/47deg/freestyle](https://badges.gitter.im/47deg/freestyle.svg)](https://gitter.im/47deg/freestyle?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[comment]: # (End Badges)

# freestyle-rpc-examples

This repo brings some simple examples using [freestyle-rpc](https://github.com/frees-io/freestyle-rpc).

1. Route Guide Demo (using scala annotations for service definitions).
1. Greeting Demo (using `.proto` files for service definitions).
1. ProtocolGen Demo (using scala annotations for service definitions).
1. User Demo (http extensions on top of gRPC, using grpc-gateway).

## Route Guide Demo

Based on the [grpc-java](https://github.com/grpc/grpc-java/tree/6ea2b8aacb0a193ac727e061bc228b40121460e3/examples/src/main/java/io/grpc/examples/routeguide) example.

Run server:

```
sbt runRouteGuideServer
```

Run client:

```
sbt runRouteGuideClient
```

## Greeting Demo

Simple demo using `.proto` files + [ScalaPB](https://scalapb.github.io/). 

Run server:

```
sbt runGreetingServer
```

Run client:

```
sbt runGreetingClient
```

## ProtocolGen Demo

Same example as above but just using scala annotations to define services and protocols. In this case, to preserve compatibility, the `.proto` file is generated automatically by `freestyle-rpc`.

Run server:

```
sbt runProtoGenServer
```

Run client:

```
sbt runProtoGenClient
```

## Http/User Demo

In this simple case, we're just using the grpc extension to provide http endpoints on top of the gRPC services.

Based on https://github.com/grpc-ecosystem/grpc-gateway.

[grpc-gateway](https://github.com/grpc-ecosystem/grpc-gateway) is a plugin of protoc. It reads gRPC service definition, and generates a reverse-proxy server which translates a RESTful JSON API into gRPC.
This server is generated according to [custom options](https://cloud.google.com/service-management/reference/rpc/google.api#http) in your gRPC definition.

### Prerequisites

It's mandatory to follow these [instructions](https://github.com/grpc-ecosystem/grpc-gateway#installation) before proceeding. You might want use `brew install protobuf` if you're using OSX.

And then:

```bash
$ brew install go
$ go get -u github.com/golang/protobuf/{proto,protoc-gen-go}
$ go get -u github.com/grpc-ecosystem/grpc-gateway/protoc-gen-grpc-gateway
$ go get -u github.com/grpc-ecosystem/grpc-gateway/protoc-gen-swagger
$ go get -u -v google.golang.org/grpc
$ go get -u github.com/golang/protobuf/protoc-gen-go
```

Finally, make sure that your `$GOPATH/bin` is in your `$PATH`.

### Troubleshooting

#### `failed to run aclocal: No such file or directory`

The development release of a program source code often comes with `autogen.sh` which is used to prepare a build process, including verifying program functionality and generating configure script. This `autogen.sh` script then relies on `autoreconf` to invoke `autoconf`, `automake`, `aclocal` and other related tools.

The missing `aclocal` is part of `automake` package. Thus, to fix this error, install `automake` package.

* `OSX`: 

https://gist.github.com/justinbellamy/2672db1c78f024f2d4fe

* `Debian`, `Ubuntu` or `Linux Mint`:

```bash
$ sudo apt-get install automake
```

* `CentOS`, `Fedora` or `RHEL`:

```bash
$ sudo yum install automake
```

### Run Demo

#### Running the Server

```
sbt -Dgo.path=$GOPATH ";project demo-http;runMain freestyle.rpc.demo.user.UserServerApp"
```

#### Running the Client

Now, you could invoke the service:

* Using the client, as usual:

```
sbt -Dgo.path=$GOPATH ";project demo-http;runMain freestyle.rpc.demo.user.UserClientApp"
```

#### Generating and Running the Gateway

You could generate a reverse proxy and writing an endpoint as it's described [here](https://github.com/grpc-ecosystem/grpc-gateway#usage).

To run the gateway:

```bash
go run http/gateway/server/entry.go
```

Then, you could use `curl` or similar to fetch the user over `HTTP`:

```bash
curl -X POST \
     -H "Content-Type: application/json" \
     -H "Cache-Control: no-cache" \
     -H "Postman-Token: 1e813409-6aa6-8cd1-70be-51305f31667f" \
     -d '{
        "password" : "password"
     }' "http://127.0.0.1:8080/v1/frees"
```

HTTP Response:

```bash
{"name":"Freestyle","email":"hello@frees.io"}%
```

[comment]: # (Start Copyright)
# Copyright

Freestyle is designed and developed by 47 Degrees

Copyright (C) 2017 47 Degrees. <http://47deg.com>

[comment]: # (End Copyright)
