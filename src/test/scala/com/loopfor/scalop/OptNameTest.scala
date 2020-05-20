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

class OptNameTest extends AnyFunSuite {
  test("valid long option names") {
    val tests = Seq(
          "fo",
          "foo",
          "foo-",
          "foo-bar",
          "f-",
          "FO",
          "FOO",
          "FOO-",
          "FOO-BAR",
          "F-",
          "f7",
          "foo7",
          "foo-7",
          "7f",
          "7foo",
          "7-foo")

    for (s <- tests) {
      val name = OptName(s)
      assert(name.lname === Some(s))
      assert(name.sname === None)
    }
  }

  test("valid short option names") {
    val tests = Seq(
          'f',
          'F',
          '7',
          '?')

    for (c <- tests) {
      val name = OptName(c)
      assert(name.sname === Some(c))
      assert(name.lname === None)
    }
  }

  test("invalid long option names") {
    val tests = Seq(
          "f",
          "*foo",
          "foo_",
          "foo ",
          "-foo",
          "F",
          "*FOO",
          "FOO_",
          "FOO ",
          "-FOO",
          "7")

    for (s <- tests) {
      intercept[IllegalArgumentException] { OptName(s) }
    }
  }

  test("invalid short option names") {
    val tests = Seq(
          '*',
          '_',
          ' ',
          '-')

    for (c <- tests) {
      intercept[IllegalArgumentException] { OptName(c) }
    }
  }
}
