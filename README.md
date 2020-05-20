# Overview

Yet another option parser for Scala, but this one is incredibly simple and rich enough in features to satisfy most needs.

The project page with links to documentation can be found at <https://davidedwards.io/scalop>.

## Usage

### Define

```scala
val opts =
  // no argument, type is Boolean
  // sets value to true if --help or -? detected
  // default value is false
  ("help", '?') ~> just(true) ~~ false ::

  // single argument, type is String
  // no default value
  ("host", 'h') ~> as[String] ::

  // single argument, type is Int
  // default value is 7777
  ("port", 'p') ~> as[Int] ~~ 7777 ::

  // single argument, type is Duration
  // default value is Duration.Inf
  ("timeout", 't') ~> as { arg: Long => Math.max(arg, 0).seconds } ~~ Duration.Inf ::

  // optional single argument, type is Option[Int]
  // value is None if -X not followed by argument
  // otherwise default value is Some(0)
  'X' ~> maybe[Int] ~~ Some(0) ::

  // single argument, type is Seq[File]
  // multiple appearances of --file or -f append argument to sequence
  // default value is empty sequence
  ("file", 'f') ~>+ as[File] ~~ Seq.empty ::
  Nil
```

### Parse

```scala
def main(args: Array[String]): Unit = {
  val optr = try opts <~ args catch {
    case e: OptException => ...
  }
  ...
}
```

### Read

```scala
val help: Boolean = optr("help")
val host: String = optr.get("host") getOrElse "localhost"
val port: Int = optr("port")
val timeout: Duration = optr("timeout")
val x = optr[Option[Int]]("X") getOrElse { Random.nextInt() }
val files: Seq[File] = optr("file")
```

## Documentation

API documentation can be found [here](https://davidedwards.io/scalop/api/2.3/com/loopfor/scalop/index.html).

## Dependency Information

This library is published in the Maven Central Repository. Dependency information can be found [here](https://search.maven.org/artifact/com.loopfor.scalop/scalop_2.13/2.3/jar).

## License

Copyright 2020 David Edwards

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
