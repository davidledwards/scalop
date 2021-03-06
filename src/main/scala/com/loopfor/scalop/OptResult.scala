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

/**
 * The result of parsing an argument sequence.
 */
trait OptResult {
  /**
   * Returns the option value map.
   * 
   * Note that non-option arguments are associated with the option name `"@"`, which is guaranteed to be present in the map
   * even if the argument sequence was empty.
   * 
   * @return the option value map
   */
  def optv: Map[String, Any]

  /**
   * Returns the value of an option.
   * 
   * @tparam A the expected value type
   * @param name the option name
   * @return the value of the option identified by `name`
   * 
   * @throws NoSuchElementException if `name` is not present
   * @throws ClassCastException if the value cannot be cast to `A`
   */
  def apply[A](name: String): A

  /**
   * Optionally returns the value of an option.
   * 
   * @tparam A the expected value type
   * @param name the option name
   * @return a `Some` containing the value of the option if present, otherwise `None`
   * 
   * @throws ClassCastException if the value cannot be cast to `A`
   */
  def get[A](name: String): Option[A]

  /**
   * Returns a sequence of all non-option arguments following the last option.
   * 
   * @return a sequence of all non-option arguments
   */
  def args: Seq[String]
}

private class BasicOptResult(val optv: Map[String, Any]) extends OptResult {
  def apply[A](name: String): A = optv(name).asInstanceOf[A]

  def get[A](name: String): Option[A] = optv.get(name).map { _.asInstanceOf[A] }

  def args: Seq[String] = optv("@").asInstanceOf[Seq[String]]
}

/**
 * Constructs [[OptResult]] values.
 */
object OptResult {
  def apply(optv: Map[String, Any]): OptResult = new BasicOptResult(optv)
}
