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

import scala.annotation.tailrec

trait OptParser {
  def opts: Seq[Opt[_]]
  def ++(next: Opt[_]): OptParser
  def parse(args: Seq[String]): Map[String, Any]
}

object OptParser {
  def apply(opts: Seq[Opt[_]]): OptParser = new Impl(opts)

  private class Impl(val opts: Seq[Opt[_]]) extends OptParser {
    def ++(next: Opt[_]): OptParser = new Impl(opts :+ next)

    def parse(args: Seq[String]): Map[String, Any] = {
      val results = (Map[String, Any]() /: opts) { case (r, opt) =>
        opt.default map { opt set _ } map { r ++ _ } getOrElse r
      } + ("@" -> Seq[String]())
      parse(args, results)
    }

    @tailrec private def parse(args: Seq[String], results: Map[String, Any]): Map[String, Any] = {
      if (args.isEmpty) results
      else {
        val arg = args.head
        val rest = args.tail

        def process(opt: Opt[_]) = {
          val (next, value) = try opt.process(rest, results) catch {
            case e: OptException => throw new OptException(arg + ": " + e.getMessage, e.getCause)
            case e: Exception => throw new OptException(arg + ": error parsing option", e)
          }
          (next, results ++ (opt set value))
        }

        arg match {
          case "--" => results + ("@" -> rest.toSeq)
          case LongName(name) =>
            opts find { _.lname.getOrElse(None) == name } match {
              case Some(opt) =>
                val (r, s) = process(opt)
                parse(r, s)
              case None => throw new OptException(arg + ": no such option")
            }
          case ShortName(name) =>
            opts find { _.sname.getOrElse(None) == name } match {
              case Some(opt) =>
                val (r, s) = process(opt)
                parse(r, s)
              case None => throw new OptException(arg + ": no such option")
            }
          case _ => results + ("@" -> args.toSeq)
        }
      }
    }
  }

  private object LongName {
    def unapply(arg: String): Option[String] = if (longPrefix(arg)) Some(arg drop 2) else None
  }

  private object ShortName {
    def unapply(arg: String): Option[Char] = if (shortPrefix(arg) && arg.length == 2) Some(arg(1)) else None
  }
}
