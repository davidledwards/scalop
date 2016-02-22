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

class OptResultTest extends FunSuite {
  test("fetching options using apply()") {
    val opts =
      "foo" ~> just(0) ::
      "bar" ~> just("bar") ::
      "yaz" ~> just(Seq(0.1, 2.3, 4.5)) :: Nil

    val r = opts <~ Seq("--foo", "--bar", "--yaz")
    assert(r[Int]("foo") === 0)
    assert(r[String]("bar") === "bar")
    assert(r[Seq[Double]]("yaz") === Seq(0.1, 2.3, 4.5))
  }

  test("fetching non-existent options using apply()") {
    val opts = "foo" ~> just(0)
    val r = opts <~ Seq()
    intercept[NoSuchElementException] {
      r[Int]("foo")
    }
  }

  test("fetching options using get()") {
    val opts =
      "foo" ~> just(0) ::
      "bar" ~> just("bar") ::
      "yaz" ~> just(Seq(0.1, 2.3, 4.5)) :: Nil

    val r = opts <~ Seq("--foo", "--bar", "--yaz")
    assert(r.get[Int]("foo") === Some(0))
    assert(r.get[String]("bar") === Some("bar"))
    assert(r.get[Seq[Double]]("yaz") === Some(Seq(0.1, 2.3, 4.5)))
  }

  test("fetching non-existent options using get()") {
    val opts = "foo" ~> just(0)
    val r = opts <~ Seq()
    assert(r.get[Int]("foo") === None)
  }

  test("non-option arguments") {
    val opts = "foo" ~> just(0)
    val r = opts <~ Seq("--foo", "hello", "there", "world")
    assert(r.args === Seq("hello", "there", "world"))
  }

  test("use of raw option value map") {
    val opts =
      "foo" ~> just(0) ::
      "bar" ~> just("bar") ::
      "yaz" ~> just(Seq(0.1, 2.3, 4.5)) :: Nil

    val r = opts <~ Seq("--foo", "--bar", "--yaz", "hello", "there", "world")
    val optv = r.optv
    assert(optv("foo") === 0)
    assert(optv("bar") === "bar")
    assert(optv("yaz") === Seq(0.1, 2.3, 4.5))
    assert(optv("@") === Seq("hello", "there", "world"))
  }
}
