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

/**
 * An option definition.
 * 
 * @tparam A the value type
 */
trait Opt[+A] {
  /**
   * The optional ''long'' name.
   * 
   * If `None`, then [[sname]] will not be `None`.
   */
  def lname: Option[String]

  /**
   * The optional ''short'' name.
   * 
   * If `None`, then [[lname]] will not be `None`.
   */
  def sname: Option[Char]

  /**
   * An optional default value.
   */
  def default: Option[A]

  /**
   * The option processor.
   */
  def processor: OptProcessor[A]

  /**
   * Returns a new option with a default value.
   * 
   * @param default the default value
   * @return a new option with `default` as the default value
   */
  def ~~[B >: A](default: B): Opt[B]

  /**
   * Returns a map in which the given value is associated with both [[lname long]] and [[sname short]] names, if applicable.
   * 
   * @tparam B the value type
   * @param value the value
   * @return a map in which `value` is associated with both [[lname long]] and [[sname short]] names
   */
  def set[B >: A](value: B): Map[String, B]
}

/**
 * Constructs [[Opt]] values.
 * 
 * In normal circumstances, option definitions are implicitly created using [[scalop DSL syntax]].
 */
object Opt {
  def apply[A](lname: Option[String], sname: Option[Char], default: Option[A], fn: OptProcessor[A]): Opt[A] = {
    if (lname.isDefined || sname.isDefined)
      new Impl(lname, sname, default, fn)
    else
      throw new IllegalArgumentException("(lname, sname): at least one must be defined")
  }

  private class Impl[+A](
        val lname: Option[String],
        val sname: Option[Char],
        val default: Option[A],
        val processor: OptProcessor[A]) extends Opt[A] {
    def ~~[B >: A](default: B): Opt[B] = new Impl(lname, sname, Some(default), processor)

    def set[B >: A](value: B): Map[String,B] =
      Map[String, B]() ++ (lname map { n => (n -> value) }) ++ (sname map { n => (n.toString -> value) })
  }
}
