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
 * An instance of this class is typically constructed using the DSL when binding a processor function to an [[OptName]]
 * using the [[OptName#~> ~>]] and [[OptName#~>+ ~>+]] methods.
 * 
 * @tparam A the value type
 */
trait Opt[+A] {
  /**
   * Returns the optional ''long'' name.
   * 
   * @return a `Some` containing the long name or `None` if not specified
   */
  def lname: Option[String]

  /**
   * Returns the optional ''short'' name.
   * 
   * @return a `Some` containing the short name or `None` if not specified
   */
  def sname: Option[Char]

  /**
   * Returns the optional default value.
   * 
   * @return a `Some` containing the default value or `None` if not specified
   */
  def default: Option[A]

  /**
   * Returns the option processor.
   * 
   * @return the option processor
   */
  def processor: OptProcessor[A]

  /**
   * Returns the canonical name.
   * 
   * @return the canonical name, which is [[lname]], if defined, otherwise [[sname]] converted to a string
   */
  val name: String = lname.getOrElse(sname.get.toString)

  /**
   * Returns a new option with a default value.
   * 
   * @tparam B the value type
   * @param default the default value
   * @return a new option with `default` as the default value
   */
  def ~~[B >: A](default: B): Opt[B]

  /**
   * Returns a copy of the option value map in which the given value is associated with both [[lname long]] and [[sname short]]
   * names, where applicable.
   * 
   * @tparam B the value type
   * @param value the value
   * @param optv the current option value map
   * @return a new option value map in which `value` is associated with both [[lname long]] and [[sname short]] names
   */
  def set[B >: A](value: B, optv: Map[String, Any]): Map[String, Any]
}

private class ReplacingOpt[+A](
      val lname: Option[String],
      val sname: Option[Char],
      val default: Option[A],
      val processor: OptProcessor[A]) extends Opt[A] {
  def ~~[B >: A](default: B): Opt[B] = new ReplacingOpt(lname, sname, Some(default), processor)

  def set[B >: A](value: B, optv: Map[String, Any]): Map[String, Any] = {
    optv ++ (lname map { n => (n -> value) }) ++ (sname map { n => (n.toString -> value) })
  }
}

private class AppendingOpt[+A](
      val lname: Option[String],
      val sname: Option[Char],
      val default: Option[A],
      val processor: OptProcessor[A]) extends Opt[A] {
  def ~~[B >: A](default: B): Opt[B] = new AppendingOpt(lname, sname, Some(default), processor)

  def set[B >: A](value: B, optv: Map[String, Any]): Map[String, Any] = {
    val vs = (optv.get(name) map { _.asInstanceOf[Seq[B]] } getOrElse Seq.empty[B]) :+ value
    optv ++ (lname map { n => (n -> vs) }) ++ (sname map { n => (n.toString -> vs) })
  }
}

/**
 * Constructs [[Opt]] values.
 */
object Opt {
  /**
   * Returns an empty sequence of options.
   */
  val empty: Seq[Opt[_]] = Seq.empty

  private[scalop] def replacing[A](lname: Option[String], sname: Option[Char], default: Option[A],
        fn: OptProcessor[A]): Opt[A] = {
    new ReplacingOpt(lname, sname, default, fn)
  }

  private[scalop] def appending[A](lname: Option[String], sname: Option[Char], default: Option[A],
        fn: OptProcessor[A]): Opt[A] = {
    new AppendingOpt(lname, sname, default, fn)
  }
}
