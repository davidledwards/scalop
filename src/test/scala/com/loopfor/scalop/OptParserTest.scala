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

import org.scalatest.FunSuite

class OptParserTest extends FunSuite {
  test("explicit construction of OptParser") {
    val opts = Seq(
          ("foo", 'f') ~> set(0),
          "bar" ~> set(1),
          '?' ~> set(2))

    val parser = OptParser(opts)
    parser.opts foreach { opt =>
      val o = opts find { o => o.lname == opt.lname && o.sname == opt.sname }
      assert(o.isDefined)
    }
  }

  test("construction of OptParser by appending") {
    val opts = Seq(
          ("foo", 'f') ~> set(0),
          "bar" ~> set(1),
          '?' ~> set(2))

    val parser = (OptParser(Seq()) /: opts) { case (p, o) => p ++ o }
    parser.opts foreach { opt =>
      val o = opts find { o => o.lname == opt.lname && o.sname == opt.sname }
      assert(o.isDefined)
    }
  }

  test("implicit construction of OptParser") {
    val parser =
      ("foo", 'f') ~> set(0) ++
      "bar" ~> set(1) ++
      '?' ~> set(2)

    assert(parser.opts.size === 3)
    assert((parser.opts find { o => o.lname == Some("foo") && o.sname == Some('f') }).isDefined)
    assert((parser.opts find { _.lname == Some("bar") }).isDefined)
    assert((parser.opts find { _.sname == Some('f') }).isDefined)
  }

  test("parsing recognized options") {
    val opts = Seq(
          (("foo", 'f') ~> set(0), 0),
          ("bar" ~> set(1), 1),
          ('?' ~> set(2), 2))

    val args = Seq(
          (Seq("--foo", "--bar", "-?"), Seq()),
          (Seq("-f", "--bar", "-?"), Seq()),
          (Seq("--foo", "--bar", "-?", "hello"), Seq("hello")),
          (Seq("-f", "--bar", "-?", "there", "world"), Seq("there", "world")))

    val parser = OptParser(opts map { case (o, _) => o })
    args foreach { case (a, etc) =>
      val r = parser parse a
      opts foreach { case (o, v) =>
        (o.lname, o.sname) match {
          case (Some(l), Some(s)) =>
            assert(r(l) === v)
            assert(r(s.toString) === v)
          case (Some(l), None) =>
            assert(r(l) === v)
          case (None, Some(s)) =>
            assert(r(s.toString) === v)
          case (None, None) => fail()
        }
      }
      assert(r("@") === etc)
    }
  }

  test("parsing unrecognized options") {
    val opts = Seq(
          ("foo", 'f') ~> set(0),
          "bar" ~> set(1),
          '?' ~> set(2))

    val args = Seq(
          Seq("--huh", "--foo", "--bar", "-?"),
          Seq("--foo", "--huh", "--bar", "-?"),
          Seq("--foo", "--bar", "--huh", "-?"),
          Seq("--foo", "--bar", "-?", "--huh"))

    val parser = OptParser(opts)
    args foreach { a =>
      intercept[OptException] {
        parser parse a
      }
    }
  }

  test("explicit use of option termination `--` argument") {
    val opts = Seq(
          ("foo", 'f') ~> set(0),
          "bar" ~> set(1),
          '?' ~> set(2))

    val args = Seq(
          (Seq("--foo", "--"), Seq()),
          (Seq("--foo", "--", "--bar"), Seq("--bar")),
          (Seq("--foo", "--", "hello", "world"), Seq("hello", "world")))

    val parser = OptParser(opts)
    args foreach { case (a, etc) =>
      val r = parser parse a
      assert(r("@") === etc)
    }
  }
}
