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

/**
 * A simple option parser.
 * 
 * ====Overview====
 * An option parser is an ordered collection of [[OptName option name]] and [[OptProcessor option processor]] pairs that
 * matches on a sequence of arguments, yielding a map of option names to option values. As the parser recognizes option names
 * in the argument sequence, it applies the corresponding processor function to obtain the option value. This process is
 * repeated until the argument sequence is exhausted or the parser encounters an error.
 * 
 * An [[OptName option name]] can be expressed in both ''long'' form (a string with at least two characters) and ''short'' form
 * (a single character). For any given sequence of arguments, ''long'' options are detected by the presence of a `--` prefix
 * and ''short'' options with a `-` prefix. Examples include `--verbose` and `-v`, respectively.
 * 
 * An [[OptProcessor option processor]] is an arbitrary function whose purpose is to return a value that gets assigned to the
 * option name. A processor may consider and absorb subsequent arguments in computing the value, such as `--timeout` that might
 * expect the next argument to be an integer.
 * 
 * ====Parser Construction====
 * The construction of a parser is best done using the DSL, which is available using the following import statement.
 * {{{
 * import com.loopfor.scalop._
 * }}}
 * 
 * The following is a simple parser that demonstrates how the DSL is used to pair an option name with a corresponding
 * processor.
 * {{{
 * val parser = ("verbose", 'v') ~> set(true)
 * }}}
 * 
 * The `~>` method implicitly converts the tuple `("verbose", 'v')` into an option, recognizing both long and short forms, and
 * associates the processor function `set(true)` with that option.
 * 
 * Invoking `parser` with an empty sequence yields the following map of option names to option values. The `@` option is a
 * special name containing all non-option values trailing the last option in the argument sequence. This is discussed in more
 * detail below.
 * {{{
 * "@" -> Seq()
 * }}}
 * 
 * Invoking `parser` with the sequence `("--verbose")` or `("-v")` produces the following map. Notice that the value is
 * associated with both ''long'' and ''short'' forms of the option name regardless of which form is specified in the argument
 * sequence.
 * {{{
 * "verbose" -> true
 * "v" -> true
 * "@" -> Seq()
 * }}}
 * 
 * The following is a more advanced parser that demonstrates additional features of the DSL.
 * <pre>
 * val parser =
 *   ("file", 'f') ~> as { (arg, _) => new File(arg) } ++
 *   "timeout" ~> asInt ~~ 0 ++
 *   '?' ~> enable ~~ false
 * </pre>
 * 
 * The `~~` operator following a processor function associates a default value, which is assigned to the option in the absence
 * of being specified in the argument sequence.
 * 
 * The `++` operator concatenates options, producing a sequence whose order of traversal also defines the order of evaluation.
 * 
 * Note that, in addition to being defined in both ''long'' and ''short'' form, an option may also be specified using only one
 * form. For example, the expression `"timeout" ~> {...}` creates an option with the ''long'' name `"timeout"`, and
 * similarly, the expression `'t' ~> {...}` creates an option with the ''short'' name `"t"`.
 * 
 * ====Parser Behavior====
 * Given a sequence of arguments, such as those provided by a shell when supplying arguments to a command line program, an
 * option parser behaves in the following manner.
 * 
 * First, default values for each option, if specified, are assigned to the option value map. The parser then recursively
 * applies the following algorithm as it traverses the argument sequence.
 * 
 * If the next argument is equal to `"--"`, the sequence of all subsequent arguments is assigned to the special option name
 * `"@"` and the parser terminates, returning the option value map. By convention, the `--` option is used to explicitly
 * terminate options so that remaining arguments, which might be prefixed with `--` or `-`, are not treated as options.
 * 
 * If the next argument is either a ''long'' or ''short'' option name recognized by the parser, the corresponding processor
 * function is applied to the remaining arguments, yielding a value, which is then associated with the option name, both
 * ''long'' and ''short'', in the option value map. However, if the argument happens to be an option that is not recognized by
 * the parser, then an [[OptException exception]] is thrown. Otherwise, the parser is recursively applied to the remaining
 * sequence of arguments.
 * 
 * If the next argument is not an option, implying that it contains neither a `--` nor a `-` prefix, then the remaining
 * argument sequence is assigned to the `"@"` option and the parser terminates.
 * 
 * ====Option Processors====
 * An [[OptProcessor option processor]] is a function accepting as input:
 *  - a sequence of arguments
 *  - an option value map
 * 
 * and returning as output a tuple containing:
 *  - a sequence of arguments
 *  - the option value
 *  
 * The argument sequence provided as input are those that follow the recognized option. For example, given the following
 * argument sequence:
 * <pre>
 * --verbose -F foo.out --timeout 10
 * </pre>
 * 
 * If `-F` was recognized by the parser, then the sequence provided to the associated processor function would be:
 * <pre>
 * foo.out --timeout 10
 * </pre>
 * 
 * Since a processor may expect additional arguments following the option, as is the case with `-F`, the resulting sequence
 * should be the arguments that follow those absorbed by the processor, which in this case, would be:
 * <pre>
 * --timeout 10
 * </pre>
 * 
 * In cases where a processor requires additional arguments, it is often necessary to perform some degree of validation or
 * transformation, both of which may fail. Exceptions that propagate beyond the processor function are caught by the parser
 * and converted to [[OptException]]. Additionally, the `yell()` methods are provided as a convenience for processor
 * implementations to throw instances of [[OptException]].
 * 
 * ====Predefined Processors====
 * A handful of predefined processors are made available to simplify the construction of options.
 * 
 * For standalone options with no additional arguments, such as `--verbose`, [[set]] can be used to explicitly assign a value.
 * {{{
 * ("verbose", 'v') ~> set(true)
 * }}}
 * 
 * Since many standalone options need only convey a boolean value, [[enable]] and [[disable]] are shorthand for `set(true)` and
 * `set(false)`, respectively.
 * 
 * The [[as]] method constructs a processor in cases where an option requires only a single additional argument, thereby
 * allowing the signature of the function to be simplified. The simplification comes from removing explicit handling of the
 * argument sequence.
 * {{{
 * ("timeout", 't') ~> as { (arg, opts) => arg.toShort }
 * }}}
 * 
 * In addition, most of the primitive types have a prebuilt processor that performs the necessary conversion:
 *  - [[asBoolean]] / [[asSomeBoolean]]
 *  - [[asByte]] / [[asSomeByte]]
 *  - [[asShort]] / [[asSomeShort]]
 *  - [[asInt]] / [[asSomeInt]]
 *  - [[asLong]] / [[asSomeLong]]
 *  - [[asFloat]] / [[asSomeFloat]]
 *  - [[asDouble]] / [[asSomeDouble]]
 *  - [[asString]] / [[asSomeString]]
 */
package object scalop {
  type OptProcessor[+A] = (Seq[String], Map[String, Any]) => (Seq[String], A)

  abstract class OptName(lname: Option[String], sname: Option[Char]) {
    def ~>[A](fn: OptProcessor[A]): Opt[A] = Opt(lname, sname, None, fn)
  }

  implicit class LongOptName(lname: String) extends OptName(Some(lname), None)
  implicit class ShortOptName(sname: Char) extends OptName(None, Some(sname))
  implicit class LongShortOptName(name: (String, Char)) extends OptName(Some(name._1), Some(name._2))

  implicit def optToParser(opt: Opt[_]): OptParser = OptParser(Seq(opt))

  /**
   * Returns a [[OptProcessor processor]] whose value does not depend on additional arguments.
   * 
   * '''Example'''
   * {{{
   * val parser = ("verbose", 'v') ~> set(true) ~~ false
   * }}}
   * 
   * @tparam A the option type
   * @param fn a function that returns an instance of `A`
   */
  def set[A](fn: => A): OptProcessor[A] = { (args, results) => (args, fn) }

  /**
   * Return a [[OptProcessor processor]] whose value is `true`.
   * 
   * This is shorthand for `set(true)`.
   */
  def enable: OptProcessor[Boolean] = set(true)

  /**
   * Return a [[OptProcessor processor]] whose value is `false`.
   * 
   * This is shorthand for `set(false)`.
   */
  def disable: OptProcessor[Boolean] = set(false)

  /**
   * Returns a [[OptProcessor processor]] that transforms a single argument.
   * 
   * '''Example'''
   * {{{
   * val parser = ("level", 'L') ~> as { (arg, opts) => arg.toInt } ~~ 0
   * }}}
   * 
   * @tparam A the option type
   * @param fn a function that transforms an argument to an instance of `A`
   */
  def as[A](fn: (String, Map[String, Any]) => A): OptProcessor[A] = { (args, results) =>
    args.headOption match {
      case Some(arg) if !optPrefix(arg) => (args.tail, fn(arg, results))
      case _ => yell("missing argument")
    }
  }

  /**
   * Return a [[OptProcessor processor]] that converts an argument to a `Boolean`.
   * 
   * The resulting processor throws an [[OptException]] if the argument cannot be converted.
   */
  def asBoolean: OptProcessor[Boolean] = as { (arg, _) => toBoolean(arg) }

  /**
   * Return a [[OptProcessor processor]] that converts an argument to a `Some[Boolean]`.
   * 
   * The resulting processor throws an [[OptException]] if the argument cannot be converted.
   */
  def asSomeBoolean: OptProcessor[Option[Boolean]] = as { (arg, _) => Some(toBoolean(arg)) }

  private def toBoolean = { (arg: String) =>
    try arg.toBoolean catch {
      case _: NumberFormatException => yell(s"$arg: must be a boolean")
    }
  }

  /**
   * Return a [[OptProcessor processor]] that converts an argument to a `Byte`.
   * 
   * The resulting processor throws an [[OptException]] if the argument cannot be converted.
   */
  def asByte: OptProcessor[Byte] = as { (arg, _) => toByte(arg) }

  /**
   * Return a [[OptProcessor processor]] that converts an argument to a `Some[Byte]`.
   * 
   * The resulting processor throws an [[OptException]] if the argument cannot be converted.
   */
  def asSomeByte: OptProcessor[Option[Byte]] = as { (arg, _) => Some(toByte(arg)) }

  private def toByte = { (arg: String) =>
    try arg.toByte catch {
      case _: NumberFormatException => yell(s"$arg: must be a byte")
    }
  }

  /**
   * Return a [[OptProcessor processor]] that converts an argument to a `Short`.
   * 
   * The resulting processor throws an [[OptException]] if the argument cannot be converted.
   */
  def asShort: OptProcessor[Short] = as { (arg, _) => toShort(arg) }

  /**
   * Return a [[OptProcessor processor]] that converts an argument to a `Some[Short]`.
   * 
   * The resulting processor throws an [[OptException]] if the argument cannot be converted.
   */
  def asSomeShort: OptProcessor[Option[Short]] = as { (arg, _) => Some(toShort(arg)) }

  private def toShort = { (arg: String) =>
    try arg.toShort catch {
      case _: NumberFormatException => yell(s"$arg: must be a short integer")
    }
  }

  /**
   * Return a [[OptProcessor processor]] that converts an argument to a `Int`.
   * 
   * The resulting processor throws an [[OptException]] if the argument cannot be converted.
   */
  def asInt: OptProcessor[Int] = as { (arg, _) => toInt(arg) }

  /**
   * Return a [[OptProcessor processor]] that converts an argument to a `Some[Int]`.
   * 
   * The resulting processor throws an [[OptException]] if the argument cannot be converted.
   */
  def asSomeInt: OptProcessor[Option[Int]] = as { (arg, _) => Some(toInt(arg)) }

  private def toInt = { (arg: String) =>
    try arg.toInt catch {
      case _: NumberFormatException => yell(s"$arg: must be an integer")
    }
  }

  /**
   * Return a [[OptProcessor processor]] that converts an argument to a `Long`.
   * 
   * The resulting processor throws an [[OptException]] if the argument cannot be converted.
   */
  def asLong: OptProcessor[Long] = as { (arg, _) => toLong(arg) }

  /**
   * Return a [[OptProcessor processor]] that converts an argument to a `Some[Long]`.
   * 
   * The resulting processor throws an [[OptException]] if the argument cannot be converted.
   */
  def asSomeLong: OptProcessor[Option[Long]] = as { (arg, _) => Some(toLong(arg)) }

  private def toLong = { (arg: String) =>
    try arg.toLong catch {
      case _: NumberFormatException => yell(s"$arg: must be a long integer")
    }
  }

  /**
   * Return a [[OptProcessor processor]] that converts an argument to a `Float`.
   * 
   * The resulting processor throws an [[OptException]] if the argument cannot be converted.
   */
  def asFloat: OptProcessor[Float] = as { (arg, _) => toFloat(arg) }

  /**
   * Return a [[OptProcessor processor]] that converts an argument to a `Some[Float]`.
   * 
   * The resulting processor throws an [[OptException]] if the argument cannot be converted.
   */
  def asSomeFloat: OptProcessor[Option[Float]] = as { (arg, _) => Some(toFloat(arg)) }

  private def toFloat = { (arg: String) =>
    try arg.toFloat catch {
      case _: NumberFormatException => yell(s"$arg: must be a single-precision float")
    }
  }

  /**
   * Return a [[OptProcessor processor]] that converts an argument to a `Double`.
   * 
   * The resulting processor throws an [[OptException]] if the argument cannot be converted.
   */
  def asDouble: OptProcessor[Double] = as { (arg, _) => toDouble(arg) }

  /**
   * Return a [[OptProcessor processor]] that converts an argument to a `Some[Double]`.
   * 
   * The resulting processor throws an [[OptException]] if the argument cannot be converted.
   */
  def asSomeDouble: OptProcessor[Option[Double]] = as { (arg, _) => Some(toDouble(arg)) }

  private def toDouble = { (arg: String) =>
    try arg.toDouble catch {
      case _: NumberFormatException => yell(s"$arg: must be a double-precision float")
    }
  }

  /**
   * Return a [[OptProcessor processor]] that simply returns an argument as a `String`.
   */
  def asString: OptProcessor[String] = as { (arg, _) => arg }

  /**
   * Return a [[OptProcessor processor]] that simply returns an argument as a `Some[String]`.
   */
  def asSomeString: OptProcessor[Option[String]] = as { (arg, _) => Some(arg) }

  /**
   * Return a [[OptProcessor processor]] that converts an argument to a `Charset`.
   * 
   * '''Example'''
   * <pre>
   * val parser = ("encoding", 'E') ~> asCharset ~~ Charset.forName("UTF-8")
   * </pre>
   * 
   * The resulting processor throws an [[OptException]] if the argument refers to an unknown character set.
   */
  def asCharset: OptProcessor[Charset] = as { (arg, _) => toCharset(arg) }

  /**
   * Return a [[OptProcessor processor]] that converts an argument to a `Some[Charset]`.
   * 
   * '''Example'''
   * {{{
   * val parser = ("encoding", 'E') ~> asSomeCharset ~~ None
   * }}}
   * 
   * The resulting processor throws an [[OptException]] if the argument refers to an unknown character set.
   */
  def asSomeCharset: OptProcessor[Option[Charset]] = as { (arg, _) => Some(toCharset(arg)) }

  private def toCharset = { (arg: String) =>
    try Charset forName arg catch {
      case e: IllegalArgumentException => throw new OptException(arg + ": no such charset", e)
    }
  }

  /**
   * A convenience method that throws [[OptException]].
   */
  def yell(message: String): Nothing = throw new OptException(message)

  /**
   * A convenience method that throws [[OptException]].
   */
  def yell(message: String, cause: Throwable): Nothing = throw new OptException(message, cause)

  private[scalop] def longPrefix(arg: String) = arg startsWith "--"
  private[scalop] def shortPrefix(arg: String) = arg startsWith "-"
  private[scalop] def optPrefix(arg: String) = longPrefix(arg) || shortPrefix(arg)
}
