/*
* Copyright 2001-2013 Artima, Inc.
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
package org.scalatest

import org.scalactic.{Equality, Every, One, Many}
import org.scalactic.StringNormalizations._
import SharedHelpers._
import FailureMessages.decorateToStringValue
import scala.collection.mutable.LinkedList
import Matchers._

class EveryShouldContainTheSameElementsInOrderAsLogicalOrSpec extends FreeSpec {

  //ADDITIONAL//

  val invertedStringEquality =
    new Equality[String] {
      def areEqual(a: String, b: Any): Boolean = a != b
    }

  val invertedListOfStringEquality =
    new Equality[Every[String]] {
      def areEqual(a: Every[String], b: Any): Boolean = a != b
    }

  private def upperCase(value: Any): Any =
    value match {
      case l: Every[_] => l.map(upperCase(_))
      case s: String => s.toUpperCase
      case c: Char => c.toString.toUpperCase.charAt(0)
      case (s1: String, s2: String) => (s1.toUpperCase, s2.toUpperCase)
      case e: java.util.Map.Entry[_, _] =>
        (e.getKey, e.getValue) match {
          case (k: String, v: String) => Entry(k.toUpperCase, v.toUpperCase)
          case _ => value
        }
      case _ => value
    }

  val upperCaseStringEquality =
    new Equality[String] {
      def areEqual(a: String, b: Any): Boolean = upperCase(a) == upperCase(b)
    }

  val fileName: String = "EveryShouldContainTheSameElementsInOrderAsLogicalOrSpec.scala"

  "an Every" - {

    val fumList: Every[String] = Every("fum", "foe", "fie", "fee")
    val toList: Every[String] = Every("you", "to", "birthday", "happy")

    "when used with (contain theSameElementsInOrderAs xx or contain theSameElementsInOrderAs xx)" - {

      "should do nothing if valid, else throw a TFE with an appropriate error message" in {
        fumList should (contain theSameElementsInOrderAs LinkedList("fum", "foe", "fie", "fee") or contain theSameElementsInOrderAs LinkedList("fum", "foe", "fie", "fee"))
        fumList should (contain theSameElementsInOrderAs LinkedList("fee", "fie", "foe", "fum") or contain theSameElementsInOrderAs LinkedList("fum", "foe", "fie", "fee"))
        fumList should (contain theSameElementsInOrderAs LinkedList("fum", "foe", "fie", "fee") or contain theSameElementsInOrderAs LinkedList("fee", "fie", "foe", "fum"))
        val e1 = intercept[TestFailedException] {
          fumList should (contain theSameElementsInOrderAs LinkedList("fee", "fie", "foe", "fum") or contain theSameElementsInOrderAs LinkedList("happy", "birthday", "to", "you"))
        }
        checkMessageStackDepth(e1, Resources("didNotContainSameElementsInOrder", decorateToStringValue(fumList), decorateToStringValue(LinkedList("fee", "fie", "foe", "fum"))) + ", and " + Resources("didNotContainSameElementsInOrder", decorateToStringValue(fumList), decorateToStringValue(LinkedList("happy", "birthday", "to", "you"))), fileName, thisLineNumber - 2)
      }

      "should use the implicit Equality in scope" in {
        implicit val ise = upperCaseStringEquality
        fumList should (contain theSameElementsInOrderAs LinkedList("FUM", "FOE", "FIE", "FEE") or contain theSameElementsInOrderAs LinkedList("FUM", "FOE", "FIE", "FEE"))
        fumList should (contain theSameElementsInOrderAs LinkedList("FEE", "FIE", "FOE", "FUM") or contain theSameElementsInOrderAs LinkedList("FUM", "FOE", "FIE", "FEE"))
        fumList should (contain theSameElementsInOrderAs LinkedList("FUM", "FOE", "FIE", "FEE") or contain theSameElementsInOrderAs LinkedList("FEE", "FIE", "FOE", "FUM"))
        val e1 = intercept[TestFailedException] {
          fumList should (contain theSameElementsInOrderAs LinkedList("FEE", "FIE", "FOE", "FUM") or (contain theSameElementsInOrderAs LinkedList("FIE", "FEE", "FAM", "FOE")))
        }
        checkMessageStackDepth(e1, Resources("didNotContainSameElementsInOrder", decorateToStringValue(fumList), decorateToStringValue(LinkedList("FEE", "FIE", "FOE", "FUM"))) + ", and " + Resources("didNotContainSameElementsInOrder", decorateToStringValue(fumList), decorateToStringValue(LinkedList("FIE", "FEE", "FAM", "FOE"))), fileName, thisLineNumber - 2)
      }

      "should use an explicitly provided Equality" in {
        (fumList should (contain theSameElementsInOrderAs LinkedList("FUM", "FOE", "FIE", "FEE") or contain theSameElementsInOrderAs LinkedList("FUM", "FOE", "FIE", "FEE"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        (fumList should (contain theSameElementsInOrderAs LinkedList("FEE", "FIE", "FOE", "FUM") or contain theSameElementsInOrderAs LinkedList("FUM", "FOE", "FIE", "FEE"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        (fumList should (contain theSameElementsInOrderAs LinkedList("FUM", "FOE", "FIE", "FEE") or contain theSameElementsInOrderAs LinkedList("FEE", "FIE", "FOE", "FUM"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (contain theSameElementsInOrderAs LinkedList("FEE", "FIE", "FOE", "FUM") or contain theSameElementsInOrderAs LinkedList("FIE", "FEE", "FAM", "FOE"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, Resources("didNotContainSameElementsInOrder", decorateToStringValue(fumList), decorateToStringValue(LinkedList("FEE", "FIE", "FOE", "FUM"))) + ", and " + Resources("didNotContainSameElementsInOrder", decorateToStringValue(fumList), decorateToStringValue(LinkedList("FIE", "FEE", "FAM", "FOE"))), fileName, thisLineNumber - 2)
        (fumList should (contain theSameElementsInOrderAs LinkedList(" FUM ", " FOE ", " FIE ", " FEE ") or contain theSameElementsInOrderAs LinkedList(" FUM ", " FOE ", " FIE ", " FEE "))) (after being lowerCased and trimmed, after being lowerCased and trimmed)
      }
    }

    "when used with (equal xx and contain theSameElementsInOrderAs xx)" - {

      "should do nothing if valid, else throw a TFE with an appropriate error message" in {
        fumList should (equal (fumList) or contain theSameElementsInOrderAs LinkedList("fum", "foe", "fie", "fee"))
        fumList should (equal (toList) or contain theSameElementsInOrderAs LinkedList("fum", "foe", "fie", "fee"))
        fumList should (equal (fumList) or contain theSameElementsInOrderAs LinkedList("fee", "fie", "foe", "fum"))
        val e1 = intercept[TestFailedException] {
          fumList should (equal (toList) or contain theSameElementsInOrderAs LinkedList("fee", "fie", "foe", "fum"))
        }
        checkMessageStackDepth(e1, Resources("didNotEqual", decorateToStringValue(fumList), decorateToStringValue(toList)) + ", and " + Resources("didNotContainSameElementsInOrder", decorateToStringValue(fumList), decorateToStringValue(LinkedList("fee", "fie", "foe", "fum"))), fileName, thisLineNumber - 2)
      }

      "should use the implicit Equality in scope" in {
        implicit val ise = upperCaseStringEquality
        fumList should (equal (fumList) or contain theSameElementsInOrderAs LinkedList("FUM", "FOE", "FIE", "FEE"))
        fumList should (equal (toList) or contain theSameElementsInOrderAs LinkedList("FUM", "FOE", "FIE", "FEE"))
        fumList should (equal (fumList) or contain theSameElementsInOrderAs LinkedList("FEE", "FIE", "FOE", "FUM"))
        val e1 = intercept[TestFailedException] {
          fumList should (equal (toList) or (contain theSameElementsInOrderAs LinkedList("FEE", "FIE", "FOE", "FUM")))
        }
        checkMessageStackDepth(e1, Resources("didNotEqual", decorateToStringValue(fumList), decorateToStringValue(toList)) + ", and " + Resources("didNotContainSameElementsInOrder", decorateToStringValue(fumList), decorateToStringValue(LinkedList("FEE", "FIE", "FOE", "FUM"))), fileName, thisLineNumber - 2)
      }

      "should use an explicitly provided Equality" in {
        (fumList should (equal (toList) or contain theSameElementsInOrderAs LinkedList("FUM", "FOE", "FIE", "FEE"))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        (fumList should (equal (fumList) or contain theSameElementsInOrderAs LinkedList("FUM", "FOE", "FIE", "FEE"))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        (fumList should (equal (toList) or contain theSameElementsInOrderAs (LinkedList("FEE", "FIE", "FOE", "FUM")))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (equal (fumList) or contain theSameElementsInOrderAs (LinkedList("FEE", "FIE", "FOE", "FUM")))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, Resources("didNotEqual", decorateToStringValue(fumList), decorateToStringValue(fumList)) + ", and " + Resources("didNotContainSameElementsInOrder", decorateToStringValue(fumList), decorateToStringValue(LinkedList("FEE", "FIE", "FOE", "FUM"))), fileName, thisLineNumber - 2)
        (fumList should (equal (toList) or contain theSameElementsInOrderAs LinkedList(" FEE ", " FIE ", " FOE ", " FUM "))) (decided by invertedListOfStringEquality, after being lowerCased and trimmed)
      }
    }

    "when used with (be xx and contain theSameElementsInOrderAs xx)" - {

      "should do nothing if valid, else throw a TFE with an appropriate error message" in {
        fumList should (be_== (fumList) or contain theSameElementsInOrderAs LinkedList("fum", "foe", "fie", "fee"))
        fumList should (be_== (toList) or contain theSameElementsInOrderAs LinkedList("fum", "foe", "fie", "fee"))
        fumList should (be_== (fumList) or contain theSameElementsInOrderAs LinkedList("fee", "fie", "foe", "fum"))
        val e1 = intercept[TestFailedException] {
          fumList should (be_== (toList) or contain theSameElementsInOrderAs LinkedList("fee", "fie", "foe", "fum"))
        }
        checkMessageStackDepth(e1, Resources("wasNotEqualTo", decorateToStringValue(fumList), decorateToStringValue(toList)) + ", and " + Resources("didNotContainSameElementsInOrder", decorateToStringValue(fumList), decorateToStringValue(LinkedList("fee", "fie", "foe", "fum"))), fileName, thisLineNumber - 2)
      }

      "should use the implicit Equality in scope" in {
        implicit val ise = upperCaseStringEquality
        fumList should (be_== (fumList) or contain theSameElementsInOrderAs LinkedList("FUM", "FOE", "FIE", "FEE"))
        fumList should (be_== (toList) or contain theSameElementsInOrderAs LinkedList("FUM", "FOE", "FIE", "FEE"))
        fumList should (be_== (fumList) or contain theSameElementsInOrderAs LinkedList("FEE", "FIE", "FOE", "FUM"))
        val e1 = intercept[TestFailedException] {
          fumList should (be_== (toList) or (contain theSameElementsInOrderAs LinkedList("FEE", "FIE", "FOE", "FUM")))
        }
        checkMessageStackDepth(e1, Resources("wasNotEqualTo", decorateToStringValue(fumList), decorateToStringValue(toList)) + ", and " + Resources("didNotContainSameElementsInOrder", decorateToStringValue(fumList), decorateToStringValue(LinkedList("FEE", "FIE", "FOE", "FUM"))), fileName, thisLineNumber - 2)
      }

      "should use an explicitly provided Equality" in {
        (fumList should (be_== (fumList) or contain theSameElementsInOrderAs LinkedList("FUM", "FOE", "FIE", "FEE"))) (decided by upperCaseStringEquality)
        (fumList should (be_== (toList) or contain theSameElementsInOrderAs LinkedList("FUM", "FOE", "FIE", "FEE"))) (decided by upperCaseStringEquality)
        (fumList should (be_== (fumList) or contain theSameElementsInOrderAs LinkedList("FEE", "FIE", "FOE", "FUM"))) (decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (be_== (toList) or contain theSameElementsInOrderAs LinkedList("FEE", "FIE", "FOE", "FUM"))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, Resources("wasNotEqualTo", decorateToStringValue(fumList), decorateToStringValue(toList)) + ", and " + Resources("didNotContainSameElementsInOrder", decorateToStringValue(fumList), decorateToStringValue(LinkedList("FEE", "FIE", "FOE", "FUM"))), fileName, thisLineNumber - 2)
        (fumList should (be_== (fumList) or contain theSameElementsInOrderAs LinkedList(" FUM ", " FOE ", " FIE ", " FEE "))) (after being lowerCased and trimmed)
      }
    }

    "when used with (contain theSameElementsInOrderAs xx and be xx)" - {

      "should do nothing if valid, else throw a TFE with an appropriate error message" in {
        fumList should (contain theSameElementsInOrderAs LinkedList("fum", "foe", "fie", "fee") or be_== (fumList))
        fumList should (contain theSameElementsInOrderAs LinkedList("fee", "fie", "foe", "fum") or be_== (fumList))
        fumList should (contain theSameElementsInOrderAs LinkedList("fum", "foe", "fie", "fee") or be_== (toList))
        val e1 = intercept[TestFailedException] {
          fumList should (contain theSameElementsInOrderAs LinkedList("fee", "fie", "foe", "fum") or be_== (toList))
        }
        checkMessageStackDepth(e1, Resources("didNotContainSameElementsInOrder", decorateToStringValue(fumList), decorateToStringValue(LinkedList("fee", "fie", "foe", "fum"))) + ", and " + Resources("wasNotEqualTo", decorateToStringValue(fumList), decorateToStringValue(toList)), fileName, thisLineNumber - 2)
      }

      "should use the implicit Equality in scope" in {
        implicit val ise = upperCaseStringEquality
        fumList should (contain theSameElementsInOrderAs LinkedList("FUM", "FOE", "FIE", "FEE") or be_== (fumList))
        fumList should (contain theSameElementsInOrderAs LinkedList("FEE", "FIE", "FOE", "FUM") or be_== (fumList))
        fumList should (contain theSameElementsInOrderAs LinkedList("FUM", "FOE", "FIE", "FEE") or be_== (toList))
        val e1 = intercept[TestFailedException] {
          fumList should (contain theSameElementsInOrderAs LinkedList("FEE", "FIE", "FOE", "FUM") or be_== (toList))
        }
        checkMessageStackDepth(e1, Resources("didNotContainSameElementsInOrder", decorateToStringValue(fumList), decorateToStringValue(LinkedList("FEE", "FIE", "FOE", "FUM"))) + ", and " + Resources("wasNotEqualTo", decorateToStringValue(fumList), decorateToStringValue(toList)), fileName, thisLineNumber - 2)
      }

      "should use an explicitly provided Equality" in {
        (fumList should (contain theSameElementsInOrderAs LinkedList("FUM", "FOE", "FIE", "FEE") or be_== (fumList))) (decided by upperCaseStringEquality)
        (fumList should (contain theSameElementsInOrderAs LinkedList("FEE", "FIE", "FOE", "FUM") or be_== (fumList))) (decided by upperCaseStringEquality)
        (fumList should (contain theSameElementsInOrderAs LinkedList("FUM", "FOE", "FIE", "FEE") or be_== (toList))) (decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (contain theSameElementsInOrderAs LinkedList("FEE", "FIE", "FOE", "FUM") or be_== (toList))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, Resources("didNotContainSameElementsInOrder", decorateToStringValue(fumList), decorateToStringValue(LinkedList("FEE", "FIE", "FOE", "FUM"))) + ", and " + Resources("wasNotEqualTo", decorateToStringValue(fumList), decorateToStringValue(toList)), fileName, thisLineNumber - 2)
        (fumList should (contain theSameElementsInOrderAs LinkedList(" FUM ", " FOE ", " FIE ", " FEE ") or be_== (fumList))) (after being lowerCased and trimmed)
      }
    }

    "when used with (not contain theSameElementsInOrderAs xx and not contain theSameElementsInOrderAs xx)" - {

      "should do nothing if valid, else throw a TFE with an appropriate error message" in {
        fumList should (not contain theSameElementsInOrderAs (LinkedList("fee", "fie", "foe", "fum")) or not contain theSameElementsInOrderAs (LinkedList("fee", "fie", "foe", "fum")))
        fumList should (not contain theSameElementsInOrderAs (LinkedList("fum", "foe", "fie", "fee")) or not contain theSameElementsInOrderAs (LinkedList("fee", "fie", "foe", "fum")))
        fumList should (not contain theSameElementsInOrderAs (LinkedList("fee", "fie", "foe", "fum")) or not contain theSameElementsInOrderAs (LinkedList("fum", "foe", "fie", "fee")))
        val e1 = intercept[TestFailedException] {
          fumList should (not contain theSameElementsInOrderAs (LinkedList("fum", "foe", "fie", "fee")) or not contain theSameElementsInOrderAs (LinkedList("fum", "foe", "fie", "fee")))
        }
        checkMessageStackDepth(e1, Resources("containedSameElementsInOrder", decorateToStringValue(fumList), decorateToStringValue(LinkedList("fum", "foe", "fie", "fee"))) + ", and " + Resources("containedSameElementsInOrder", decorateToStringValue(fumList), decorateToStringValue(LinkedList("fum", "foe", "fie", "fee"))), fileName, thisLineNumber - 2)
      }

      "should use the implicit Equality in scope" in {
        implicit val ise = upperCaseStringEquality
        fumList should (not contain theSameElementsInOrderAs (LinkedList("FEE", "FIE", "FOE", "FUM")) or not contain theSameElementsInOrderAs (LinkedList("FEE", "FIE", "FOE", "FUM")))
        fumList should (not contain theSameElementsInOrderAs (LinkedList("FUM", "FOE", "FIE", "FEE")) or not contain theSameElementsInOrderAs (LinkedList("FEE", "FIE", "FOE", "FUM")))
        fumList should (not contain theSameElementsInOrderAs (LinkedList("FEE", "FIE", "FOE", "FUM")) or not contain theSameElementsInOrderAs (LinkedList("FUM", "FOE", "FIE", "FEE")))
        val e1 = intercept[TestFailedException] {
          fumList should (not contain theSameElementsInOrderAs (LinkedList("FUM", "FOE", "FIE", "FEE")) or not contain theSameElementsInOrderAs (LinkedList("FUM", "FOE", "FIE", "FEE")))
        }
        checkMessageStackDepth(e1, Resources("containedSameElementsInOrder", decorateToStringValue(fumList), decorateToStringValue(LinkedList("FUM", "FOE", "FIE", "FEE"))) + ", and " + Resources("containedSameElementsInOrder", decorateToStringValue(fumList), decorateToStringValue(LinkedList("FUM", "FOE", "FIE", "FEE"))), fileName, thisLineNumber - 2)
      }

      "should use an explicitly provided Equality" in {
        (fumList should (not contain theSameElementsInOrderAs (LinkedList("FEE", "FIE", "FOE", "FUM")) or not contain theSameElementsInOrderAs (LinkedList("FEE", "FIE", "FOE", "FUM")))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        (fumList should (not contain theSameElementsInOrderAs (LinkedList("FUM", "FOE", "FIE", "FEE")) or not contain theSameElementsInOrderAs (LinkedList("FEE", "FIE", "FOE", "FUM")))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        (fumList should (not contain theSameElementsInOrderAs (LinkedList("FEE", "FIE", "FOE", "FUM")) or not contain theSameElementsInOrderAs (LinkedList("FUM", "FOE", "FIE", "FEE")))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (not contain theSameElementsInOrderAs (LinkedList("FUM", "FOE", "FIE", "FEE")) or not contain theSameElementsInOrderAs (LinkedList("FUM", "FOE", "FIE", "FEE")))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, Resources("containedSameElementsInOrder", decorateToStringValue(fumList), decorateToStringValue(LinkedList("FUM", "FOE", "FIE", "FEE"))) + ", and " + Resources("containedSameElementsInOrder", decorateToStringValue(fumList), decorateToStringValue(LinkedList("FUM", "FOE", "FIE", "FEE"))), fileName, thisLineNumber - 2)
      }
    }

    "when used with (not equal xx and not contain theSameElementsInOrderAs xx)" - {

      "should do nothing if valid, else throw a TFE with an appropriate error message" in {
        fumList should (not equal (toList) or not contain theSameElementsInOrderAs (LinkedList("fee", "fie", "foe", "fum")))
        fumList should (not equal (fumList) or not contain theSameElementsInOrderAs (LinkedList("fee", "fie", "foe", "fum")))
        fumList should (not equal (toList) or not contain theSameElementsInOrderAs (LinkedList("fum", "foe", "fie", "fee")))
        val e1 = intercept[TestFailedException] {
          fumList should (not equal (fumList) or not contain theSameElementsInOrderAs (LinkedList("fum", "foe", "fie", "fee")))
        }
        checkMessageStackDepth(e1, Resources("equaled", decorateToStringValue(fumList), decorateToStringValue(fumList)) + ", and " + Resources("containedSameElementsInOrder", decorateToStringValue(fumList), decorateToStringValue(LinkedList("fum", "foe", "fie", "fee"))), fileName, thisLineNumber - 2)
      }

      "should use the implicit Equality in scope" in {
        implicit val ise = upperCaseStringEquality
        fumList should (not equal (toList) or not contain theSameElementsInOrderAs (LinkedList("FEE", "FIE", "FOE", "FUM")))
        fumList should (not equal (fumList) or not contain theSameElementsInOrderAs (LinkedList("FEE", "FIE", "FOE", "FUM")))
        fumList should (not equal (toList) or not contain theSameElementsInOrderAs (LinkedList("FUM", "FOE", "FIE", "FEE")))
        val e2 = intercept[TestFailedException] {
          fumList should (not equal (fumList) or (not contain theSameElementsInOrderAs (LinkedList("FUM", "FOE", "FIE", "FEE"))))
        }
        checkMessageStackDepth(e2, Resources("equaled", decorateToStringValue(fumList), decorateToStringValue(fumList)) + ", and " + Resources("containedSameElementsInOrder", decorateToStringValue(fumList), decorateToStringValue(LinkedList("FUM", "FOE", "FIE", "FEE"))), fileName, thisLineNumber - 2)
      }

      "should use an explicitly provided Equality" in {
        (fumList should (not equal (fumList) or not contain theSameElementsInOrderAs (LinkedList("FEE", "FIE", "FOE", "FUM")))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        (fumList should (not equal (toList) or not contain theSameElementsInOrderAs (LinkedList("FEE", "FIE", "FOE", "FUM")))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        (fumList should (not equal (fumList) or not contain theSameElementsInOrderAs (LinkedList("FUM", "FOE", "FIE", "FEE")))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (not equal (toList) or not contain theSameElementsInOrderAs (LinkedList("FUM", "FOE", "FIE", "FEE")))) (decided by invertedListOfStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, Resources("equaled", decorateToStringValue(fumList), decorateToStringValue(toList)) + ", and " + Resources("containedSameElementsInOrder", decorateToStringValue(fumList), decorateToStringValue(LinkedList("FUM", "FOE", "FIE", "FEE"))), fileName, thisLineNumber - 2)
      }
    }

    "when used with (not be xx and not contain theSameElementsInOrderAs xx)" - {

      "should do nothing if valid, else throw a TFE with an appropriate error message" in {
        fumList should (not be_== (toList) or not contain theSameElementsInOrderAs (LinkedList("fee", "fie", "foe", "fum")))
        fumList should (not be_== (fumList) or not contain theSameElementsInOrderAs (LinkedList("fee", "fie", "foe", "fum")))
        fumList should (not be_== (toList) or not contain theSameElementsInOrderAs (LinkedList("fum", "foe", "fie", "fee")))
        val e1 = intercept[TestFailedException] {
          fumList should (not be_== (fumList) or not contain theSameElementsInOrderAs (LinkedList("fum", "foe", "fie", "fee")))
        }
        checkMessageStackDepth(e1, Resources("wasEqualTo", decorateToStringValue(fumList), decorateToStringValue(fumList)) + ", and " + Resources("containedSameElementsInOrder", decorateToStringValue(fumList), decorateToStringValue(LinkedList("fum", "foe", "fie", "fee"))), fileName, thisLineNumber - 2)
      }

      "should use the implicit Equality in scope" in {
        implicit val ise = upperCaseStringEquality
        fumList should (not be_== (toList) or not contain theSameElementsInOrderAs (LinkedList("FEE", "FIE", "FOE", "FUM")))
        fumList should (not be_== (fumList) or not contain theSameElementsInOrderAs (LinkedList("FEE", "FIE", "FOE", "FUM")))
        fumList should (not be_== (toList) or not contain theSameElementsInOrderAs (LinkedList("FUM", "FOE", "FIE", "FEE")))
        val e1 = intercept[TestFailedException] {
          fumList should (not be_== (fumList) or (not contain theSameElementsInOrderAs (LinkedList("FUM", "FOE", "FIE", "FEE"))))
        }
        checkMessageStackDepth(e1, Resources("wasEqualTo", decorateToStringValue(fumList), decorateToStringValue(fumList)) + ", and " + Resources("containedSameElementsInOrder", decorateToStringValue(fumList), decorateToStringValue(LinkedList("FUM", "FOE", "FIE", "FEE"))), fileName, thisLineNumber - 2)
      }

      "should use an explicitly provided Equality" in {
        (fumList should (not be_== (toList) or not contain theSameElementsInOrderAs (LinkedList("FEE", "FIE", "FOE", "FUM")))) (decided by upperCaseStringEquality)
        (fumList should (not be_== (fumList) or not contain theSameElementsInOrderAs (LinkedList("FEE", "FIE", "FOE", "FUM")))) (decided by upperCaseStringEquality)
        (fumList should (not be_== (toList) or not contain theSameElementsInOrderAs (LinkedList("FUM", "FOE", "FIE", "FEE")))) (decided by upperCaseStringEquality)
        val e1 = intercept[TestFailedException] {
          (fumList should (not be_== (fumList) or not contain theSameElementsInOrderAs (LinkedList("FUM", "FOE", "FIE", "FEE")))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, Resources("wasEqualTo", decorateToStringValue(fumList), decorateToStringValue(fumList)) + ", and " + Resources("containedSameElementsInOrder", decorateToStringValue(fumList), decorateToStringValue(LinkedList("FUM", "FOE", "FIE", "FEE"))), fileName, thisLineNumber - 2)
        (fumList should (not contain theSameElementsInOrderAs (Set(" FEE ", " FIE ", " FOE ", " FUU ")) or not contain theSameElementsInOrderAs (Set(" FEE ", " FIE ", " FOE ", " FUU ")))) (after being lowerCased and trimmed, after being lowerCased and trimmed)
      }
    }

  }

  "every of Everys" - {

    val list1s: Every[Every[Int]] = Every(Every(1, 2, 3), Every(1, 2, 3), Every(1, 2, 3))
    val lists: Every[Every[Int]] = Every(Every(1, 2, 3), Every(1, 2, 3), Every(2, 3, 4))
    val hiLists: Every[Every[String]] = Every(Every("hi", "hello"), Every("hi", "hello"), Every("hi", "hello"))
    val toLists: Every[Every[String]] = Every(Every("you", "to"), Every("you", "to"), Every("you", "to"))

    def allErrMsg(index: Int, message: String, lineNumber: Int, left: Any): String =
      "'all' inspection failed, because: \n" +
        "  at index " + index + ", " + message + " (" + fileName + ":" + (lineNumber) + ") \n" +
        "in " + decorateToStringValue(left)

    "when used with (contain theSameElementsInOrderAs xx and contain theSameElementsInOrderAs xx)" - {

      "should do nothing if valid, else throw a TFE with an appropriate error message" in {
        all (list1s) should (contain theSameElementsInOrderAs LinkedList(1, 2, 3) or contain theSameElementsInOrderAs LinkedList(1, 2, 3))
        all (list1s) should (contain theSameElementsInOrderAs LinkedList(3, 2, 5) or contain theSameElementsInOrderAs LinkedList(1, 2, 3))
        all (list1s) should (contain theSameElementsInOrderAs LinkedList(1, 2, 3) or contain theSameElementsInOrderAs LinkedList(2, 3, 4))

        atLeast (2, lists) should (contain theSameElementsInOrderAs LinkedList(1, 2, 3) or contain theSameElementsInOrderAs LinkedList(1, 2, 3))
        atLeast (2, lists) should (contain theSameElementsInOrderAs LinkedList(3, 6, 5) or contain theSameElementsInOrderAs LinkedList(1, 2, 3))
        atLeast (2, lists) should (contain theSameElementsInOrderAs LinkedList(1, 2, 3) or contain theSameElementsInOrderAs LinkedList(8, 3, 4))

        val e1 = intercept[TestFailedException] {
          all (lists) should (contain theSameElementsInOrderAs LinkedList(1, 2, 3) or contain theSameElementsInOrderAs LinkedList(1, 2, 3))
        }
        checkMessageStackDepth(e1, allErrMsg(2, decorateToStringValue(Many(2, 3, 4)) + " did not contain the same elements in the same (iterated) order as " + decorateToStringValue(LinkedList(1, 2, 3)) + ", and " + decorateToStringValue(Many(2, 3, 4)) + " did not contain the same elements in the same (iterated) order as " + decorateToStringValue(LinkedList(1, 2, 3)), thisLineNumber - 2, lists), fileName, thisLineNumber - 2)
      }

      "should use the implicit Equality in scope" in {
        implicit val ise = upperCaseStringEquality

        all (hiLists) should (contain theSameElementsInOrderAs LinkedList("HI", "HELLO") or contain theSameElementsInOrderAs LinkedList("hi", "hello"))
        all (hiLists) should (contain theSameElementsInOrderAs LinkedList("HELLO", "HO") or contain theSameElementsInOrderAs LinkedList("hi", "hello"))
        all (hiLists) should (contain theSameElementsInOrderAs LinkedList("HI", "HELLO") or contain theSameElementsInOrderAs LinkedList("hello", "ho"))

        val e1 = intercept[TestFailedException] {
          all (hiLists) should (contain theSameElementsInOrderAs LinkedList("HELLO", "HO") or contain theSameElementsInOrderAs LinkedList("hello", "ho"))
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(Many("hi", "hello")) + " did not contain the same elements in the same (iterated) order as " + decorateToStringValue(LinkedList("HELLO", "HO")) + ", and " + decorateToStringValue(Many("hi", "hello")) + " did not contain the same elements in the same (iterated) order as " + decorateToStringValue(LinkedList("hello", "ho")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      "should use an explicitly provided Equality" in {
        (all (hiLists) should (contain theSameElementsInOrderAs LinkedList("HI", "HELLO") or contain theSameElementsInOrderAs LinkedList("hi", "hello"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        (all (hiLists) should (contain theSameElementsInOrderAs LinkedList("HELLO", "HO") or contain theSameElementsInOrderAs LinkedList("hi", "hello"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        (all (hiLists) should (contain theSameElementsInOrderAs LinkedList("HI", "HELLO") or contain theSameElementsInOrderAs LinkedList("hello", "ho"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)

        val e1 = intercept[TestFailedException] {
          (all (hiLists) should (contain theSameElementsInOrderAs LinkedList("HELLO", "HO") or contain theSameElementsInOrderAs LinkedList("hello", "ho"))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(Many("hi", "hello")) + " did not contain the same elements in the same (iterated) order as " + decorateToStringValue(LinkedList("HELLO", "HO")) + ", and " + decorateToStringValue(Many("hi", "hello")) + " did not contain the same elements in the same (iterated) order as " + decorateToStringValue(LinkedList("hello", "ho")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }
    }

    "when used with (be xx and contain theSameElementsInOrderAs xx)" - {

      "should do nothing if valid, else throw a TFE with an appropriate error message" in {
        all (list1s) should (be_== (Many(1, 2, 3)) or contain theSameElementsInOrderAs LinkedList(1, 2, 3))
        all (list1s) should (be_== (Many(2, 3, 4)) or contain theSameElementsInOrderAs LinkedList(1, 2, 3))
        all (list1s) should (be_== (Many(1, 2, 3)) or contain theSameElementsInOrderAs LinkedList(2, 3, 4))

        val e1 = intercept[TestFailedException] {
          all (list1s) should (be_== (Many(2, 3, 4)) or contain theSameElementsInOrderAs LinkedList(2, 3, 4))
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(Many(1, 2, 3)) + " was not equal to " + decorateToStringValue(Many(2, 3, 4)) + ", and " + decorateToStringValue(Many(1, 2, 3)) + " did not contain the same elements in the same (iterated) order as " + decorateToStringValue(LinkedList(2, 3, 4)), thisLineNumber - 2, list1s), fileName, thisLineNumber - 2)
      }

      "should use the implicit Equality in scope" in {
        implicit val ise = upperCaseStringEquality

        all (hiLists) should (be_== (Many("hi", "hello")) or contain theSameElementsInOrderAs LinkedList("HI", "HELLO"))
        all (hiLists) should (be_== (Many("ho", "hello")) or contain theSameElementsInOrderAs LinkedList("HI", "HELLO"))
        all (hiLists) should (be_== (Many("hi", "hello")) or contain theSameElementsInOrderAs LinkedList("HELLO", "HI"))

        val e1 = intercept[TestFailedException] {
          all (hiLists) should (be_== (Many("ho", "hello")) or contain theSameElementsInOrderAs LinkedList("HELLO", "HI"))
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(Many("hi", "hello")) + " was not equal to " + decorateToStringValue(Many("ho", "hello")) + ", and " + decorateToStringValue(Many("hi", "hello")) + " did not contain the same elements in the same (iterated) order as " + decorateToStringValue(LinkedList("HELLO", "HI")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      "should use an explicitly provided Equality" in {
        (all (hiLists) should (be_== (Many("hi", "hello")) or contain theSameElementsInOrderAs LinkedList("HI", "HELLO"))) (decided by upperCaseStringEquality)
        (all (hiLists) should (be_== (Many("ho", "hello")) or contain theSameElementsInOrderAs LinkedList("HI", "HELLO"))) (decided by upperCaseStringEquality)
        (all (hiLists) should (be_== (Many("hi", "hello")) or contain theSameElementsInOrderAs LinkedList("HELLO", "HI"))) (decided by upperCaseStringEquality)

        val e1 = intercept[TestFailedException] {
          (all (hiLists) should (be_== (Many("ho", "hello")) or contain theSameElementsInOrderAs LinkedList("HELLO", "HI"))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(Many("hi", "hello")) + " was not equal to " + decorateToStringValue(Many("ho", "hello")) + ", and " + decorateToStringValue(Many("hi", "hello")) + " did not contain the same elements in the same (iterated) order as " + decorateToStringValue(LinkedList("HELLO", "HI")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }
    }

    "when used with (not contain theSameElementsInOrderAs xx and not contain theSameElementsInOrderAs xx)" - {

      "should do nothing if valid, else throw a TFE with an appropriate error message" in {
        all (list1s) should (not contain theSameElementsInOrderAs (LinkedList(3, 2, 8)) or not contain theSameElementsInOrderAs (LinkedList(8, 3, 4)))
        all (list1s) should (not contain theSameElementsInOrderAs (LinkedList(1, 2, 3)) or not contain theSameElementsInOrderAs (LinkedList(8, 3, 4)))
        all (list1s) should (not contain theSameElementsInOrderAs (LinkedList(3, 2, 8)) or not contain theSameElementsInOrderAs (LinkedList(1, 2, 3)))

        val e1 = intercept[TestFailedException] {
          all (lists) should (not contain theSameElementsInOrderAs (LinkedList(2, 3, 4)) or not contain theSameElementsInOrderAs (LinkedList(2, 3, 4)))
        }
        checkMessageStackDepth(e1, allErrMsg(2, decorateToStringValue(Many(2, 3, 4)) + " contained the same elements in the same (iterated) order as " + decorateToStringValue(LinkedList(2, 3, 4)) + ", and " + decorateToStringValue(Many(2, 3, 4)) + " contained the same elements in the same (iterated) order as " + decorateToStringValue(LinkedList(2, 3, 4)), thisLineNumber - 2, lists), fileName, thisLineNumber - 2)
      }

      "should use the implicit Equality in scope" in {
        implicit val ise = upperCaseStringEquality

        all (hiLists) should (not contain theSameElementsInOrderAs (LinkedList("HELLO", "HI")) or not contain theSameElementsInOrderAs (LinkedList("hello", "hi")))
        all (hiLists) should (not contain theSameElementsInOrderAs (LinkedList("HI", "HELLO")) or not contain theSameElementsInOrderAs (LinkedList("hello", "hi")))
        all (hiLists) should (not contain theSameElementsInOrderAs (LinkedList("HELLO", "HI")) or not contain theSameElementsInOrderAs (LinkedList("hi", "hello")))

        val e1 = intercept[TestFailedException] {
          all (hiLists) should (not contain theSameElementsInOrderAs (LinkedList("HI", "HELLO")) or not contain theSameElementsInOrderAs (LinkedList("hi", "hello")))
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(Many("hi", "hello")) + " contained the same elements in the same (iterated) order as " + decorateToStringValue(LinkedList("HI", "HELLO")) + ", and " + decorateToStringValue(Many("hi", "hello")) + " contained the same elements in the same (iterated) order as " + decorateToStringValue(LinkedList("hi", "hello")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      "should use an explicitly provided Equality" in {
        (all (hiLists) should (not contain theSameElementsInOrderAs (LinkedList("HELLO", "HI")) or not contain theSameElementsInOrderAs (LinkedList("hello", "hi")))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        (all (hiLists) should (not contain theSameElementsInOrderAs (LinkedList("HI", "HELLO")) or not contain theSameElementsInOrderAs (LinkedList("hello", "hi")))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        (all (hiLists) should (not contain theSameElementsInOrderAs (LinkedList("HELLO", "HI")) or not contain theSameElementsInOrderAs (LinkedList("hi", "hello")))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)

        val e1 = intercept[TestFailedException] {
          (all (hiLists) should (not contain theSameElementsInOrderAs (LinkedList("HI", "HELLO")) or not contain theSameElementsInOrderAs (LinkedList("hi", "hello")))) (decided by upperCaseStringEquality, decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(Many("hi", "hello")) + " contained the same elements in the same (iterated) order as " + decorateToStringValue(LinkedList("HI", "HELLO")) + ", and " + decorateToStringValue(Many("hi", "hello")) + " contained the same elements in the same (iterated) order as " + decorateToStringValue(LinkedList("hi", "hello")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }
    }

    "when used with (not be xx and not contain theSameElementsInOrderAs xx)" - {

      "should do nothing if valid, else throw a TFE with an appropriate error message" in {
        all (list1s) should (not be_== (One(2)) or not contain theSameElementsInOrderAs (LinkedList(8, 3, 4)))
        all (list1s) should (not be_== (Many(1, 2, 3)) or not contain theSameElementsInOrderAs (LinkedList(8, 3, 4)))
        all (list1s) should (not be_== (One(2)) or not contain theSameElementsInOrderAs (LinkedList(1, 2, 3)))

        val e1 = intercept[TestFailedException] {
          all (list1s) should (not be_== (Many(1, 2, 3)) or not contain theSameElementsInOrderAs (LinkedList(1, 2, 3)))
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(Many(1, 2, 3)) + " was equal to " + decorateToStringValue(Many(1, 2, 3)) + ", and " + decorateToStringValue(Many(1, 2, 3)) + " contained the same elements in the same (iterated) order as " + decorateToStringValue(LinkedList(1, 2, 3)), thisLineNumber - 2, list1s), fileName, thisLineNumber - 2)
      }

      "should use the implicit Equality in scope" in {
        implicit val ise = upperCaseStringEquality

        all (hiLists) should (not be_== (Many("hello", "ho")) or not contain theSameElementsInOrderAs (LinkedList("HELLO", "HO")))
        all (hiLists) should (not be_== (Many("hi", "hello")) or not contain theSameElementsInOrderAs (LinkedList("HELLO", "HO")))
        all (hiLists) should (not be_== (Many("hello", "ho")) or not contain theSameElementsInOrderAs (LinkedList("HI", "HELLO")))

        val e1 = intercept[TestFailedException] {
          all (hiLists) should (not be_== (Many("hi", "hello")) or not contain theSameElementsInOrderAs (LinkedList("HI", "HELLO")))
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(Many("hi", "hello")) + " was equal to " + decorateToStringValue(Many("hi", "hello")) + ", and " + decorateToStringValue(Many("hi", "hello")) + " contained the same elements in the same (iterated) order as " + decorateToStringValue(LinkedList("HI", "HELLO")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }

      "should use an explicitly provided Equality" in {
        (all (hiLists) should (not be_== (Many("hello", "ho")) or not contain theSameElementsInOrderAs (LinkedList("HELLO", "HO")))) (decided by upperCaseStringEquality)
        (all (hiLists) should (not be_== (Many("hi", "hello")) or not contain theSameElementsInOrderAs (LinkedList("HELLO", "HO")))) (decided by upperCaseStringEquality)
        (all (hiLists) should (not be_== (Many("hello", "ho")) or not contain theSameElementsInOrderAs (LinkedList("HI", "HELLO")))) (decided by upperCaseStringEquality)

        val e1 = intercept[TestFailedException] {
          (all (hiLists) should (not be_== (Many("hi", "hello")) or not contain theSameElementsInOrderAs (LinkedList("HI", "HELLO")))) (decided by upperCaseStringEquality)
        }
        checkMessageStackDepth(e1, allErrMsg(0, decorateToStringValue(Many("hi", "hello")) + " was equal to " + decorateToStringValue(Many("hi", "hello")) + ", and " + decorateToStringValue(Many("hi", "hello")) + " contained the same elements in the same (iterated) order as " + decorateToStringValue(LinkedList("HI", "HELLO")), thisLineNumber - 2, hiLists), fileName, thisLineNumber - 2)
      }
    }
  }
}
