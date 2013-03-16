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
package com.loopfor

import java.nio.charset.Charset
import scala.language._

package object scalop {
  type OptProcessor[+A] = (Seq[String], Map[String, Any]) => (Seq[String], A)

  abstract class OptName(lname: Option[String], sname: Option[Char]) {
    def ~>[A](fn: OptProcessor[A]): Opt[A] = Opt(lname, sname, None, fn)
  }

  implicit class LongOptName(lname: String) extends OptName(Some(lname), None)
  implicit class ShortOptName(sname: Char) extends OptName(None, Some(sname))
  implicit class LongShortOptName(name: (String, Char)) extends OptName(Some(name._1), Some(name._2))

  implicit def optToParser(opt: Opt[_]): OptParser = OptParser(Seq(opt))

  def set[A](fn: => A): OptProcessor[A] = { (args, results) => (args, fn) }

  val enable: OptProcessor[Boolean] = set(true)
  val disable: OptProcessor[Boolean] = set(false)

  def as[A](implicit fn: (String, Map[String, Any]) => A): OptProcessor[A] = { (args, results) =>
    args.headOption match {
      case Some(arg) if !optPrefix(arg) => (args.tail, fn(arg, results))
      case _ => throw new OptException("missing argument")
    }
  }

  val asShort: OptProcessor[Short] = as { (arg, _) => toShort(arg) }
  val asShortOption: OptProcessor[Option[Short]] = as { (arg, _) => Some(toShort(arg)) }

  private def toShort = { (arg: String) =>
    try arg.toShort catch {
      case _: NumberFormatException => yell(s"$arg: must be a short integer")
    }
  }

  val asInt: OptProcessor[Int] = as { (arg, _) => toInt(arg) }
  val asIntOption: OptProcessor[Option[Int]] = as { (arg, _) => Some(toInt(arg)) }

  private def toInt = { (arg: String) =>
    try arg.toInt catch {
      case _: NumberFormatException => yell(s"$arg: must be an integer")
    }
  }

  val asLong: OptProcessor[Long] = as { (arg, _) => toLong(arg) }
  val asLongOption: OptProcessor[Option[Long]] = as { (arg, _) => Some(toLong(arg)) }

  private def toLong = { (arg: String) =>
    try arg.toLong catch {
      case _: NumberFormatException => yell(s"$arg: must be a long integer")
    }
  }

  val asFloat: OptProcessor[Float] = as { (arg, _) => toFloat(arg) }
  val asFloatOption: OptProcessor[Option[Float]] = as { (arg, _) => Some(toFloat(arg)) }

  private def toFloat = { (arg: String) =>
    try arg.toFloat catch {
      case _: NumberFormatException => yell(s"$arg: must be a single-precision float")
    }
  }

  val asDouble: OptProcessor[Double] = as { (arg, _) => toDouble(arg) }
  val asDoubleOption: OptProcessor[Option[Double]] = as { (arg, _) => Some(toDouble(arg)) }

  private def toDouble = { (arg: String) =>
    try arg.toDouble catch {
      case _: NumberFormatException => yell(s"$arg: must be a double-precision float")
    }
  }

  val asString: OptProcessor[String] = as { (arg, _) => arg }
  val asStringOption: OptProcessor[Option[String]] = as { (arg, _) => Some(arg) }

  val asCharset: OptProcessor[Charset] = as { (arg, _) => toCharset(arg) }
  val asCharsetOption: OptProcessor[Option[Charset]] = as { (arg, _) => Some(toCharset(arg)) }

  private def toCharset = { (arg: String) =>
    try Charset forName arg catch {
      case e: IllegalArgumentException => throw new OptException(arg + ": no such charset", e)
    }
  }

  def yell(message: String): Nothing = throw new OptException(message)
  def yell(message: String, cause: Throwable): Nothing = throw new OptException(message, cause)

  private[scalop] def longPrefix(arg: String) = arg startsWith "--"
  private[scalop] def shortPrefix(arg: String) = arg startsWith "-"
  private[scalop] def optPrefix(arg: String) = longPrefix(arg) || shortPrefix(arg)
}
