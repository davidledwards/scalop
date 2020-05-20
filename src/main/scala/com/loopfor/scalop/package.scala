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
package com.loopfor

import java.io.File
import java.net.{URI, URL, MalformedURLException, URISyntaxException}
import java.nio.charset.Charset
import scala.annotation.tailrec
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
 * An [[OptName option name]] can be expressed in both ''long'' and ''short'' form. For any given sequence of arguments,
 * ''long'' options are detected by the presence of a `--` prefix and ''short'' options with a `-` prefix. Examples include
 * `--verbose` and `-v`, respectively.
 * 
 * An [[OptProcessor option processor]] is an arbitrary function whose purpose is to return a value that gets assigned to the
 * option name. A processor may consider and absorb subsequent arguments in computing the value, such as `--timeout` that might
 * expect the next argument to be an integer. Library users are encouraged to utilize the predefined builder methods rather
 * than implement processor functions from scratch.
 * 
 * ====Option Construction====
 * The construction of an [[Opt option]] is best done using the DSL, which is available from the following import statement.
 * 
 * {{{
 * import com.loopfor.scalop._
 * }}}
 * 
 * The following is a simple option that demonstrates how the DSL is used to pair an option name with a corresponding
 * processor.
 * 
 * {{{
 * val opt = ("verbose", 'v') ~> just(true)
 * }}}
 * 
 * The [[OptName#~> ~>]] method triggers an implicit conversion of the tuple `("verbose", 'v')` into an option name and then
 * binds the processor function `just(true)`, resulting in an option.
 * 
 * An option can also be adorned with a default value upon construction using the [[Opt#~~ ~~]] operator following the
 * processor function. Doing so ensures that the option is assigned the default value in the absence of being specified in an
 * argument sequence at the time of parsing.
 * 
 * {{{
 * val opt = ("verbose", 'v') ~> just(true) ~~ false
 * }}}
 * 
 * A sequence of options can then be constructed using the `::` operator. Note that the order of options is important since the
 * parser will evaluate arguments against those options based on traversal order of the sequence.
 * 
 * {{{
 * val opts = ("verbose", 'v') ~> just(true) ~~ false ::
 *            ("timeout", 't') ~> as[Int] ~~ 0 ::
 *            "encoding" ~> as[Charset] ~~ Charset.forName("UTF-8") ::
 *            '?' ~> just(true) ~~ false ::
 *            Nil
 * }}}
 * 
 * =====Replacing Options=====
 * A ''replacing'' option is one which replaces values previously assigned to an option name during argument parsing. Options
 * of this type are created using the [[OptName#~> ~>]] operator.
 * 
 * For example:
 * 
 * {{{
 * val opt = "timeout" ~> as[Int]
 * }}}
 * 
 * Given the following sequence of arguments, the value of `timeout` at the completion of parsing would be `60`.
 * 
 * {{{
 * --timeout 30 --timeout 60
 * }}}
 * 
 * =====Appending Options=====
 * An ''appending'' option is one which appends values to a sequence of values previously assigned to an option name during
 * argument parsing. Options of this type are created using the [[OptName#~>+ ~>+]] operator.
 * 
 * For example:
 * 
 * {{{
 * val opt = "server" ~>+ as[String]
 * }}}
 * 
 * Given the following sequence of arguments, the value of `server` at the completion of parsing would be
 * `Seq("foo.com", "bar.com")`.
 * 
 * {{{
 * --server foo.com --server bar.com
 * }}}
 * 
 * 
 * ====Parser Construction====
 * The construction of an [[OptParser option parser]] happens implicitly when the [[OptParser#<~ <~]] operator is applied to
 * either a single option or a sequence of options.
 * 
 * The following illustrates the evaluation of arguments against a sequence of options. Note that an ephemeral instance of
 * an option parser is constructed as a byproduct of applying the `<~` operator.
 * 
 * {{{
 * val opts = ("verbose", 'v') ~> just(true) ~~ false ::
 *            ("timeout", 't') ~> as[Int] ~~ 0 ::
 *            "encoding" ~> as[Charset] ~~ Charset.forName("UTF-8") ::
 *            '?' ~> just(true) ~~ false ::
 *            Nil
 * 
 * val optr = opts <~ Seq("--verbose", "-t", "30", "--encoding", "iso-8859-1", "this", "and", "that")
 * }}}
 * 
 * In the aforementioned example, invoking the parser with the given sequence of arguments produces an
 * [[OptResult option result]] containing a map of option names to option values. Notice that a value is associated with both
 * ''long'' and ''short'' forms of the option name regardless of which form is specified in the argument sequence.
 * 
 * {{{
 * "verbose" -> true
 * "v" -> true
 * "timeout" -> 30
 * "t" -> 30
 * "encoding" -> Charset(ISO-8859-1)
 * "?" -> false
 * "@" -> Seq("this", "and", "that")
 * }}}
 * 
 * The `@` option is a special name containing all non-option values trailing the last option in the argument sequence.
 * 
 * ====Parser Behavior====
 * Given a sequence of arguments, such as those provided by a shell when supplying arguments to a command line program, an
 * option parser behaves in the following manner.
 * 
 * The parser recursively applies the following algorithm as it traverses the argument sequence.
 * 
 *  - If the next argument is equal to `"--"`, the sequence of all subsequent arguments is assigned to the special option name
 *  `"@"` and the parser terminates, returning the option value map. By convention, the `--` option is used to explicitly
 *  terminate options so that remaining arguments, which might be prefixed with `--` or `-`, are not treated as options.
 * 
 *  - If the next argument is either a ''long'' or ''short'' option name recognized by the parser, the corresponding processor
 *  function is applied to the remaining arguments, yielding a value, which is then associated with the option name, both
 *  ''long'' and ''short'', in the option value map. However, if the argument happens to be an option that is not recognized by
 *  the parser, then an [[OptException exception]] is thrown. Otherwise, the parser is recursively applied to the remaining
 *  sequence of arguments.
 * 
 *  - If the next argument is not an option, implying that it contains neither a `--` nor a `-` prefix, then the remaining
 *  argument sequence is assigned to the `"@"` option and the parser terminates.
 * 
 * Finally, default values for each option are assigned to the option value map if values for those corresponding options are
 * absent.
 * 
 * In cases where values, which follow an option, contain either a `-` or `--` prefix, such values must be escaped by
 * prepending the `\` character. Otherwise, the parser will interpret the value as an option and proceed accordingly.
 *
 * The following example will parse incorrectly since `-10` will be interpreted as an option.
 *
 * {{{
 * --time-adjust -10
 * }}}
 *
 * The correct way to specify this value is as follows:
 *
 * {{{
 * --time-adjust \-10
 * }}}
 *
 * ====Option Processors====
 * An [[OptProcessor option processor]] is a function accepting as input:
 *  - the sequence of arguments that follow an option name
 * 
 * and returning as output a tuple containing:
 *  - the sequence of arguments following those consumed by the processor
 *  - the option value
 *  
 * The argument sequence provided as input are those that follow the recognized option. For example, given the following
 * argument sequence:
 * 
 * <pre>
 * --verbose -F foo.out --timeout 10
 * </pre>
 * 
 * Assuming `-F` was recognized by the parser, then the sequence provided to the associated processor function would be:
 * 
 * {{{
 * Seq("foo.out", "--timeout", "10")
 * }}}
 * 
 * Since a processor may expect additional arguments following the option, as is the case with `-F`, the resulting sequence
 * will be the arguments that follow those absorbed by the processor, which in this case, would be:
 * 
 * {{{
 * Seq("--timeout", "10")
 * }}}
 * 
 * In cases where a processor requires additional arguments, it is often necessary to perform some degree of validation or
 * transformation, both of which may fail. Exceptions that propagate beyond the processor function are caught by the parser
 * and converted to [[OptException]]. Additionally, the `yell()` methods are provided as a convenience for processor
 * implementations to throw instances of [[OptException]].
 * 
 * ====Processor Construction====
 * A processor is typically constructed using one of the predefined builders, the choice of which depends on the nature of the
 * option.
 * 
 * For standalone options with no additional argument, the [[just]] builder can be used to explicitly assign a value.
 * 
 * {{{
 * ("verbose", 'v') ~> just(true)
 * }}}
 * 
 * For options that contain one additional argument, the [[[com.loopfor.scalop.package.as[A,B]* as]]] builders simplify
 * construction of the processor by standardizing the manner in which error cases are handled, such as missing arguments and
 * problems encountered with argument converters.
 * 
 * The simplest form of using [[com.loopfor.scalop.package.as[A]* as]] follows, which essentially converts the argument into
 * the parameterized type.
 * 
 * {{{
 * ("timeout", 't') ~> as[Int]
 * }}}
 * 
 * As with all uses of `as`, an [[ArgConverter argument converter]] must be implicitly defined or supplied directly to the
 * function. Argument converters are discussed in more detail below.
 * 
 * A secondary form of using [[[com.loopfor.scalop.package.as[A,B]* as]]] accepts a user-supplied function, which performs
 * additional processing on the argument once converted. The result of that function becomes the value of the option.
 * 
 * The following example illustrates how a function can be used for validation or alteration.
 * 
 * {{{
 * ("timeout", 't') ~> as { arg: Int => if (arg < 0) 0 else arg }
 * }}}
 * 
 * The type of the return value does not need to match the type of the converted argument, which is demonstrated below.
 * 
 * {{{
 * ("timeout", 't') ~> as { arg: Int => (if (arg < 0) 0 else arg).seconds }
 * }}}
 * 
 * For options that ''might'' contain an additional argument, the [[[com.loopfor.scalop.package.maybe[A,B]* maybe]]] builders
 * are similar to their `as` counterparts in that the handling of error cases is standardized. All processors constructed in
 * this manner yield `Option[A]` rather than `A`. This is necessary to distinguish between the presence or absence of a
 * subsequent argument.
 * 
 * The simplest form of using [[[com.loopfor.scalop.package.maybe[A]* maybe]]] is demonstrated below.
 * 
 * {{{
 * val opts = "help" ~> maybe[String]
 * }}}
 * 
 * The value of the option will vary depending on the arguments provided to the parser:
 * 
 * {{{
 * optr = opts <~ Seq()
 * optr("help") // undefined
 * 
 * optr = opts <~ Seq("--help")
 * optr("help") == None
 * 
 * optr = opts <~ Seq("--help", "foo")
 * optr("help") == Some("foo")
 * }}}
 * 
 * A secondary form of using [[[com.loopfor.scalop.package.maybe[A,B]* maybe]]] accepts a user-supplied function, but is
 * invoked only if a subsequent argument is present.
 * 
 * {{{
 * "help" ~> maybe { arg: String => if (canHelp(arg)) arg else "*" }
 * }}}
 * 
 * ====Argument Converters====
 * An [[ArgConverter argument converter]] is a function that transforms individual argument strings into other types. A
 * converter accepts as input:
 *  - an argument string
 * 
 * and returns an `Either[String, A]` where:
 *  - `Right(A)` conveys successful conversion to an instance of type `A`
 *  - `Left(String)` conveys failure with corresponding user-readable text
 * 
 * A handful of implicit argument converters are provided in the `com.loopfor.scalop` package as a convenience for
 * constructing the most common option types. In the absence of a suitable converter, a custom implementation can be written
 * quite easily. The following example converts an argument string into its equivalent JDK logging level.
 * 
 * {{{
 * import java.util.logging.Level
 * 
 * implicit def argToLevel(arg: String): Either[String, Level] = {
 *   try Right(Level.parse(arg.toUpperCase)) catch {
 *     case _: IllegalArgumentException => Left("unrecognized level")
 *   }
 * }
 * }}}
 * 
 * Providing this converter simplifies option construction as follows.
 * 
 * {{{
 * val opts = ("level", 'L') ~> as[Level] ~~ Level.OFF
 * }}}
 */
package object scalop {
  /**
   * A function that serves as a processor for an option name.
   */
  type OptProcessor[+A] = Seq[String] => (Seq[String], A)

  /**
   * A function that converts string arguments into another type.
   * 
   * The use of `Either` as the return type allows the conversion function to alternatively report errors in the form of
   * user-readable text.
   * 
   * If the conversion is successful, return `Right(A)`, otherwise return `Left(String)` containing the error text.
   */
  type ArgConverter[A] = String => Either[String, A]

  /*
   * Implicits that lift various simple types into an option name.
   */
  implicit def stringToOptName(lname: String) = OptName(lname)
  implicit def charToOptName(sname: Char) = OptName(sname)
  implicit def tupleToOptName(name: (String, Char)) = OptName(name._1, name._2)

  /*
   * Implicits that lift options into an option parser.
   */
  implicit def optToOptParser(opt: Opt[_]) = OptParser(Seq(opt))
  implicit def optsToOptParser(opts: Seq[Opt[_]]) = OptParser(opts)

  /**
   * Returns a [[OptProcessor processor]] whose value does not depend on additional arguments.
   * 
   * '''Example'''
   * {{{
   * val opts = ("verbose", 'v') ~> set(true) ~~ false
   * }}}
   * 
   * @tparam A The option type.
   * @param fn A function that returns an instance of `A`.
   */
  def just[A](fn: => A): OptProcessor[A] = { (_, fn) }

  /**
   * Returns a [[OptProcessor processor]] that transforms a single argument.
   * 
   * '''Example'''
   * {{{
   * val opts = ("timeout", 't') ~> as { arg: Int => arg.seconds } ~~ 60.seconds
   * }}}
   * 
   * @tparam A The type of the argument.
   * @tparam B The value type of the processor.
   * @param fn A function that transforms an argument of type `A` to an instance of type `B`.
   * @param converter An implicit function that converts an argument to type `A`.
   */
  def as[A, B](fn: A => B)(implicit converter: ArgConverter[A]): OptProcessor[B] = {
    args => args.headOption match {
      case Some(arg) if !dashes(arg) =>
        val _arg = unescape(arg)
        converter(_arg) match {
          case Right(a) => (args.tail, fn(a))
          case Left(msg) => yell(s"${_arg}: $msg")
        }
      case _ => yell("missing argument")
    }
  }

  /**
   * Returns a [[OptProcessor processor]] that transforms a single argument.
   * 
   * '''Example'''
   * {{{
   * val opts = ("level", 'L') ~> as[Int] ~~ 0
   * }}}
   * 
   * @tparam A The value type of the processor.
   * @param converter An implicit function that converts an argument to type `A`.
   */
  def as[A](implicit converter: ArgConverter[A]): OptProcessor[A] = as { arg: A => arg }

  /**
   * Returns a [[OptProcessor processor]] that transforms an optional argument.
   * 
   * '''Example'''
   * {{{
   * val opts = ("help", '?') ~> maybe { arg: String => if (canHelp(arg)) arg else "*" }
   * }}}
   * 
   * @tparam A The type of the argument.
   * @tparam B The value type of the processor.
   * @param fn A function that transforms an argument of type `A` to an instance of type `B`.
   * @param converter An implicit function that converts an argument to type `A`.
   */
  def maybe[A, B](fn: A => B)(implicit converter: ArgConverter[A]): OptProcessor[Option[B]] = {
    args => args.headOption match {
      case Some(arg) =>
        if (dashes(arg)) (args, None)
        else {
          val _arg = unescape(arg)
          converter(_arg) match {
            case Right(a) => (args.tail, Some(fn(a)))
            case Left(msg) => yell(s"${_arg}: $msg")
          }
        }
      case _ => (args, None)
    }
  }

  /**
   * Returns a [[OptProcessor processor]] that transforms an optional argument.
   * 
   * '''Example'''
   * {{{
   * val opts = ("help", '?') ~> maybe[String]
   * }}}
   * 
   * @tparam A The value type of the processor.
   * @param converter An implicit function that converts an argument to type `A`.
   */
  def maybe[A](implicit converter: ArgConverter[A]): OptProcessor[Option[A]] = maybe { arg: A => arg }

  /**
   * A convenience method that throws [[OptException]].
   */
  def yell(message: String): Nothing = throw new OptException(message)

  /**
   * A convenience method that throws [[OptException]].
   */
  def yell(message: String, cause: Throwable): Nothing = throw new OptException(message, cause)

  /*
   * The following implicits represent standard argument converters.
   */
  implicit def argToBoolean(arg: String): Either[String, Boolean] = {
    try Right(arg.toBoolean) catch {
      case _: NumberFormatException => Left("must be a boolean")
    }
  }

  implicit def argToByte(arg: String): Either[String, Byte] = {
    try Right(arg.toByte) catch {
      case _: NumberFormatException => Left("must be a byte")
    }
  }

  implicit def argToShort(arg: String): Either[String, Short] = {
    try Right(arg.toShort) catch {
      case _: NumberFormatException => Left("must be a short integer")
    }
  }

  implicit def argToInt(arg: String): Either[String, Int] = {
    try Right(arg.toInt) catch {
      case _: NumberFormatException => Left("must be an integer")
    }
  }

  implicit def argToLong(arg: String): Either[String, Long] = {
    try Right(arg.toLong) catch {
      case _: NumberFormatException => Left("must be a long integer")
    }
  }

  implicit def argToFloat(arg: String): Either[String, Float] = {
    try Right(arg.toFloat) catch {
      case _: NumberFormatException => Left("must be a single-precision float")
    }
  }

  implicit def argToDouble(arg: String): Either[String, Double] = {
    try Right(arg.toDouble) catch {
      case _: NumberFormatException => Left("must be a double-precision float")
    }
  }

  implicit def argToString(arg: String): Either[String, String] = Right(arg)

  implicit def argToCharset(arg: String): Either[String, Charset] = {
    try Right(Charset.forName(arg)) catch {
      case e: IllegalArgumentException => Left("no such charset")
    }
  }

  implicit def argToFile(arg: String): Either[String, File] = Right(new File(arg))

  implicit def argToURI(arg: String): Either[String, URI] = {
    try Right(new URI(arg)) catch {
      case e: URISyntaxException => Left(e.getMessage)
    }
  }

  implicit def argToURL(arg: String): Either[String, URL] = {
    try Right(new URL(arg)) catch {
      case e: MalformedURLException => Left(e.getMessage)
    }
  }

  implicit val argToBooleanOption = liftToOption(argToBoolean)
  implicit def argToByteOption = liftToOption(argToByte)
  implicit def argToShortOption = liftToOption(argToShort)
  implicit def argToIntOption = liftToOption(argToInt)
  implicit def argToLongOption = liftToOption(argToLong)
  implicit def argToFloatOption = liftToOption(argToFloat)
  implicit def argToDoubleOption = liftToOption(argToDouble)
  implicit def argToStringOption = liftToOption(argToString)
  implicit val argToCharsetOption = liftToOption(argToCharset)
  implicit val argToFileOption = liftToOption(argToFile)
  implicit val argToURIOption = liftToOption(argToURI)
  implicit val argToURLOption = liftToOption(argToURL)

  private def liftToOption[A](fn: ArgConverter[A]): ArgConverter[Option[A]] = {
    arg => fn(arg).map { Some(_) }
  }

  private[scalop] def dashdash(arg: String) = arg.startsWith("--")
  private[scalop] def dash(arg: String) = arg.startsWith("-")
  private[scalop] def dashes(arg: String) = dash(arg) || dashdash(arg)
  private[scalop] def unescape(arg: String) = if (arg.startsWith("\\")) arg.drop(1) else arg
}
