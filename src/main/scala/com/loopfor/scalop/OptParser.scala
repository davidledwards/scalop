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

import scala.annotation.tailrec

/**
 * An option parser defined with a sequence of [[Opt option]] definitions.
 */
trait OptParser {
  /**
   * Returns the options associated with this parser.
   * 
   * @return the options associated with this parser
   */
  def opts: Seq[Opt[_]]

  /**
   * Parses the given argument sequence and returns a map of option names to option values.
   * 
   * '''Example'''
   * {{{
   * val opts = ("timeout", 't') ~> as[Int] ~~ 0
   * val optr = opts <~ Seq("--timeout", "10", "foo", "bar")
   * 
   * // fetch value using either long or short form
   * val timeout = optr[Int]("timeout")
   * val timeout = optr[Int]("t")
   * 
   * // get arguments following last option: ("foo", "bar")
   * val rest = optr.args
   * }}}
   * 
   * @param args the argument sequence
   * @return the parse result containing the option value map
   * 
   * @throws OptException if an error occurred during parsing
   */
  def <~(args: Seq[String]): OptResult
}

private class BasicOptParser(val opts: Seq[Opt[_]]) extends OptParser {
  def <~(args: Seq[String]): OptResult = parse(args)

  private def parse(args: Seq[String]): OptResult = {
    val optv = parse(args, Map("@" -> Seq.empty[String]))
    OptResult {
      opts.foldLeft(optv) { case (vs, opt) =>
        vs.get(opt.name) match {
          case None => opt.default.map { opt.set(_, vs) } getOrElse vs
          case _ => vs
        }
      }
    }
  }

  @tailrec private def parse(args: Seq[String], optv: Map[String, Any]): Map[String, Any] = {
    if (args.isEmpty) optv
    else {
      val arg = args.head
      val rest = args.tail

      def process(opt: Opt[_]) = {
        val (next, value) = try opt.processor(rest) catch {
          case e: OptException => throw new OptException(s"$arg: ${e.getMessage}", e.getCause)
          case e: Exception => throw new OptException(s"$arg: error parsing option", e)
        }
        (next, opt.set(value, optv))
      }

      arg match {
        case "--" => optv + ("@" -> rest.toSeq)
        case LongName(name) =>
          opts.find { _.lname.getOrElse(None) == name } match {
            case Some(opt) =>
              val (r, s) = process(opt)
              parse(r, s)
            case None => throw new OptException(s"$arg: no such option")
          }
        case ShortName(name) =>
          opts.find { _.sname.getOrElse(None) == name } match {
            case Some(opt) =>
              val (r, s) = process(opt)
              parse(r, s)
            case None => throw new OptException(s"$arg: no such option")
          }
        case _ => optv + ("@" -> args.toSeq)
      }
    }
  }

  private object LongName {
    def unapply(arg: String): Option[String] = if (dashdash(arg)) Some(arg.drop(2)) else None
  }

  private object ShortName {
    def unapply(arg: String): Option[Char] = if (dash(arg) && arg.length == 2) Some(arg(1)) else None
  }
}

/**
 * Constructs [[OptParser]] values.
 */
object OptParser {
  /**
   * Creates an option parser using the sequence of options.
   * 
   * @param opts a sequence of options
   * @return an option parser
   */
  def apply(opts: Seq[Opt[_]]): OptParser = new BasicOptParser(opts)
}
