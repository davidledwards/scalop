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

import scala.language._

trait Opt[+A] {
  def lname: Option[String]
  def sname: Option[Char]
  def default: Option[A]
  def process(args: Seq[String], results: Map[String, Any]): (Seq[String], A)
  def ~~[B >: A](default: B): Opt[B]
  def set[B >: A](value: B): Map[String, B]
}

object Opt {
  def apply[A](lname: Option[String], sname: Option[Char], default: Option[A], fn: OptProcessor[A]): Opt[A] =
    new Impl(lname, sname, default, fn)

  private class Impl[+A](
        val lname: Option[String],
        val sname: Option[Char],
        val default: Option[A],
        fn: OptProcessor[A]) extends Opt[A] {
    def process(args: Seq[String], results: Map[String, Any]): (Seq[String], A) = fn(args, results)

    def ~~[B >: A](default: B): Opt[B] = new Impl(lname, sname, Some(default), fn)

    def set[B >: A](value: B): Map[String,B] =
      Map[String, B]() ++ (lname map { n => (n -> value) }) ++ (sname map { n => (n.toString -> value) })
  }

}
