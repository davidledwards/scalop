# Overview
Yet another option parser for Scala, but this one is incredibly simple and rich enough in features to satisfy most needs.

### Define
```scala
val opts = ("help", '?') ~> just(true) ~~ false ::
           ("host", 'h') ~> as[String] ::
           ("port", 'p') ~> as[Int] ~~ 7777 ::
           ("timeout", 't') ~> as { arg: Long => Math.max(arg, 0).seconds } ~~ Duration.Inf ::
           'X' ~> maybe[Int] ~~ Some(0) ::
           'Y' ~> maybe[Int] ~~ Some(0) ::
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
val help = optr[Boolean]("help")

val host = optr.get[String]("host") getOrElse "localhost"
val port = optr[Int]("port")

val timeout: Duration = optr("timeout")

val x = optr[Option[Int]]("X") getOrElse { Random.nextInt() }
val y = optr[Option[Int]]("Y") getOrElse { Random.nextInt() }
```

## Documentation
API documentation can be found [here](http://www.loopfor.com/scalop/api/2.0/index.html).

## Dependency Information
This library is published in the Maven Central Repository. Dependency information can be found [here](http://search.maven.org/#artifactdetails%7Ccom.loopfor.scalop%7Cscalop_2.11%7C2.0%7Cjar).

## License
Copyright 2013 David Edwards

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
