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
  test("explicit construction of option parser") {
    val opts =
      ("foo", 'f') ~> just(0) ::
      "bar" ~> just(1) ::
      '?' ~> just(2) :: Nil

    val parser = OptParser(opts)
    for (opt <- parser.opts) {
      val o = opts find { o => o.lname == opt.lname && o.sname == opt.sname }
      assert(o.isDefined)
    }
  }

  test("implicit construction of option parser") {
    val opts =
      ("foo", 'f') ~> just(0) ::
      "bar" ~> just(1) ::
      '?' ~> just(2) :: Nil

    val parser: OptParser = opts
    for (opt <- parser.opts) {
      val o = opts find { o => o.lname == opt.lname && o.sname == opt.sname }
      assert(o.isDefined)
    }
  }

  test("parsing recognized options") {
    val tests = Seq(
          (("foo", 'f') ~> just(0), 0),
          ("bar" ~> just(1), 1),
          ('?' ~> just(2), 2))

    val args = Seq(
          (Seq("--foo", "--bar", "-?"), Seq()),
          (Seq("-f", "--bar", "-?"), Seq()),
          (Seq("--foo", "--bar", "-?", "hello"), Seq("hello")),
          (Seq("-f", "--bar", "-?", "there", "world"), Seq("there", "world")))

    val opts = tests map { _._1 }
    for ((a, etc) <- args) {
      val r = opts <~ a
      for ((o, v) <- tests) {
        (o.lname, o.sname) match {
          case (Some(l), Some(s)) =>
            assert(r.optv(l) === v)
            assert(r.optv(s.toString) === v)
          case (Some(l), None) =>
            assert(r.optv(l) === v)
          case (None, Some(s)) =>
            assert(r.optv(s.toString) === v)
          case (None, None) => fail()
        }
      }
      assert(r.args === etc)
    }
  }

  test("parsing unrecognized options") {
    val opts =
      ("foo", 'f') ~> just(0) ::
      "bar" ~> just(1) ::
      '?' ~> just(2) :: Nil

    val args = Seq(
          Seq("--huh", "--foo", "--bar", "-?"),
          Seq("--foo", "--huh", "--bar", "-?"),
          Seq("--foo", "--bar", "--huh", "-?"),
          Seq("--foo", "--bar", "-?", "--huh"))

    for (a <- args) {
      intercept[OptException] { opts <~ a }
    }
  }

  test("explicit use of option termination `--` argument") {
    val opts =
      ("foo", 'f') ~> just(0) ::
      "bar" ~> just(1) ::
      '?' ~> just(2) :: Nil

    val args = Seq(
          (Seq("--foo", "--"), Seq()),
          (Seq("--foo", "--", "--bar"), Seq("--bar")),
          (Seq("--foo", "--", "hello", "world"), Seq("hello", "world")))

    for ((a, etc) <- args) {
      val r = opts <~ a
      assert(r.args === etc)
    }
  }
}
