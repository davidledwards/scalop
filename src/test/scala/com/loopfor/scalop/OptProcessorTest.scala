/*
 * Copyright 2013 David Edwards
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

import java.nio.charset.Charset
import org.scalatest.FunSuite

class OptProcessorTest extends FunSuite {
  test("implicit construction of parser by chaining options and processors") {
    val parser =
      ("verbose", 'v') ~> enable ~~ false ++
      ("level", 'l') ~> asInt ~~ 0 ++
      ("file", 'f') ~> asStringOption ~~ None

  }

  test("construction of elemental processor") {
    val parser = "foo" ~> { case (args, results) =>
      args.headOption match {
        case Some(a) => (args.tail, a.toInt)
        case None => (args, -1)
      }
    } ~~ 0

    val args = Seq(
          (Seq(), 0),
          (Seq("--foo", "1"), 1),
          (Seq("--foo"), -1))

    args foreach { case (a, v) =>
      val r = parser parse a
      assert(r("foo") === v)
    }
  }

  test("use of `set` processor") {
    val parser = "foo" ~> set(1) ~~ 0

    val args = Seq(
          (Seq(), 0),
          (Seq("--foo"), 1))

    args foreach { case (a, v) =>
      val r = parser parse a
      assert(r("foo") === v)
    }
  }

  test("use of `enable` processor") {
    val parser = "foo" ~> enable ~~ false

    val args = Seq(
          (Seq(), false),
          (Seq("--foo"), true))

    args foreach { case (a, v) =>
      val r = parser parse a
      assert(r("foo") === v)
    }
  }

  test("use of `disable` processor") {
    val parser = "foo" ~> disable ~~ true

    val args = Seq(
          (Seq(), true),
          (Seq("--foo"), false))

    args foreach { case (a, v) =>
      val r = parser parse a
      assert(r("foo") === v)
    }
  }

  test("use of `as` for constructing simplified processor") {
    val parser = "foo" ~> as { case (arg, results) => arg.toInt } ~~ 0

    val args = Seq(
          (Seq(), 0),
          (Seq("--foo", "1"), 1))

    args foreach { case (a, v) =>
      val r = parser parse a
      assert(r("foo") === v)
    }
  }

  test("use of `asBoolean` processor") {
    val parser = "foo" ~> asBoolean ~~ false

    val args = Seq(
          (Seq(), false),
          (Seq("--foo", "true"), true))

    args foreach { case (a, v) =>
      val r = parser parse a
      assert(r("foo") === v)
    }
  }

  test("use of `asByte` processor") {
    val parser = "foo" ~> asByte ~~ 0

    val args = Seq(
          (Seq(), 0),
          (Seq("--foo", "1"), 1))

    args foreach { case (a, v) =>
      val r = parser parse a
      assert(r("foo") === v)
    }
  }

  test("use of `asShort` processor") {
    val parser = "foo" ~> asShort ~~ 0

    val args = Seq(
          (Seq(), 0),
          (Seq("--foo", "1"), 1))

    args foreach { case (a, v) =>
      val r = parser parse a
      assert(r("foo") === v)
    }
  }

  test("use of `asShortOption` processor") {
    val parser = "foo" ~> asShortOption ~~ None

    val args = Seq(
          (Seq(), None),
          (Seq("--foo", "1"), Some(1)))

    args foreach { case (a, v) =>
      val r = parser parse a
      assert(r("foo") === v)
    }
  }

  test("use of `asInt` processor") {
    val parser = "foo" ~> asInt ~~ 0

    val args = Seq(
          (Seq(), 0),
          (Seq("--foo", "1"), 1))

    args foreach { case (a, v) =>
      val r = parser parse a
      assert(r("foo") === v)
    }
  }

  test("use of `asIntOption` processor") {
    val parser = "foo" ~> asIntOption ~~ None

    val args = Seq(
          (Seq(), None),
          (Seq("--foo", "1"), Some(1)))

    args foreach { case (a, v) =>
      val r = parser parse a
      assert(r("foo") === v)
    }
  }

  test("use of `asLong` processor") {
    val parser = "foo" ~> asLong ~~ 0

    val args = Seq(
          (Seq(), 0),
          (Seq("--foo", "1"), 1))

    args foreach { case (a, v) =>
      val r = parser parse a
      assert(r("foo") === v)
    }
  }

  test("use of `asLongOption` processor") {
    val parser = "foo" ~> asLongOption ~~ None

    val args = Seq(
          (Seq(), None),
          (Seq("--foo", "1"), Some(1)))

    args foreach { case (a, v) =>
      val r = parser parse a
      assert(r("foo") === v)
    }
  }

  test("use of `asFloat` processor") {
    val parser = "foo" ~> asFloat ~~ 0.0

    val args = Seq(
          (Seq(), 0.0),
          (Seq("--foo", "1.0"), 1.0))

    args foreach { case (a, v) =>
      val r = parser parse a
      assert(r("foo") === v)
    }
  }

  test("use of `asFloatOption` processor") {
    val parser = "foo" ~> asFloatOption ~~ None

    val args = Seq(
          (Seq(), None),
          (Seq("--foo", "1.0"), Some(1.0)))

    args foreach { case (a, v) =>
      val r = parser parse a
      assert(r("foo") === v)
    }
  }

  test("use of `asDouble` processor") {
    val parser = "foo" ~> asDouble ~~ 0.0

    val args = Seq(
          (Seq(), 0.0),
          (Seq("--foo", "1.0"), 1.0))

    args foreach { case (a, v) =>
      val r = parser parse a
      assert(r("foo") === v)
    }
  }

  test("use of `asDoubleOption` processor") {
    val parser = "foo" ~> asDoubleOption ~~ None

    val args = Seq(
          (Seq(), None),
          (Seq("--foo", "1.0"), Some(1.0)))

    args foreach { case (a, v) =>
      val r = parser parse a
      assert(r("foo") === v)
    }
  }

  test("use of `asString` processor") {
    val parser = "foo" ~> asString ~~ ""

    val args = Seq(
          (Seq(), ""),
          (Seq("--foo", "bar"), "bar"))

    args foreach { case (a, v) =>
      val r = parser parse a
      assert(r("foo") === v)
    }
  }

  test("use of `asStringOption` processor") {
    val parser = "foo" ~> asStringOption ~~ None

    val args = Seq(
          (Seq(), None),
          (Seq("--foo", "bar"), Some("bar")))

    args foreach { case (a, v) =>
      val r = parser parse a
      assert(r("foo") === v)
    }
  }

  test("use of `asCharset` processor") {
    val parser = "foo" ~> asCharset ~~ Charset.forName("UTF-8")

    val args = Seq(
          (Seq(), Charset forName "UTF-8"),
          (Seq("--foo", "iso-8859-1"), Charset forName "iso-8859-1"))

    args foreach { case (a, v) =>
      val r = parser parse a
      assert(r("foo") === v)
    }
  }

  test("use of `asCharsetOption` processor") {
    val parser = "foo" ~> asCharsetOption ~~ None

    val args = Seq(
          (Seq(), None),
          (Seq("--foo", "iso-8859-1"), Some(Charset forName "iso-8859-1")))

    args foreach { case (a, v) =>
      val r = parser parse a
      assert(r("foo") === v)
    }
  }
}
