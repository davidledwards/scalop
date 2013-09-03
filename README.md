# Scalop
Yet another option parser for Scala, but this one is incredibly simple and rich enough in features to satisfy most needs.

## Build Instructions
In order to build the corresponding artifacts, you must install [Java 1.6](http://www.java.com/en/download/index.jsp) or
higher and [sbt 0.12.4](http://www.scala-sbt.org/0.12.4/docs/Getting-Started/Setup.html).

In the root directory `scalop`, the following command will build the project and install in your local Ivy repository:
```
sbt publish-local
```

API documentation is automatically generated and deployed with `publish-local`, but may also be generated via:
```
sbt doc
```

## Including as Dependency
`scalop` is built against Scala 2.10.2.

### sbt
```
libraryDependencies += "com.loopfor.scalop" %% "scalop" % "1.1"
```

### Maven
```
<dependency>
   <groupId>com.loopfor.scalop</groupId>
   <artifactId>scalop_2.10</artifactId>
   <version>1.1</version>
</dependency>
```

## Overview
An option parser is an ordered collection of option name (``OptName``) and option processor (``OptProcessor``) pairs that
matches on a sequence of arguments, yielding a map of option names to option values. As the parser recognizes option names in
the argument sequence, it applies the corresponding processor function to obtain the option value. This process is repeated
until the argument sequence is exhausted or the parser encounters an error.

An option name can be expressed in both *long* form (a string with at least two characters) and *short* form (a single
character). For any given sequence of arguments, *long* options are detected by the presence of a `--` prefix and *short*
options with a `-` prefix. Examples include `--verbose` and `-v`, respectively.

An option processor is an arbitrary function whose purpose is to return a value that gets assigned to the option name. A
processor may consider and absorb subsequent arguments in computing the value, such as `--timeout` that might expect the next
argument to be an integer.

### Parser Construction
The construction of a parser is best done using the DSL, which is available using the following import statement.
```
import com.loopfor.scalop._
```

The following is a simple parser that demonstrates how the DSL is used to pair an option name with a corresponding processor.
```
val parser = ("verbose", 'v') ~> set(true)
```

The `~>` method implicitly converts the tuple `("verbose", 'v')` into an option, recognizing both long and short forms, and
associates the processor function `set(true)` with that option.

Invoking `parser` with an empty sequence yields the following map of option names to option values. The `@` option is a
special name containing all non-option values trailing the last option in the argument sequence. This is discussed in more
detail below.
```
"@" -> Seq()
```

Invoking `parser` with the sequence `("--verbose")` or `("-v")` produces the following map. Notice that the value is associated
with both ''long'' and ''short'' forms of the option name regardless of which form is specified in the argument sequence.
```
"verbose" -> true
"v" -> true
"@" -> Seq()
```

The following is a more advanced parser that demonstrates additional features of the DSL.
```
val parser =
  ("file", 'f') ~> as { (arg, _) => new File(arg) } ++
  "timeout" ~> asInt ~~ 0 ++
  '?' ~> enable ~~ false
```

The `~~` operator following a processor function associates a default value, which is assigned to the option in the absence
of being specified in the argument sequence.

The `++` operator concatenates options, producing a sequence whose order of traversal also defines the order of evaluation.

Note that, in addition to being defined in both *long* and *short* form, an option may also be specified using only one
form. For example, the expression `"timeout" ~> {...}` creates an option with the *long* name `"timeout"`, and
similarly, the expression `'t' ~> {...}` creates an option with the *short* name `"t"`.

### Parser Behavior
Given a sequence of arguments, such as those provided by a shell when supplying arguments to a command line program, an option
parser behaves in the following manner.

First, default values for each option, if specified, are assigned to the option value map. The parser then recursively applies
the following algorithm as it traverses the argument sequence.
 
If the next argument is equal to `"--"`, the sequence of all subsequent arguments is assigned to the special option name
`"@"` and the parser terminates, returning the option value map. By convention, the `--` option is used to explicitly
terminate options so that remaining arguments, which might be prefixed with `--` or `-`, are not treated as options.

If the next argument is either a *long* or *short* option name recognized by the parser, the corresponding processor
function is applied to the remaining arguments, yielding a value, which is then associated with the option name, both
*long* and *short*, in the option value map. However, if the argument happens to be an option that is not recognized by
the parser, then an `OptException` is thrown. Otherwise, the parser is recursively applied to the remaining sequence of
arguments.

If the next argument is not an option, implying that it contains neither a `--` nor a `-` prefix, then the remaining
argument sequence is assigned to the `"@"` option and the parser terminates.

### Option Processors
An `OptProcessor` is a function accepting as input:
- a sequence of arguments
- an option value map

and returning as output a tuple containing:
- a sequence of arguments
- the option value

The argument sequence provided as input are those that follow the recognized option. For example, given the following
argument sequence:
```
--verbose -F foo.out --timeout 10
```

If `-F` was recognized by the parser, then the sequence provided to the associated processor function would be:
```
foo.out --timeout 10
```

Since a processor may expect additional arguments following the option, as is the case with `-F`, the resulting sequence
should be the arguments that follow those absorbed by the processor, which in this case, would be:
```
--timeout 10
```

In cases where a processor requires additional arguments, it is often necessary to perform some degree of validation or
transformation, both of which may fail. Exceptions that propagate beyond the processor function are caught by the parser
and converted to `OptException`. Additionally, the `yell()` methods are provided as a convenience for processor
implementations to throw instances of `OptException`.

### Predefined Processors
A handful of predefined processors are made available to simplify the construction of options.

For standalone options with no additional arguments, such as `--verbose`, `set` can be used to explicitly assign a value.
```
("verbose", 'v') ~> set(true)
```

Since many standalone options need only convey a boolean value, `enable` and `disable` are shorthand for `set(true)` and
`set(false)`, respectively.

The `as` method constructs a processor in cases where an option requires only a single additional argument, thereby
allowing the signature of the function to be simplified. The simplification comes from removing explicit handling of the
argument sequence.
```
("timeout", 't') ~> as { (arg, opts) => arg.toShort }
```

In addition, most of the primitive types have a prebuilt processor that performs the necessary conversion:
- `asBoolean` / `asSomeBoolean`
- `asByte` / `asSomeByte`
- `asShort` / `asSomeShort`
- `asInt` / `asSomeInt`
- `asLong` / `asSomeLong`
- `asFloat` / `asSomeFloat`
- `asDouble` / `asSomeDouble`
- `asString` / `asSomeString`

## License
Copyright 2013 David Edwards

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
