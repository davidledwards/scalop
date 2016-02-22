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

/**
 * An option name containing both ''long'' and ''short'' forms.
 * 
 * Option names can be expressed in one of three forms, all of which are represented by this type:
 *  - ''long'' only
 *  - ''short'' only
 *  - both ''long'' and ''short''
 * 
 * An instance of this class is typically constructed using the DSL in which several implicit methods convert various
 * combinations of types to [[OptName]] when the [[~>]] and [[~>+]] methods are used to bind a processor.
 * 
 * The following types are implicitly converted:
 *  - `(String, Char)` -- represents both ''long'' and ''short'' forms
 *  - `String` -- represents ''long'' form only
 *  - `Char` -- represents ''short'' form only
 * 
 * '''Examples'''
 * {{{
 * ("help", '?')
 * "help"
 * '?'
 * }}}
 * 
 * '''Constraints'''
 * 
 * The construction of both ''long'' and ''short'' names is constrained to a subset of printable characters found in the
 * `US-ASCII` character set. A violation of these constraints will cause `IllegalArgumentException` to be thrown by the
 * constructor.
 * 
 * A ''long'' name must adhere to the following regular expression:
 * {{{
 * [a-zA-Z0-9][a-zA-Z0-9-]+
 * }}}
 * 
 * A ''short'' name must adhere to the following regular expression:
 * {{{
 * [a-zA-Z0-9?]
 * }}}
 */
trait OptName {
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
   * Binds a [[OptProcessor processor]] to this option name in ''replacing'' mode.
   * 
   * During argument parsing, the value returned by this processor replaces any value previously assigned to this option.
   * 
   * '''Example'''
   * {{{
   * val opt = ("timeout", 't') ~> as[Int]
   * }}}
   * 
   * @tparam A the return type of the processor
   * @param fn an option processor
   */
  def ~>[A](fn: OptProcessor[A]): Opt[A]

  /**
   * Binds a [[OptProcessor processor]] to this option name in ''appending'' mode.
   * 
   * During argument parsing, the value returned by this processor is appended to a sequence of values previously assigned to
   * this option.
   * 
   * '''Example'''
   * {{{
   * val opt = ("server", 's') ~>+ as[String]
   * }}}
   * 
   * @tparam A the return type of the processor
   * @param fn an option processor
   */
  def ~>+[A](fn: OptProcessor[A]): Opt[A]
}

private class BasicOptName(val lname: Option[String], val sname: Option[Char]) extends OptName {
  def ~>[A](fn: OptProcessor[A]): Opt[A] = Opt.replacing(lname, sname, None, fn)

  def ~>+[A](fn: OptProcessor[A]): Opt[A] = Opt.appending(lname, sname, None, fn)
}

/**
 * Constructs [[OptName]] values.
 */
object OptName {
  /**
   * Creates an option name with ''long'' form only.
   * 
   * @param lname a long name
   * @throws IllegalArgumentException if `lname` does not conform to naming constraints
   */
  def apply(lname: String): OptName = new BasicOptName(verify(lname), None)

  /**
   * Creates an option name with ''short'' form only.
   * 
   * @param sname a short name
   * @throws IllegalArgumentException if `sname` does not conform to naming constraints
   */
  def apply(sname: Char): OptName = new BasicOptName(None, verify(sname))

  /**
   * Creates an option name with both ''long'' and ''short'' forms.
   * 
   * @param lname a long name
   * @param sname a short name
   * @throws IllegalArgumentException if `lname` or `sname` does not conform to naming constraints
   */
  def apply(lname: String, sname: Char): OptName = new BasicOptName(verify(lname), verify(sname))

  private val snamePattern = """[a-zA-Z0-9?]""".r
  private val lnamePattern = """[a-zA-Z0-9][a-zA-Z0-9-]+""".r

  private def verify(lname: String): Option[String] = lname match {
    case lnamePattern(_*) => Some(lname)
    case _ => throw new IllegalArgumentException(s"$lname: long name invalid; must conform to pattern $lnamePattern")
  }

  private def verify(sname: Char): Option[Char] = sname match {
    case snamePattern(_*) => Some(sname)
    case _ => throw new IllegalArgumentException(s"$sname: short name invalid; must conform to pattern $snamePattern")
  }
}
