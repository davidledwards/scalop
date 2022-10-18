/*
 * Copyright 2020 David Edwards
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.loopfor.scalop

import java.io.File
import java.net.{URI, URL}
import java.nio.charset.Charset
import org.scalatest.funsuite.AnyFunSuite
import scala.language.implicitConversions

class OptProcessorTest extends AnyFunSuite {
  test("implicit construction of parser by chaining options and processors") {
    val opts =
      ("verbose", 'v') ~> just(true) ~~ false ::
      ("level", 'l') ~> as[Int] ~~ 0 ::
      ("file", 'f') ~> as[Option[String]] ~~ None :: Nil

    val parser: OptParser = opts
    assert(parser.opts.size === opts.size)
    for ((a, b) <- parser.opts.zip(opts)) {
      assert(a.lname === b.lname)
      assert(a.sname === b.sname)
      assert(a.name === b.name)
      assert(a.default.asInstanceOf[Option[Any]] === b.default)
    }
  }

  test("construction of elemental processor") {
    val opts = "foo" ~> { args =>
      args.headOption match {
        case Some(a) => (args.tail, a.toInt)
        case None => (args, -1)
      }
    } ~~ 0

    val args = Seq(
          (Seq(), 0),
          (Seq("--foo", "1"), 1),
          (Seq("--foo"), -1))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of `just` for constructing processor") {
    val opts = "foo" ~> just(1) ~~ 0

    val args = Seq(
          (Seq(), 0),
          (Seq("--foo"), 1))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of `as` for constructing processor") {
    val opts = "foo" ~> as[Int] ~~ 0

    val args = Seq(
          (Seq(), 0),
          (Seq("--foo", "3"), 3))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of `as` for constructing processor with function") {
    val opts = "foo" ~> as { (arg: Int) => "@" * arg } ~~ ""

    val args = Seq(
          (Seq(), ""),
          (Seq("--foo", "3"), "@@@"))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of `maybe` for constructing processor") {
    val opts = "foo" ~> maybe[Int] ~~ Some(0)

    val args = Seq(
          (Seq(), Some(0)),
          (Seq("--foo"), None),
          (Seq("--foo", "1"), Some(1)))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of `maybe` for constructing processor with function") {
    val opts = "foo" ~> maybe { (arg: Int) => arg.toString } ~~ Some("")

    val args = Seq(
          (Seq(), Some("")),
          (Seq("--foo"), None),
          (Seq("--foo", "1"), Some("1")))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of implicit `Boolean` argument converter") {
    val opts = "foo" ~> as[Boolean] ~~ false

    val args = Seq(
          (Seq(), false),
          (Seq("--foo", "true"), true))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of implicit `Option[Boolean]` argument converter") {
    val opts = "foo" ~> as[Option[Boolean]] ~~ None

    val args = Seq(
          (Seq(), None),
          (Seq("--foo", "true"), Some(true)))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of implicit `Byte` argument converter") {
    val opts = "foo" ~> as[Byte] ~~ 0

    val args = Seq(
          (Seq(), 0),
          (Seq("--foo", "1"), 1))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of implicit `Option[Byte]` argument converter") {
    val opts = "foo" ~> as[Option[Byte]] ~~ None

    val args = Seq(
          (Seq(), None),
          (Seq("--foo", "1"), Some(1)))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of implicit `Short` argument converter") {
    val opts = "foo" ~> as[Short] ~~ 0

    val args = Seq(
          (Seq(), 0),
          (Seq("--foo", "1"), 1))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of implicit `Option[Short]` argument converter") {
    val opts = "foo" ~> as[Option[Short]] ~~ None

    val args = Seq(
          (Seq(), None),
          (Seq("--foo", "1"), Some(1)))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of implicit `Int` argument converter") {
    val opts = "foo" ~> as[Int] ~~ 0

    val args = Seq(
          (Seq(), 0),
          (Seq("--foo", "1"), 1))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of implicit `Option[Int]` argument converter") {
    val opts = "foo" ~> as[Option[Int]] ~~ None

    val args = Seq(
          (Seq(), None),
          (Seq("--foo", "1"), Some(1)))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of implicit `Long` argument converter") {
    val opts = "foo" ~> as[Long] ~~ 0

    val args = Seq(
          (Seq(), 0),
          (Seq("--foo", "1"), 1))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of implicit `Option[Long]` argument converter") {
    val opts = "foo" ~> as[Option[Long]] ~~ None

    val args = Seq(
          (Seq(), None),
          (Seq("--foo", "1"), Some(1)))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of implicit `Float` argument converter") {
    val opts = "foo" ~> as[Float] ~~ 0.0

    val args = Seq(
          (Seq(), 0.0),
          (Seq("--foo", "1.0"), 1.0))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of implicit `Option[Float]` argument converter") {
    val opts = "foo" ~> as[Option[Float]] ~~ None

    val args = Seq(
          (Seq(), None),
          (Seq("--foo", "1.0"), Some(1.0)))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of implicit `Double` argument converter") {
    val opts = "foo" ~> as[Double] ~~ 0.0

    val args = Seq(
          (Seq(), 0.0),
          (Seq("--foo", "1.0"), 1.0))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of implicit `Option[Double]` argument converter") {
    val opts = "foo" ~> as[Option[Double]] ~~ None

    val args = Seq(
          (Seq(), None),
          (Seq("--foo", "1.0"), Some(1.0)))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of implicit `String` argument converter") {
    val opts = "foo" ~> as[String] ~~ ""

    val args = Seq(
          (Seq(), ""),
          (Seq("--foo", "bar"), "bar"))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of implicit `Option[String]` argument converter") {
    val opts = "foo" ~> as[Option[String]] ~~ None

    val args = Seq(
          (Seq(), None),
          (Seq("--foo", "bar"), Some("bar")))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of implicit `Charset` argument converter") {
    val opts = "foo" ~> as[Charset] ~~ Charset.forName("UTF-8")

    val args = Seq(
          (Seq(), Charset forName "UTF-8"),
          (Seq("--foo", "iso-8859-1"), Charset.forName("iso-8859-1")))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of implicit `Option[Charset]` argument converter") {
    val opts = "foo" ~> as[Option[Charset]] ~~ None

    val args = Seq(
          (Seq(), None),
          (Seq("--foo", "iso-8859-1"), Some(Charset.forName("iso-8859-1"))))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of implicit `File` argument converter") {
    val opts = "foo" ~> as[File] ~~ new File("")

    val args = Seq(
          (Seq(), new File("")),
          (Seq("--foo", "foo.bar"), new File("foo.bar")))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of implicit `Option[File]` argument converter") {
    val opts = "foo" ~> as[Option[File]] ~~ None

    val args = Seq(
          (Seq(), None),
          (Seq("--foo", "foo.bar"), Some(new File("foo.bar"))))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of implicit `URI` argument converter") {
    val opts = "foo" ~> as[URI] ~~ URI.create("urn:foo")

    val args = Seq(
          (Seq(), URI.create("urn:foo")),
          (Seq("--foo", "http://foo.bar"), URI.create("http://foo.bar")))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of implicit `Option[URI]` argument converter") {
    val opts = "foo" ~> as[Option[URI]] ~~ None

    val args = Seq(
          (Seq(), None),
          (Seq("--foo", "http://foo.bar"), Some(URI.create("http://foo.bar"))))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of implicit `URL` argument converter") {
    val opts = "foo" ~> as[URL] ~~ new URL("http://foo")

    val args = Seq(
          (Seq(), new URL("http://foo")),
          (Seq("--foo", "http://foo.bar"), new URL("http://foo.bar")))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of implicit `Option[URL]` argument converter") {
    val opts = "foo" ~> as[Option[URL]] ~~ None

    val args = Seq(
          (Seq(), None),
          (Seq("--foo", "http://foo.bar"), Some(new URL("http://foo.bar"))))

    for ((a, v) <- args) {
      val r = opts <~ a
      assert(r.optv("foo") === v)
    }
  }

  test("use of custom argument converter") {
    case class Foo(n: Int)

    implicit def argToFoo(arg: String): Either[String, Foo] = {
      try Right(Foo(arg.toInt)) catch {
        case _: NumberFormatException => Left("error")
      }
    }

    val opts = "foo" ~> as[Foo]

    val r = opts <~ Seq("--foo", "1")
    assert(r.optv("foo") === Foo(1))

    val e = intercept[OptException] {
      opts <~ Seq("--foo", "oops")
    }
    assert(e.getMessage === "--foo: oops: error")
  }
}
