/*
 * Copyright 2018 status-page contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.timepit.statuspage

import eu.timepit.statuspage.core.Item.{Entry, Group}
import eu.timepit.statuspage.core.Result.{Error, Info, Ok}

import scala.annotation.tailrec

object core {

  final case class Root(items: List[Item], overall: Overall)

  final case class Overall(result: Result)

  sealed trait Item extends Product with Serializable {
    def result: Result
  }

  object Item {
    final case class Group(name: String, items: List[Item], overall: Overall) extends Item {
      override def result: Result = overall.result
    }

    final case class Entry(name: String, result: Result) extends Item
  }

  sealed trait Result extends Product with Serializable
  object Result {
    final case object Ok extends Result
    final case class Info(message: String) extends Result
    final case class Error(maybeMessage: Option[String]) extends Result
  }

  def rootAsPlainText(root: Root): String =
    (overallAsPlainText(root.overall) :: root.items.map(itemAsPlainText)).mkString("\n")

  private def overallAsPlainText(overall: Overall): String =
    s"status: ${resultAsPlainText(overall.result)}"

  private def itemAsPlainText(item: Item): String =
    item match {
      case Group(name, items, overall) =>
        (overallAsPlainText(overall) :: items.map(itemAsPlainText))
          .map(name + "_" + _)
          .mkString("\n")
      case Entry(name, result) => s"$name: ${resultAsPlainText(result)}"
    }

  private def resultAsPlainText(result: Result): String =
    result match {
      case Ok                  => "OK"
      case Info(message)       => message
      case Error(maybeMessage) => "ERROR" + maybeMessage.fold("")(s => s" $s")
    }

  private[statuspage] def overallOf(items: List[Item]): Overall = {
    @tailrec
    def loop(items: List[Item], acc: Result): Result =
      items match {
        case x :: xs =>
          x.result match {
            case Error(_) => Error(None)
            case _        => loop(xs, acc)
          }
        case Nil => acc
      }
    Overall(loop(items, Ok))
  }

}
