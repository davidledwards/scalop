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

import org.scalatest.funsuite.AnyFunSuite

class OptTest extends AnyFunSuite {
  test("implicit construction of option") {
    val tests = Seq(
          (Some("foo"), Some('f'), "foo"),
          (Some("foo"), None, "foo"),
          (None, Some('f'), "f"))

    for ((lname, sname, name) <- tests) {
      // Construction using ~> operator.
      {
        val opt = (lname, sname) match {
          case (Some(l), Some(s)) => (l, s) ~> just(0)
          case (Some(l), None) => l ~> just(0)
          case (None, Some(s)) => s ~> just(0)
          case _ => fail()
        }
        assert(opt.lname === lname)
        assert(opt.sname === sname)
        assert(opt.name === name)
      }

      // Construction using ~>+ operator.
      {
        val opt = (lname, sname) match {
          case (Some(l), Some(s)) => (l, s) ~>+ just(0)
          case (Some(l), None) => l ~>+ just(0)
          case (None, Some(s)) => s ~>+ just(0)
          case _ => fail()
        }
        assert(opt.lname === lname)
        assert(opt.sname === sname)
      }
    }
  }

  test("explicit construction of option") {
    val tests = Seq(
          (Some("foo"), Some('f'), "foo"),
          (Some("foo"), None, "foo"),
          (None, Some('f'), "f"))

    for ((lname, sname, name) <- tests) {
      // Construction using replacing mode.
      {
        val opt = Opt.replacing(lname, sname, None, just(0))
        assert(opt.lname === lname)
        assert(opt.sname === sname)
        assert(opt.name === name)
      }

      // Construction using appending mode.
      {
        val opt = Opt.appending(lname, sname, None, just(0))
        assert(opt.lname === lname)
        assert(opt.sname === sname)
        assert(opt.name === name)
      }
    }
  }

  test("behavior of `replacing` option") {
    val opt = ("foo", 'f') ~> as[Int]
    val tests = Seq(
          (Seq("--foo", "0"), 0),
          (Seq("-f", "0"), 0),
          (Seq("--foo", "0", "--foo", "1"), 1),
          (Seq("-f", "0", "-f", "1"), 1),
          (Seq("--foo", "0", "-f", "1"), 1),
          (Seq("-f", "0", "--foo", "1"), 1))

    for ((args, v) <- tests) {
      val optv = opt <~ args
      assert(optv[Int]("foo") === v)
      assert(optv[Int]("f") === v)
    }
  }

  test("behavior of `appending` option") {
    val opt = ("foo", 'f') ~>+ as[Int]
    val tests = Seq(
          (Seq("--foo", "0"), Seq(0)),
          (Seq("-f", "0"), Seq(0)),
          (Seq("--foo", "0", "--foo", "1"), Seq(0, 1)),
          (Seq("-f", "0", "-f", "1"), Seq(0, 1)),
          (Seq("--foo", "0", "-f", "1"), Seq(0, 1)),
          (Seq("-f", "0", "--foo", "1"), Seq(0, 1)))

    for ((args, v) <- tests) {
      val optv = opt <~ args
      assert(optv[List[Int]]("foo") === v)
      assert(optv[List[Int]]("f") === v)
    }
  }

  test("attaching default value to option") {
    val a = ("foo", 'f') ~> just(0)
    assert(a.default === None)
    val b = a ~~ 1
    assert(b.default === Some(1))
  }

  test("empty sequence of options") {
    val opts = Opt.empty
    assert(opts.size === 0)
  }
}
