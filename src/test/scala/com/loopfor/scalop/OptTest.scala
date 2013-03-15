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

class OptTest extends FunSuite {
  test("implicit construction of Opt") {
    val tests = Seq(
          (Some("foo"), Some('f')),
          (Some("foo"), None),
          (None, Some('f')))

    tests foreach { case (lname, sname) =>
      val opt = (lname, sname) match {
        case (Some(l), Some(s)) => (l, s) ~> set(0)
        case (Some(l), None) => l ~> set(0)
        case (None, Some(s)) => s ~> set(0)
        case _ => fail()
      }
      assert(opt.lname === lname)
      assert(opt.sname === sname)
    }
  }

  test("illegal construction of Opt") {
    intercept[IllegalArgumentException] {
      Opt(None, None, None, set(0))
    }
  }

  test("attaching default value to Opt") {
    val a = ("foo", 'f') ~> set(0)
    assert(a.default === None)
    val b = a ~~ 1
    assert(b.default === Some(1))
  }

  test("setting Opt with value") {
    val tests = Seq(
          (Some("foo"), Some('f')),
          (Some("foo"), None),
          (None, Some('f')))

    tests foreach { case (lname, sname) =>
      val opt = (lname, sname) match {
        case (Some(l), Some(s)) => (l, s) ~> set(0)
        case (Some(l), None) => l ~> set(0)
        case (None, Some(s)) => s ~> set(0)
        case _ => fail()
      }

      val r = opt set 1

      (lname, sname) match {
        case (Some(l), Some(s)) =>
          assert(r(l) === 1)
          assert(r(s.toString) === 1)
        case (Some(l), None) =>
          assert(r(l) === 1)
        case (None, Some(s)) =>
          assert(r(s.toString) === 1)
        case (None, None) => fail()
      }
    }
  }
}