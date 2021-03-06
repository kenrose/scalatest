/*
 * Copyright 2001-2013 Artima, Inc.
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
package org.scalactic

import java.text._
import org.scalatest._
import scala.collection.GenTraversable
import scala.collection.mutable.Buffer
import scala.collection.mutable.ListBuffer

/*
val t = EquaSets[String](StringNormalizations.trimmed.toHashingEquality)
val w = SortedEquaSets[String](StringNormalizations.lowerCased.toOrderingEquality)
val tes = t.EquaSet("tes")
val tfes = t.FastEquaSet("tfes")
val wes = w.EquaSet("les")
val wfes = w.FastEquaSet("lfes")
val wses = w.SortedEquaSet("lses")
val wtes = w.TreeEquaSet("ltes")

EquaBridge[F, EquaSets#EquaSet, EquaSets] // EquaSets#EquaSet                      thatEquaSets.EquaSetBridge
EquaBridge[F, EquaSets#EquaSet, SortedEquaSets] // SortedEquaSets#EquaSet          thatEquaSets.EquaSetBridge

EquaBridge[F, EquaSets#FastEquaSet, EquaSets] // EquaSets#FastEquaSet              thatEquaSets.FastEquaSetBridge
EquaBridge[F, EquaSets#FastEquaSet, SortedEquaSets] // SortedEquaSets#FastEquaSet  thatEquaSets.FastEquaSetBridge

EquaBridge[F, SortedEquaSets#EquaSet, EquaSets] // EquaSets#EquaSet                thatEquaSets.EquaSetBridge
EquaBridge[F, SortedEquaSets#EquaSet, SortedEquaSets] // SortedEquaSets#EquaSet    thatEquaSets.EquaSetBridge

EquaBridge[F, SortedEquaSets#FastEquaSet, EquaSets] // EquaSets#FastEquaSet               thatEquaSets.FastEquaSetBridge
EquaBridge[F, SortedEquaSets#FastEquaSet, SortedEquaSets] // SortedEquaSets#FastEquaSet   thatEquaSets.FastEquaSetBridge

EquaBridge[F, SortedEquaSets#SortedEquaSet, EquaSets] // EquaSets#EquaSet                    thatEquaSets.EquaSetBridge
EquaBridge[F, SortedEquaSets#SortedEquaSet, SortedEquaSets] // SortedEquaSets#SortedEquaSet  thatEquaSets.SortedEquaSetBridge

EquaBridge[F, SortedEquaSets#TreeEquaSet, EquaSets] // EquaSets#EquaSet                      thatEquaSets.EquaSetBridge
EquaBridge[F, SortedEquaSets#TreeEquaSet, SortedEquaSets] // SortedEquaSets#TreeEquaSet      thatEquaSets.TreeEquaSetBridge

// def into[U, EQUASETS[u] <: EquaSets[u]](thatEquaSets: EQUASETS[U]): thatEquaSets.EquaBridgeResult[T]
*/
class EquaSetSpec extends UnitSpec {
  implicit class HasExactType[T](o: T) {
    def shouldHaveExactType[U](implicit ev: T =:= U): Unit = ()
  }
  def normalHashingEquality[T] =
    new HashingEquality[T] {
      def hashCodeFor(a: T): Int = a.hashCode
      def areEqual(a: T, b: Any): Boolean = a == b
    }
  def normalOrderingEquality[T](implicit ord: Ordering[T]) =
    new OrderingEquality[T] {
      def compare(a: T, b: T): Int = ord.compare(a, b)
      def hashCodeFor(a: T): Int = a.hashCode
      def areEqual(a: T, b: Any): Boolean = a == b
    }
  val number = EquaSets[Int](normalHashingEquality[Int])
  val sortedNumber = SortedEquaSets[Int](normalOrderingEquality[Int])
  val lower = EquaSets[String](StringNormalizations.lowerCased.toHashingEquality)
  val trimmed = EquaSets[String](StringNormalizations.trimmed.toHashingEquality)
  val sortedLower = SortedEquaSets[String](StringNormalizations.lowerCased.toOrderingEquality)
  val numberList = EquaSets[List[Int]](normalHashingEquality[List[Int]])
  val numberLower = EquaSets[(Int, String)](normalHashingEquality[(Int, String)])
  val numberLowerTrimmed = EquaSets[(Int, String, String)](normalHashingEquality[(Int, String, String)])
  val numberNumber = EquaSets[number.EquaSet](normalHashingEquality[number.EquaSet])
  def upperCharHashingEquality =
    new HashingEquality[Char] {
      def hashCodeFor(a: Char): Int = a.toUpper.hashCode
      def areEqual(a: Char, b: Any): Boolean =
        b match {
          case bChar: Char => a.toUpper == bChar.toUpper
          case _ => false
        }
    }
  val upperChar = EquaSets[Char](upperCharHashingEquality)
  val regularChar = EquaSets[Char](normalHashingEquality[Char])

  "An EquaSet" can "be constructed with empty" in {
    val emptySet = lower.EquaSet.empty
    emptySet shouldBe empty
  }
  it can "be constructed with apply" in {
    val nonEmptySet = lower.EquaSet("one", "two", "three")
    nonEmptySet should have size 3
    // TODO: After moving enablers to scalactic, make a nominal typeclass
    // instance for Size and Length for EquaSet.
  }
  it should "construct only sets with appropriate element types" in {
    "lower.EquaSet(1, 2, 3)" shouldNot compile
  }
  it should "eliminate 'duplicate' entries passed to the apply factory method" in {
    val nonEmptySet = lower.EquaSet("one", "two", "two", "three", "Three")
    nonEmptySet should have size 3
    // TODO: After moving enablers to scalactic, make a nominal typeclass
    // instance for Size and Length for EquaSet.
  }
  it should "have a toString method" in {
    lower.EquaSet("hi", "ho").toString should === ("EquaSet(hi, ho)")
  }
  it should "have a diff method that takes another EquaSet instance with the same path-dependant type" in {
    lower.EquaSet("hi", "ho") diff lower.EquaSet("HI", "HO") shouldBe lower.EquaSet()
    trimmed.EquaSet("hi", "ho") diff trimmed.EquaSet(" hi ", " ho ") shouldBe trimmed.EquaSet()
    """lower.EquaSet(" hi ", "hi") diff trimmed.EquaSet("hi", "HI")""" shouldNot typeCheck
    lower.EquaSet("hi", "ho") diff lower.EquaSet("ho") shouldBe lower.EquaSet("hi")
    lower.EquaSet("hi", "ho", "let's", "go") diff lower.EquaSet("bo", "no", "go", "ho") shouldBe lower.EquaSet("hi", "let's")
  }
  it should "have a &~ method that takes another EquaSet instance with the same path-dependant type" in {
    lower.EquaSet("hi", "ho") &~ lower.EquaSet("HI", "HO") shouldBe lower.EquaSet()
    trimmed.EquaSet("hi", "ho") &~ trimmed.EquaSet(" hi ", " ho ") shouldBe trimmed.EquaSet()
    """lower.EquaSet(" hi ", "hi") &~ trimmed.EquaSet("hi", "HI")""" shouldNot typeCheck
    lower.EquaSet("hi", "ho") &~ lower.EquaSet("ho") shouldBe lower.EquaSet("hi")
    lower.EquaSet("hi", "ho", "let's", "go") &~ lower.EquaSet("bo", "no", "go", "ho") shouldBe lower.EquaSet("hi", "let's")
  }
  it should "have an intersect method that takes another EquaSet instance with the same path-dependant type" in {
    lower.EquaSet("hi", "ho") intersect lower.EquaSet("HI", "HO") shouldBe lower.EquaSet("hi", "ho")
    trimmed.EquaSet("hi", "ho") intersect trimmed.EquaSet(" hi ", " ho ") shouldBe trimmed.EquaSet("hi", "ho")
    """lower.EquaSet(" hi ", "hi") intersect trimmed.EquaSet("hi", "HI")""" shouldNot typeCheck
    lower.EquaSet("hi", "ho") intersect lower.EquaSet("ho") shouldBe lower.EquaSet("ho")
    lower.EquaSet("hi", "ho", "let's", "go") intersect lower.EquaSet("bo", "no", "go", "ho") shouldBe lower.EquaSet("ho", "go")
  }
  it should "have an & method that takes another EquaSet instance with the same path-dependant type" in {
    lower.EquaSet("hi", "ho") & lower.EquaSet("HI", "HO") shouldBe lower.EquaSet("hi", "ho")
    trimmed.EquaSet("hi", "ho") & trimmed.EquaSet(" hi ", " ho ") shouldBe trimmed.EquaSet("hi", "ho")
    """lower.EquaSet(" hi ", "hi") & trimmed.EquaSet("hi", "HI")""" shouldNot typeCheck
    lower.EquaSet("hi", "ho") & lower.EquaSet("ho") shouldBe lower.EquaSet("ho")
    lower.EquaSet("hi", "ho", "let's", "go") & lower.EquaSet("bo", "no", "go", "ho") shouldBe lower.EquaSet("ho", "go")
  }
  it should "have a union method that takes another EquaSet instance with the same path-dependant type" in {
    lower.EquaSet("hi", "ho") union lower.EquaSet("HI", "HO") shouldBe lower.EquaSet("hi", "ho")
    trimmed.EquaSet("hi", "ho") union trimmed.EquaSet(" hi ", " ho ") shouldBe trimmed.EquaSet("hi", "ho")
    """lower.EquaSet(" hi ", "hi") union trimmed.EquaSet("hi", "HI")""" shouldNot typeCheck
  }
  it should "have a | method that takes another EquaSet instance with the same path-dependant type" in {
    lower.EquaSet("hi", "ho") | lower.EquaSet("HI", "HO") shouldBe lower.EquaSet("hi", "ho")
    trimmed.EquaSet("hi", "ho") | trimmed.EquaSet(" hi ", " ho ") shouldBe trimmed.EquaSet("hi", "ho")
    """lower.EquaSet(" hi ", "hi") | trimmed.EquaSet("hi", "HI")""" shouldNot typeCheck
  }
  it should "have a toSet method" in {
    lower.EquaSet("hi", "ho").toSet should === (Set(lower.EquaBox("hi"), lower.EquaBox("ho")))
  }
  it should "have a + method that takes one argument" in {
    lower.EquaSet("hi", "ho") + "ha" shouldBe lower.EquaSet("hi", "ho", "ha")
    lower.EquaSet("hi", "ho") + "HO" shouldBe lower.EquaSet("hi", "ho")
  }
  it should "have a + method that takes two or more arguments" in {
    lower.EquaSet("hi", "ho") + ("ha", "hey!") shouldBe lower.EquaSet("hi", "ho", "ha", "hey!")
    lower.EquaSet("hi", "ho") + ("HO", "hoe", "Ho!") shouldBe lower.EquaSet("hi", "ho", "hoe", "Ho!")
  }
  it should "have a - method that takes one argument" in {
    lower.EquaSet("hi", "ho", "ha") - "ha" shouldBe lower.EquaSet("hi", "ho")
    lower.EquaSet("hi", "ho") - "HO" shouldBe lower.EquaSet("hi")
    lower.EquaSet("hi", "ho") - "who?" shouldBe lower.EquaSet("hi", "ho")
  }
  it should "have a - method that takes two or more arguments" in {
    lower.EquaSet("hi", "ho", "ha") - ("ha", "howdy!") shouldBe lower.EquaSet("hi", "ho")
    lower.EquaSet("hi", "ho", "fee", "fie", "foe", "fum") - ("HO", "FIE", "fUm")  shouldBe lower.EquaSet("hi", "fee", "foe")
    lower.EquaSet("hi", "ho") - ("who", "goes", "thar") shouldBe lower.EquaSet("hi", "ho")
    lower.EquaSet("hi", "ho") - ("HI", "HO") shouldBe lower.EquaSet.empty
  }
  it should "return an iterator that returns the set's elements" in {
    lower.EquaSet("hi", "ho", "ha", "he").iterator.toList should contain theSameElementsAs List("ha", "he", "hi", "ho")
  }
  it should "have a ++ method that takes a GenTraversableOnce" in {
    lower.EquaSet("hi", "ho") ++ List("ha", "hey!") shouldBe lower.EquaSet("hi", "ho", "ha", "hey!")
    lower.EquaSet("hi", "ho") ++ List("HO", "hoe", "Ho!") shouldBe lower.EquaSet("hi", "ho", "hoe", "Ho!")

    lower.EquaSet("hi", "ho") ++ Set("ha", "hey!") shouldBe lower.EquaSet("hi", "ho", "ha", "hey!")
    lower.EquaSet("hi", "ho") ++ Set("HO", "hoe", "Ho!") shouldBe lower.EquaSet("hi", "ho", "hoe", "Ho!")

    lower.EquaSet("hi", "ho") ++ Vector("ha", "hey!") shouldBe lower.EquaSet("hi", "ho", "ha", "hey!")
    lower.EquaSet("hi", "ho") ++ Vector("HO", "hoe", "Ho!") shouldBe lower.EquaSet("hi", "ho", "hoe", "Ho!")
  }
  it should "have a ++ method that takes another EquaSet" in {
    lower.EquaSet("hi", "ho") ++ lower.EquaSet("ha", "hey!") shouldBe lower.EquaSet("hi", "ho", "ha", "hey!")
    lower.EquaSet("hi", "ho") ++ lower.EquaSet("HO", "hoe", "Ho!") shouldBe lower.EquaSet("hi", "ho", "hoe", "Ho!")
  }
  it should "have a -- method that takes a GenTraversableOnce" in {
    lower.EquaSet("hi", "ho", "ha") -- List("ha", "howdy!") shouldBe lower.EquaSet("hi", "ho")
    lower.EquaSet("hi", "ho", "fee", "fie", "foe", "fum") -- List("HO", "FIE", "fUm")  shouldBe lower.EquaSet("hi", "fee", "foe")
    lower.EquaSet("hi", "ho") -- List("who", "goes", "thar") shouldBe lower.EquaSet("hi", "ho")
    lower.EquaSet("hi", "ho") -- List("HI", "HO") shouldBe lower.EquaSet.empty

    lower.EquaSet("hi", "ho", "ha") -- Set("ha", "howdy!") shouldBe lower.EquaSet("hi", "ho")
    lower.EquaSet("hi", "ho", "fee", "fie", "foe", "fum") -- Set("HO", "FIE", "fUm")  shouldBe lower.EquaSet("hi", "fee", "foe")
    lower.EquaSet("hi", "ho") -- Set("who", "goes", "thar") shouldBe lower.EquaSet("hi", "ho")
    lower.EquaSet("hi", "ho") -- Set("HI", "HO") shouldBe lower.EquaSet.empty

    lower.EquaSet("hi", "ho", "ha") -- Vector("ha", "howdy!") shouldBe lower.EquaSet("hi", "ho")
    lower.EquaSet("hi", "ho", "fee", "fie", "foe", "fum") -- Vector("HO", "FIE", "fUm")  shouldBe lower.EquaSet("hi", "fee", "foe")
    lower.EquaSet("hi", "ho") -- Vector("who", "goes", "thar") shouldBe lower.EquaSet("hi", "ho")
    lower.EquaSet("hi", "ho") -- Vector("HI", "HO") shouldBe lower.EquaSet.empty
  }
  it should "have a -- method that takes another EquaSet" in {
    lower.EquaSet("hi", "ho", "ha") -- lower.EquaSet("ha", "howdy!") shouldBe lower.EquaSet("hi", "ho")
    lower.EquaSet("hi", "ho", "fee", "fie", "foe", "fum") -- lower.EquaSet("HO", "FIE", "fUm")  shouldBe lower.EquaSet("hi", "fee", "foe")
    lower.EquaSet("hi", "ho") -- lower.EquaSet("who", "goes", "thar") shouldBe lower.EquaSet("hi", "ho")
    lower.EquaSet("hi", "ho") -- lower.EquaSet("HI", "HO") shouldBe lower.EquaSet.empty
  }
  it should "have a /: method" in {
    (0 /: number.EquaSet(1))(_ + _) shouldBe 1
    (1 /: number.EquaSet(1))(_ + _) shouldBe 2
    (0 /: number.EquaSet(1, 2, 3))(_ + _) shouldBe 6
    (1 /: number.EquaSet(1, 2, 3))(_ + _) shouldBe 7
  }
  it should "have a :\\ method" in {
    (number.EquaSet(1) :\ 0)(_ + _) shouldBe 1
    (number.EquaSet(1) :\ 1)(_ + _) shouldBe 2
    (number.EquaSet(1, 2, 3) :\ 0)(_ + _) shouldBe 6
    (number.EquaSet(1, 2, 3) :\ 1)(_ + _) shouldBe 7
  }
  it should "have 3 addString methods" in {
    lower.EquaSet("hi").addString(new StringBuilder) shouldBe new StringBuilder("hi")
    number.EquaSet(1, 2, 3).addString(new StringBuilder) shouldBe new StringBuilder("123")

    lower.EquaSet("hi").addString(new StringBuilder, "#") shouldBe new StringBuilder("hi")
    number.EquaSet(1, 2, 3).addString(new StringBuilder, "#") shouldBe new StringBuilder("1#2#3")
    number.EquaSet(1, 2, 3).addString(new StringBuilder, ", ") shouldBe new StringBuilder("1, 2, 3")

    lower.EquaSet("hi").addString(new StringBuilder, "<", "#", ">") shouldBe new StringBuilder("<hi>")
    number.EquaSet(1, 2, 3).addString(new StringBuilder, "<", "#", ">") shouldBe new StringBuilder("<1#2#3>")
    number.EquaSet(1, 2, 3).addString(new StringBuilder, " ( ", ", ", " ) ") shouldBe new StringBuilder(" ( 1, 2, 3 ) ")
  }
  it should "have a aggregate method" in {
    lower.EquaSet("hi", "ho", "ha", "hey!").aggregate(Set[String]())(_ + _, _ ++ _) shouldBe Set("hi", "ho", "ha", "hey!")
    lower.EquaSet("hi", "ho", "ha", "hey!").aggregate(lower.EquaSet())(_ + _, _ ++ _) shouldBe lower.EquaSet("hi", "ho", "ha", "hey!")

    lower.EquaSet("hi", "ho", "HO", "hoe", "Ho!").aggregate(Set[String]())(_ + _, _ ++ _) shouldBe Set("hi", "ho", "hoe", "Ho!")
    lower.EquaSet("hi", "ho", "HO", "hoe", "Ho!").aggregate(lower.EquaSet())(_ + _, _ ++ _) shouldBe lower.EquaSet("hi", "ho", "hoe", "Ho!")
  }
  it should "have an apply method" in {
    val a = number.EquaSet(1, 2, 3)
    a(2) shouldEqual true
    a(5) shouldEqual false

    val b = lower.EquaSet("hi")
    b("hi") shouldEqual true
    b("Hi") shouldEqual true
    b("hI") shouldEqual true
    b("HI") shouldEqual true
    b("he") shouldEqual false
  }
  it should "have an andThen method (inherited from PartialFunction)" in {
    val pf1 = number.EquaSet(1) andThen (!_)
    pf1(1) shouldEqual false
    pf1(2) shouldEqual true

    val pf2 = number.EquaSet(1, 2, 3) andThen (!_)
    pf2(1) shouldEqual false
    pf2(2) shouldEqual false
    pf2(3) shouldEqual false
    pf2(0) shouldEqual true
  }
  it should "have a canEqual method" in {
    number.EquaSet(1).canEqual(3) shouldBe false
    number.EquaSet(1).canEqual("hi") shouldBe false
    number.EquaSet(1).canEqual(number.EquaSet(1)) shouldBe true
    number.EquaSet(1).canEqual(number.EquaSet(1, 2, 3)) shouldBe true
    number.EquaSet(1).canEqual(lower.EquaSet("hi")) shouldBe false
    val orderingEquality = StringNormalizations.lowerCased.toOrderingEquality
    val equaSets = EquaSets[String](orderingEquality) // Two different EquaSets instances
    val sortedEquaSets = SortedEquaSets[String](orderingEquality)
    val equaSet = equaSets.EquaSet("hi", "ho")
    val fastEquaSet = equaSets.FastEquaSet("Bi", "Bo")
    val sortedEquaSet = sortedEquaSets.SortedEquaSet("cI", "cO")
    val treeEquaSet = sortedEquaSets.TreeEquaSet("DI", "DO")
    equaSet.canEqual(equaSet) shouldBe true
    equaSet.canEqual(equaSets.FastEquaSet("Hi", "Ho")) shouldBe true
    equaSets.FastEquaSet("Hi", "Ho").canEqual(equaSet) shouldBe true
    equaSet.canEqual(fastEquaSet) shouldBe true
    fastEquaSet.canEqual(equaSet) shouldBe true
    equaSet.canEqual(sortedEquaSet) shouldBe true
    sortedEquaSet.canEqual(equaSet) shouldBe true
    equaSet.canEqual(treeEquaSet) shouldBe true
    treeEquaSet.canEqual(equaSet) shouldBe true
    fastEquaSet.canEqual(fastEquaSet) shouldBe true
    fastEquaSet.canEqual(sortedEquaSet) shouldBe true
    sortedEquaSet.canEqual(fastEquaSet) shouldBe true
    fastEquaSet.canEqual(treeEquaSet) shouldBe true
    treeEquaSet.canEqual(fastEquaSet) shouldBe true
    sortedEquaSet.canEqual(sortedEquaSet) shouldBe true
    sortedEquaSet.canEqual(treeEquaSet) shouldBe true
    treeEquaSet.canEqual(sortedEquaSet) shouldBe true
    treeEquaSet.canEqual(treeEquaSet) shouldBe true
  }
  it should "have an into.collect method" in {
    // Can map into self explicitly too
    number.EquaSet(1, 2, 3).into(number).map(_ + 1) shouldBe number.EquaSet(2, 3, 4)
    number.EquaSet(5).into(number).map(_ + 3) shouldBe number.EquaSet(8)

    // EquaSet into EquaSets => EquaSet
    val result1 = number.EquaSet(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).into(lower).collect { case i if i % 2 == 0 => (i * 2).toString }
    result1 shouldBe lower.EquaSet("4", "8", "12", "16", "20")
    result1.shouldHaveExactType[lower.EquaSet]

    // EquaSet into SortedEquaSets => EquaSet
    val result2 = number.EquaSet(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).into(sortedLower).collect { case i if i % 2 == 0 => (i * 2).toString }
    result2 shouldBe sortedLower.EquaSet("4", "8", "12", "16", "20")
    result2.shouldHaveExactType[sortedLower.EquaSet]

    // FastEquaSet into EquaSets => FastEquaSet
    val result3 = number.FastEquaSet(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).into(lower).collect { case i if i % 2 == 0 => (i * 2).toString }
    result3 shouldBe lower.FastEquaSet("4", "8", "12", "16", "20")
    result3.shouldHaveExactType[lower.FastEquaSet]

    // FastEquaSet into SortedEquaSets => FastEquaSet
    val result4 = number.FastEquaSet(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).into(sortedLower).collect { case i if i % 2 == 0 => (i * 2).toString }
    result4 shouldBe sortedLower.FastEquaSet("4", "8", "12", "16", "20")
    result4.shouldHaveExactType[sortedLower.FastEquaSet]

    // Extra stuff from oldInto tests
    /*
    scala> List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).collect { case i if i % 2 == 0 => i * 2 }
    res3: List[Int] = List(4, 8, 12, 16, 20)

    scala> List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).collect { case i if i > 10 == 0 => i * 2 }
    res4: List[Int] = List()
    */
    val result = number.EquaSet(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).into(lower) collect { case i if i % 2 == 0 => (i * 2).toString }
    result shouldBe lower.EquaSet("4", "8", "12", "16", "20")
    // result.shouldHaveExactType[lower.EquaSet]
    number.EquaSet(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).into(lower) collect { case i if i > 10 => (i * 2).toString } shouldBe lower.EquaSet.empty
    /*
    scala> List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).collect { case i if i % 2 == 0 => i * 2 }
    res3: List[Int] = List(4, 8, 12, 16, 20)

    scala> List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).collect { case i if i > 10 == 0 => i * 2 }
    res4: List[Int] = List()
    */
    val resultB = number.EquaSet(10, 9, 8, 7, 6, 5, 4, 3, 2, 1).into(sortedLower) collect { case i if i % 2 == 0 => (i * 2).toString }
    resultB shouldBe sortedLower.EquaSet("4", "8", "12", "16", "20")
    // result.shouldHaveExactType[sortedLower.EquaSet]
    number.EquaSet(10, 9, 8, 7, 6, 5, 4, 3, 2, 1).into(sortedLower) collect { case i if i > 10 => (i * 2).toString } shouldBe sortedLower.EquaSet.empty
  }
  it should "have a collect method that only accepts functions that result in the path-enclosed type" in {
    /*
    scala> List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).collect { case i if i % 2 == 0 => i * 2 }
    res3: List[Int] = List(4, 8, 12, 16, 20)

    scala> List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).collect { case i if i > 10 == 0 => i * 2 }
    res4: List[Int] = List()
    */
    val result1 = number.EquaSet(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) collect { case i if i % 2 == 0 => i * 2 }
    result1 shouldBe number.EquaSet(4, 8, 12, 16, 20)
    result1.shouldHaveExactType[number.EquaSet]
    number.EquaSet(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) collect { case i if i > 10 => i * 2 } shouldBe number.EquaSet.empty
    val result2 = number.FastEquaSet(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) collect { case i if i % 2 == 0 => i * 2 }
    result2 shouldBe number.FastEquaSet(4, 8, 12, 16, 20)
    result2.shouldHaveExactType[number.FastEquaSet]
  }
  it should "have an compose method, inherited from PartialFunction" in {
    val fn: Int => Boolean = number.EquaSet(1, 2, 3).compose(_ + 1)
    fn(0) shouldBe true
    fn(1) shouldBe true
    fn(2) shouldBe true
    fn(3) shouldBe false
  }
  it should "have an contains method that does a type check" in {
    val e = number.EquaSet(1, 2, 3)
    e.contains(-1) shouldBe false
    e.contains(0) shouldBe false
    e.contains(1) shouldBe true
    e.contains(2) shouldBe true
    e.contains(3) shouldBe true
    e.contains(4) shouldBe false
    """e.contains("five")""" shouldNot typeCheck
    new CheckedEquality {
      val es = lower.EquaSet("one", "two", "three")
      """es.contains(5)""" shouldNot typeCheck
      es.contains("ONE") shouldBe true;
    }
    abstract class Fruit {
      val name: String
    }
    case class Apple(name: String) extends Fruit
    case class Orange(name: String) extends Fruit
    val mac = Apple("Mcintosh")
    val navel = Orange("Navel") 
    val equalityOfFruit =
      new HashingEquality[Fruit] {
        private val nameEquality = StringNormalizations.lowerCased.toHashingEquality
        def areEqual(a: Fruit, b: Any): Boolean =
          b match {
            case bFruit: Fruit => nameEquality.areEqual(a.name, bFruit.name)
            case _ => false
          }
        def hashCodeFor(a: Fruit): Int = nameEquality.hashCodeFor(a.name)
      }
    val fruitEquaSets = EquaSets(equalityOfFruit)
    val fruits = fruitEquaSets.EquaSet(mac, navel)
    fruits.contains(mac) shouldBe true
  }
  it should "have 3 copyToArray methods" in {

    val seq = number.EquaSet(1, 2, 3, 4, 5).toSet.toSeq

    val arr1 = Array.fill(5)(number.EquaBox(-1))
    number.EquaSet(1, 2, 3, 4, 5).copyToArray(arr1)
    arr1 shouldEqual Array(seq(0), seq(1), seq(2), seq(3), seq(4))

    val arr2 = Array.fill(5)(number.EquaBox(-1))
    number.EquaSet(1, 2, 3, 4, 5).copyToArray(arr2, 1)
    arr2 shouldEqual Array(number.EquaBox(-1), seq(0), seq(1), seq(2), seq(3))

    val arr3 = Array.fill(5)(number.EquaBox(-1))
    number.EquaSet(1, 2, 3, 4, 5).copyToArray(arr3, 1, 2)
    arr3 shouldEqual Array(number.EquaBox(-1), seq(0), seq(1), number.EquaBox(-1), number.EquaBox(-1))
  }
  it should "have a copyToBuffer method" in {
    val seq = number.EquaSet(1, 2, 3, 4, 5).toSet.toSeq
    val buf = ListBuffer.fill(3)(number.EquaBox(-1))
    number.EquaSet(1, 2, 3, 4, 5).copyToBuffer(buf)
    buf shouldEqual Buffer(number.EquaBox(-1), number.EquaBox(-1), number.EquaBox(-1), seq(0), seq(1), seq(2), seq(3), seq(4))
  }
  it should "have a count method" in {
    val set = number.EquaSet(1, 2, 3, 4, 5)
    set.count(_ > 10) shouldBe 0
    set.count(_ % 2 == 0) shouldBe 2
    set.count(_ % 2 == 1) shouldBe 3
  }
  it should "have a drop method" in {
    val set = number.EquaSet(1, 2, 3, 4, 5)
    val seq = number.EquaSet(1, 2, 3, 4, 5).toSet.toSeq
    set.drop(0) shouldBe number.EquaSet(seq(0).value, seq(1).value, seq(2).value, seq(3).value, seq(4).value)
    set.drop(1) shouldBe number.EquaSet(seq(1).value, seq(2).value, seq(3).value, seq(4).value)
    set.drop(2) shouldBe number.EquaSet(seq(2).value, seq(3).value, seq(4).value)
    set.drop(3) shouldBe number.EquaSet(seq(3).value, seq(4).value)
    set.drop(4) shouldBe number.EquaSet(seq(4).value)
    set.drop(5) shouldBe number.EquaSet()
  }
  it should "have a dropRight method" in {
    val set = number.EquaSet(1, 2, 3, 4, 5)
    val seq = number.EquaSet(1, 2, 3, 4, 5).toSet.toSeq
    set.dropRight(0) shouldBe number.EquaSet(seq(0).value, seq(1).value, seq(2).value, seq(3).value, seq(4).value)
    set.dropRight(1) shouldBe number.EquaSet(seq(0).value, seq(1).value, seq(2).value, seq(3).value)
    set.dropRight(2) shouldBe number.EquaSet(seq(0).value, seq(1).value, seq(2).value)
    set.dropRight(3) shouldBe number.EquaSet(seq(0).value, seq(1).value)
    set.dropRight(4) shouldBe number.EquaSet(seq(0).value)
    set.dropRight(5) shouldBe number.EquaSet()
  }
  it should "have a dropWhile method" in {
    val set = number.EquaSet(1, 2, 3, 4, 5)
    val seq = number.EquaSet(1, 2, 3, 4, 5).toSet.toSeq
    set.dropWhile(_ < 1) shouldBe number.EquaSet(seq.map(_.value).dropWhile(_ < 1): _*)
    set.dropWhile(_ < 2) shouldBe number.EquaSet(seq.map(_.value).dropWhile(_ < 2): _*)
    set.dropWhile(_ < 3) shouldBe number.EquaSet(seq.map(_.value).dropWhile(_ < 3): _*)
    set.dropWhile(_ < 4) shouldBe number.EquaSet(seq.map(_.value).dropWhile(_ < 4): _*)
    set.dropWhile(_ < 5) shouldBe number.EquaSet(seq.map(_.value).dropWhile(_ < 5): _*)
    set.dropWhile(_ < 6) shouldBe number.EquaSet()
  }
  it should "have an enclosingEquaSets method" in {
    lower.EquaSet("hi").enclosingEquaSets shouldBe lower
    lower.FastEquaSet("hi").enclosingEquaSets shouldBe lower
  }
  it should "have an equals method" in {
    sortedLower.SortedEquaSet("one", "two", "three") shouldEqual sortedLower.EquaSet("Three", "Two", "One")
    sortedLower.EquaSet("one", "two", "three") shouldEqual sortedLower.SortedEquaSet("Three", "Two", "One")
    val orderingEquality = StringNormalizations.lowerCased.toOrderingEquality
    val equaSets = EquaSets[String](orderingEquality) // Two different EquaSets instances
    val sortedEquaSets = SortedEquaSets[String](orderingEquality)
    val equaSet = equaSets.EquaSet("hi", "ho")
    val fastEquaSet = equaSets.FastEquaSet("Hi", "Ho")
    val sortedEquaSet = sortedEquaSets.SortedEquaSet("hI", "hO")
    val treeEquaSet = sortedEquaSets.TreeEquaSet("HI", "HO")
    equaSet shouldEqual equaSet
    equaSet shouldEqual equaSets.FastEquaSet("Hi", "Ho")
    equaSets.FastEquaSet("Hi", "Ho") shouldEqual equaSet
    equaSet shouldEqual fastEquaSet
    fastEquaSet shouldEqual equaSet
    equaSet shouldEqual sortedEquaSet
    sortedEquaSet shouldEqual equaSet
    equaSet shouldEqual treeEquaSet
    treeEquaSet shouldEqual equaSet
    fastEquaSet shouldEqual fastEquaSet
    fastEquaSet shouldEqual sortedEquaSet
    sortedEquaSet shouldEqual fastEquaSet
    fastEquaSet shouldEqual treeEquaSet
    treeEquaSet shouldEqual fastEquaSet
    sortedEquaSet shouldEqual sortedEquaSet
    sortedEquaSet shouldEqual treeEquaSet
    treeEquaSet shouldEqual sortedEquaSet
    treeEquaSet shouldEqual treeEquaSet
  }
  it should "have an exists method" in {
    number.EquaSet(1, 2, 3).exists(_ == 2) shouldBe true
    number.EquaSet(1, 2, 3).exists(_ == 5) shouldBe false
  }
  it should "have a filter method" in {
    val set = number.EquaSet(1, 2, 3)
    set.filter(_ == 1) shouldBe number.EquaSet(1)
    set.filter(_ == 2) shouldBe number.EquaSet(2)
    set.filter(_ == 3) shouldBe number.EquaSet(3)
  }
  it should "have a filterNot method" in {
    val set = number.EquaSet(1, 2, 3)
    set.filterNot(_ == 1) shouldBe number.EquaSet(2, 3)
    set.filterNot(_ == 2) shouldBe number.EquaSet(1, 3)
    set.filterNot(_ == 3) shouldBe number.EquaSet(1, 2)
  }
  it should "have a find method" in {
    number.EquaSet(1, 2, 3).find(_ == 5) shouldBe None
    number.EquaSet(1, 2, 3).find(_ == 2) shouldBe Some(number.EquaBox(2))
  }
  it should "have an into.flatMap method" in {

    // EquaSet into EquaSets => EquaSet
    val result1 = number.EquaSet(7, 8, 9).into(lower).flatMap(i => lower.EquaSet(i.toString))
    result1 shouldBe lower.EquaSet("7", "8", "9")
    result1.shouldHaveExactType[lower.EquaSet]

    // EquaSet into SortedEquaSets => EquaSet
    val result2 = number.EquaSet(7, 8, 9).into(sortedLower).flatMap(i => sortedLower.EquaSet(i.toString))
    result2 shouldBe sortedLower.EquaSet("7", "8", "9")
    result2.shouldHaveExactType[sortedLower.EquaSet]

    // FastEquaSet into EquaSets => FastEquaSet
    val result3 = number.FastEquaSet(7, 8, 9).into(lower).flatMap(i => lower.FastEquaSet(i.toString))
    result3 shouldBe lower.FastEquaSet("7", "8", "9")
    result3.shouldHaveExactType[lower.FastEquaSet]

    // FastEquaSet into SortedEquaSets => FastEquaSet
    val result4 = number.FastEquaSet(7, 8, 9).into(sortedLower).flatMap(i => sortedLower.FastEquaSet(i.toString))
    result4 shouldBe sortedLower.FastEquaSet("7", "8", "9")
    result4.shouldHaveExactType[sortedLower.FastEquaSet]

    // Extra stuff from oldInto test
    number.EquaSet(8).into(lower).flatMap(i => lower.EquaSet(i.toString)) shouldBe lower.EquaSet("8")
    number.EquaSet(8).into(sortedLower).flatMap(i => sortedLower.SortedEquaSet(i.toString)) shouldBe sortedLower.SortedEquaSet("8")

    number.EquaSet(9, 8, 7).into(lower).flatMap(i => lower.EquaSet(i.toString)) shouldBe lower.EquaSet("9", "8", "7")
    number.EquaSet(9, 8, 7).into(sortedLower).flatMap(i => sortedLower.SortedEquaSet(i.toString)) shouldBe sortedLower.EquaSet("9", "8", "7")

    val cis = number.EquaSet('c'.toInt, 'C'.toInt, 'b'.toInt, 'B'.toInt, 'a'.toInt, 'A'.toInt)
    cis.into(regularChar).map(i => i.toChar) shouldBe regularChar.EquaSet('A', 'a', 'b', 'B', 'C', 'c')
    (for (i <- cis.into(regularChar)) yield i.toChar) shouldBe regularChar.EquaSet('A', 'a', 'b', 'B', 'C', 'c')

    val regChars = cis.into(regularChar).flatMap(i => regularChar.EquaSet(i.toChar))
    regChars shouldBe regularChar.EquaSet('A', 'a', 'b', 'B', 'C', 'c')
    regChars.into(upperChar).flatMap(c => upperChar.EquaSet(c)) shouldBe upperChar.EquaSet('A', 'b', 'C')
    val regCharsFromFor =
      for {
        u <- (
          for (c <- cis into regularChar) yield c.toChar
        ) into upperChar
      } yield u
    regCharsFromFor shouldBe upperChar.EquaSet('A', 'B', 'C')
  }
  it should "have a flatMap method" in {
    number.EquaSet(1, 2, 3) flatMap (i => number.EquaSet(i + 1)) shouldBe number.EquaSet(2, 3, 4)
    number.EquaSet(5) flatMap (i => number.EquaSet(i + 3)) shouldBe number.EquaSet(8)
    val ss = number.EquaSet(1, 2)
    val is = number.EquaSet(1, 2, 3)
    (for (s <- ss; i <- is) yield s + i) shouldBe number.EquaSet(2, 3, 4, 3, 4, 5)
  }
  it should "have an into.flatten method that works on nested EquaSet" in {
/*
    implicit def nestedOrdering: Ordering[number.SortedEquaSet] =
      new Ordering[number.SortedEquaSet] {
        def compare(x: number.SortedEquaSet, y: number.SortedEquaSet): Int = x.size - y.size
      }
*/

    // EquaSet into EquaSets => EquaSet
    val numberNumber1 = EquaSets[number.EquaSet](normalHashingEquality[number.EquaSet])
    val result1 = numberNumber1.EquaSet(number.EquaSet(1, 2), number.EquaSet(3)).into(number).flatten
    result1 shouldBe number.EquaSet(1, 2, 3)
    result1.shouldHaveExactType[number.EquaSet]

    // EquaSet into SortedEquaSets => EquaSet
    val numberNumber2 = EquaSets[sortedNumber.SortedEquaSet](normalHashingEquality[sortedNumber.SortedEquaSet])
    val result2 = numberNumber2.EquaSet(sortedNumber.SortedEquaSet(1, 2), sortedNumber.SortedEquaSet(3)).into(sortedNumber).flatten
    result2 shouldBe sortedNumber.EquaSet(1, 2, 3)
    result2.shouldHaveExactType[sortedNumber.EquaSet]

    // FastEquaSet into EquaSets => FastEquaSet
    val numberNumber3 = EquaSets[number.FastEquaSet](normalHashingEquality[number.FastEquaSet])
    val result3 = numberNumber3.FastEquaSet(number.FastEquaSet(1, 2), number.FastEquaSet(3)).into(number).flatten // I think also true for into EquaSets.EquaSet
    result3 shouldBe number.FastEquaSet(1, 2, 3)
    result3.shouldHaveExactType[number.FastEquaSet]

    // FastEquaSet into SortedEquaSets => FastEquaSet
    val numberNumber4 = EquaSets[sortedNumber.EquaSet](normalHashingEquality[sortedNumber.EquaSet])
    val result4 = numberNumber4.FastEquaSet(sortedNumber.EquaSet(1, 2), sortedNumber.EquaSet(3)).into(sortedNumber).flatten
    result4 shouldBe sortedNumber.FastEquaSet(1, 2, 3)
    result4.shouldHaveExactType[sortedNumber.FastEquaSet]

    // Extra stuff from oldInto test
    numberNumber.EquaSet(number.EquaSet(1, 2), number.EquaSet(3)).into(number).flatten shouldBe number.EquaSet(1, 2, 3)
    numberNumber.EquaSet(number.EquaSet(1)).into(number).flatten shouldBe number.EquaSet(1)
  }
  it can "be flattened when in a GenTraversableOnce" in {
    // need to keep this commented out until finish implementing all methods
    Vector(number.EquaSet(1, 2, 3), number.EquaSet(1, 2, 3)).flatten shouldBe Vector(1, 2, 3, 1, 2, 3)
    List(number.EquaSet(1, 2, 3), number.EquaSet(1, 2, 3)).flatten shouldBe List(1, 2, 3, 1, 2, 3)
    List(number.EquaSet(1, 2, 3), number.EquaSet(1, 2, 3)).toIterator.flatten.toStream shouldBe List(1, 2, 3, 1, 2, 3).toIterator.toStream
    List(number.EquaSet(1, 2, 3), number.EquaSet(1, 2, 3)).par.flatten shouldBe List(1, 2, 3, 1, 2, 3).par
  }
  it should "have a flatten method that works on nested GenTraversable" in {
    numberList.EquaSet(List(1, 2), List(3)).flatten shouldBe List(1, 2, 3)
    numberList.EquaSet(List(1)).flatten shouldBe List(1)
  }
  it should "have a fold method" in {
    number.EquaSet(1).fold(0)(_ + _) shouldBe 1
    number.EquaSet(1).fold(1)(_ * _) shouldBe 1
    number.EquaSet(2).fold(0)(_ + _) shouldBe 2
    number.EquaSet(2).fold(1)(_ * _) shouldBe 2
    number.EquaSet(3).fold(0)(_ + _) shouldBe 3
    number.EquaSet(3).fold(1)(_ * _) shouldBe 3
    number.EquaSet(1, 2, 3).fold(0)(_ + _) shouldBe 6
    number.EquaSet(1, 2, 3).fold(1)(_ * _) shouldBe 6
    number.EquaSet(1, 2, 3, 4, 5).fold(0)(_ + _) shouldBe 15
    number.EquaSet(1, 2, 3, 4, 5).fold(1)(_ * _) shouldBe 120
  }
  it should "have a foldLeft method" in {
    number.EquaSet(1).foldLeft(0)(_ + _) shouldBe 1
    number.EquaSet(1).foldLeft(1)(_ + _) shouldBe 2
    number.EquaSet(1, 2, 3).foldLeft(0)(_ + _) shouldBe 6
    number.EquaSet(1, 2, 3).foldLeft(1)(_ + _) shouldBe 7
  }
  it should "have a foldRight method" in {
    number.EquaSet(1).foldRight(0)(_ + _) shouldBe 1
    number.EquaSet(1).foldRight(1)(_ + _) shouldBe 2
    number.EquaSet(1, 2, 3).foldRight(0)(_ + _) shouldBe 6
    number.EquaSet(1, 2, 3).foldRight(1)(_ + _) shouldBe 7
  }
  it should "have a forall method" in {
    number.EquaSet(1, 2, 3, 4, 5).forall(_ > 0) shouldBe true
    number.EquaSet(1, 2, 3, 4, 5).forall(_ < 0) shouldBe false
  }
  it should "have a foreach method" in {
    var num = 0
    number.EquaSet(1, 2, 3) foreach (num += _)
    num shouldBe 6
    for (i <- number.EquaSet(1, 2, 3))
      num += i
    num shouldBe 12
    number.EquaSet(5) foreach (num *= _)
    num shouldBe 60
  }
  it should "have a groupBy method" in {
    number.EquaSet(1, 2, 3, 4, 5).groupBy(_ % 2) shouldBe Map(1 -> number.EquaSet(1, 3, 5), 0 -> number.EquaSet(2, 4))
    number.EquaSet(1, 2, 3, 3, 3).groupBy(_ % 2) shouldBe Map(1 -> number.EquaSet(1, 3, 3, 3), 0 -> number.EquaSet(2))
    number.EquaSet(1, 1, 3, 3, 3).groupBy(_ % 2) shouldBe Map(1 -> number.EquaSet(1, 1, 3, 3, 3))
    number.EquaSet(1, 2, 3, 5, 7).groupBy(_ % 2) shouldBe Map(1 -> number.EquaSet(1, 3, 5, 7), 0 -> number.EquaSet(2))
  }
  it should "have a grouped method" in {
    number.EquaSet(1, 2, 3).grouped(2).toList shouldBe List(number.EquaSet(1, 2), number.EquaSet(3))
    number.EquaSet(1, 2, 3).grouped(1).toList shouldBe List(number.EquaSet(1), number.EquaSet(2), number.EquaSet(3))
    an [IllegalArgumentException] should be thrownBy { number.EquaSet(1, 2, 3).grouped(0).toList }
    val set = number.EquaSet(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    val seq = set.toSet.toSeq
    set.grouped(2).toList shouldBe List(number.EquaSet(seq(0).value, seq(1).value), number.EquaSet(seq(2).value, seq(3).value), number.EquaSet(seq(4).value, seq(5).value), number.EquaSet(seq(6).value, seq(7).value), number.EquaSet(seq(8).value, seq(9).value))
    set.grouped(3).toList shouldBe List(number.EquaSet(seq(0).value, seq(1).value, seq(2).value), number.EquaSet(seq(3).value, seq(4).value, seq(5).value), number.EquaSet(seq(6).value, seq(7).value, seq(8).value), number.EquaSet(seq(9).value))
    set.grouped(4).toList shouldBe List(number.EquaSet(seq(0).value, seq(1).value, seq(2).value, seq(3).value), number.EquaSet(seq(4).value, seq(5).value, seq(6).value, seq(7).value), number.EquaSet(seq(8).value, seq(9).value))
    number.EquaSet(1).grouped(2).toList shouldBe List(number.EquaSet(1))
    number.EquaSet(1).grouped(1).toList shouldBe List(number.EquaSet(1))
  }
  it should "have a hasDefiniteSize method" in {
    number.EquaSet(1).hasDefiniteSize shouldBe true
    number.EquaSet(1, 2).hasDefiniteSize shouldBe true
  }
  it should "have a head method" in {
    lower.EquaSet("hi").head shouldBe "hi"
    number.EquaSet(1, 2, 3).head shouldBe 1
  }
  it should "have a headOption method" in {
    lower.EquaSet("hi").headOption shouldBe Some("hi")
    number.EquaSet(1, 2, 3).headOption shouldBe Some(1)
  }
  it should "have an init method" in {
    number.EquaSet(1, 2, 3).init shouldBe number.EquaSet(1, 2)
  }
  it should "have an inits method" in {
    val inits = number.EquaSet(1, 2, 3).inits
    inits.next shouldBe number.EquaSet(1,2,3)
    inits.next shouldBe number.EquaSet(1,2)
    inits.next shouldBe number.EquaSet(1)
    inits.next shouldBe number.EquaSet()
    inits.hasNext shouldBe false
  }
  it should "have an isTraversableAgain method" in {
    lower.EquaSet("hi").isTraversableAgain shouldBe true
    number.EquaSet(1, 2, 3).isTraversableAgain shouldBe true
  }
  it should "have a last method" in {
    lower.EquaSet("hi").last shouldBe "hi"
    number.EquaSet(1, 2, 3).last shouldBe 3
  }
  it should "have an lastOption method" in {
    lower.EquaSet("hi").lastOption shouldBe Some("hi")
    number.EquaSet(1, 2, 3).lastOption shouldBe Some(3)
  }
  it should "have an into.map method" in {
    // Can map directly if want to stay in same EquaSets
    number.EquaSet(1, 2, 3).map(_ + 1) shouldBe number.EquaSet(2, 3, 4)
    (for (ele <- number.EquaSet(1, 2, 3)) yield ele * 2) shouldBe number.EquaSet(2, 4, 6)
    number.EquaSet(5) map (_ + 3) shouldBe number.EquaSet(8)

    // Can map into self explicitly too
    number.EquaSet(1, 2, 3).into(number).map(_ + 1) shouldBe number.EquaSet(2, 3, 4)
    number.EquaSet(5).into(number).map(_ + 3) shouldBe number.EquaSet(8)

    // EquaSet into EquaSets => EquaSet
    val result1 = number.EquaSet(7, 8, 9).into(lower).map(_.toString)
    result1 shouldBe lower.EquaSet("7", "8", "9")
    result1.shouldHaveExactType[lower.EquaSet]

    // EquaSet into SortedEquaSets => EquaSet
    val result2 = number.EquaSet(7, 8, 9).into(sortedLower).map(_.toString)
    result2 shouldBe sortedLower.EquaSet("7", "8", "9")
    result2.shouldHaveExactType[sortedLower.EquaSet]

    // FastEquaSet into EquaSets => FastEquaSet
    val result3 = number.FastEquaSet(7, 8, 9).into(lower).map(_.toString)
    result3 shouldBe lower.FastEquaSet("7", "8", "9")
    result3.shouldHaveExactType[lower.FastEquaSet]

    // FastEquaSet into SortedEquaSets => FastEquaSet
    val result4 = number.FastEquaSet(7, 8, 9).into(sortedLower).map(_.toString)
    result4 shouldBe sortedLower.FastEquaSet("7", "8", "9")
    result4.shouldHaveExactType[sortedLower.FastEquaSet]

    // Extra stuff from oldInto test
    number.EquaSet(1, 2, 3).into(number).map(_ + 1) shouldBe number.EquaSet(2, 3, 4)
    number.EquaSet(5).into(number).map(_ + 3) shouldBe number.EquaSet(8)
    number.EquaSet(8).into(lower).map(_.toString) shouldBe lower.EquaSet("8")
  }
  it should "have a map method" in {
    number.EquaSet(1, 2, 3) .map (_ + 1) shouldBe number.EquaSet(2, 3, 4)
    (for (ele <- number.EquaSet(1, 2, 3)) yield ele * 2) shouldBe number.EquaSet(2, 4, 6)
    number.EquaSet(5) map (_ + 3) shouldBe number.EquaSet(8)
  }
  it should "have a max method" in {
    number.EquaSet(1, 2, 3, 4, 5).max shouldBe 5
    number.EquaSet(1).max shouldBe 1
    number.EquaSet(-1).max shouldBe -1
    lower.EquaSet("aaa", "ccc", "bbb").max shouldBe "ccc"
  }
  it should "have a maxBy method" in {
    number.EquaSet(1, 2, 3, 4, 5).maxBy(_.abs) shouldBe 5
    number.EquaSet(1, 2, 3, 4, -5).maxBy(_.abs) shouldBe -5
  }
  it should "have a min method" in {
    number.EquaSet(1, 2, 3, 4, 5).min shouldBe 1
    number.EquaSet(1).min shouldBe 1
    number.EquaSet(-1).min shouldBe -1
    lower.EquaSet("aaa", "ccc", "bbb").min shouldBe "aaa"
  }
  it should "have a minBy method" in {
    number.EquaSet(1, 2, 3, 4, 5).minBy(_.abs) shouldBe 1
    number.EquaSet(-1, -2, 3, 4, 5).minBy(_.abs) shouldBe -1
  }
  it should "have a 3 mkString method" in {

    lower.EquaSet("hi").mkString shouldBe "hi"
    number.EquaSet(1, 2, 3).mkString shouldBe "123"

    lower.EquaSet("hi").mkString("#") shouldBe "hi"
    number.EquaSet(1, 2, 3).mkString("#") shouldBe "1#2#3"
    number.EquaSet(1, 2, 3).mkString(", ") shouldBe "1, 2, 3"

    lower.EquaSet("hi").mkString("<", "#", ">") shouldBe "<hi>"
    number.EquaSet(1, 2, 3).mkString("<", "#", ">") shouldBe "<1#2#3>"
    number.EquaSet(1, 2, 3).mkString(" ( ", ", ", " ) ") shouldBe " ( 1, 2, 3 ) "
  }
  it should "have an nonEmpty method" in {
    lower.EquaSet("hi").nonEmpty shouldBe true
    number.EquaSet(1, 2, 3).nonEmpty shouldBe true
  }
  it should "have a partition method" in {
    number.EquaSet(1, 2, 3, 4).partition(_ < 3) shouldBe (number.EquaSet(1, 2), number.EquaSet(3, 4))
  }
  it should "have a product method" in {
    number.EquaSet(1, 2, 3).product shouldBe 6
    number.EquaSet(3).product shouldBe 3
    number.EquaSet(3, 4, 5).product shouldBe 60
    number.EquaSet(3, 4, 5).product shouldBe 60
  }
  it should "have a reduce method" in {
    number.EquaSet(1, 2, 3, 4, 5).reduce(_ + _) shouldBe 15
    number.EquaSet(1, 2, 3, 4, 5).reduce(_ * _) shouldBe 120
    number.EquaSet(5).reduce(_ + _) shouldBe 5
    number.EquaSet(5).reduce(_ * _) shouldBe 5
  }
  it should "have a reduceLeft method" in {
    number.EquaSet(1).reduceLeft(_ + _) shouldBe 1
    number.EquaSet(1).reduceLeft(_ * _) shouldBe 1
    number.EquaSet(1, 2, 3).reduceLeft(_ + _) shouldBe 6
    number.EquaSet(1, 2, 3).reduceLeft(_ * _) shouldBe 6
    number.EquaSet(1, 2, 3, 4, 5).reduceLeft(_ * _) shouldBe 120
  }
  it should "have a reduceLeftOption method" in {
    number.EquaSet(1).reduceLeftOption(_ + _) shouldBe Some(1)
    number.EquaSet(1).reduceLeftOption(_ * _) shouldBe Some(1)
    number.EquaSet(1, 2, 3).reduceLeftOption(_ + _) shouldBe Some(6)
    number.EquaSet(1, 2, 3).reduceLeftOption(_ * _) shouldBe Some(6)
    number.EquaSet(1, 2, 3, 4, 5).reduceLeftOption(_ * _) shouldBe Some(120)
  }
  it should "have a reduceOption method" in {
    number.EquaSet(1, 2, 3, 4, 5).reduceOption(_ + _) shouldBe Some(15)
    number.EquaSet(1, 2, 3, 4, 5).reduceOption(_ * _) shouldBe Some(120)
    number.EquaSet(5).reduceOption(_ + _) shouldBe Some(5)
    number.EquaSet(5).reduceOption(_ * _) shouldBe Some(5)
  }
  it should "have a reduceRight method" in { One(1).reduceRight(_ + _) shouldBe 1
    number.EquaSet(1).reduceRight(_ * _) shouldBe 1
    number.EquaSet(1, 2, 3).reduceRight(_ + _) shouldBe 6
    number.EquaSet(1, 2, 3).reduceRight(_ * _) shouldBe 6
    number.EquaSet(1, 2, 3, 4, 5).reduceRight(_ * _) shouldBe 120
  }
  it should "have a reduceRightOption method" in {
    number.EquaSet(1).reduceRightOption(_ + _) shouldBe Some(1)
    number.EquaSet(1).reduceRightOption(_ * _) shouldBe Some(1)
    number.EquaSet(1, 2, 3).reduceRightOption(_ + _) shouldBe Some(6)
    number.EquaSet(1, 2, 3).reduceRightOption(_ * _) shouldBe Some(6)
    number.EquaSet(1, 2, 3, 4, 5).reduceRightOption(_ * _) shouldBe Some(120)
  }
  it should "have a repr method" in {
    number.EquaSet(1, 2, 3).repr shouldBe Set(number.EquaBox(1), number.EquaBox(2), number.EquaBox(3))
  }
  it should "have a sameElements method that takes a GenIterable" in {
    number.EquaSet(1, 2, 3, 4, 5).sameElements(number.EquaSet(1, 2, 3, 4, 5).toSet.toSeq.map(_.value)) shouldBe true
    number.EquaSet(1, 2, 3, 4, 5).sameElements(List(1, 2, 3, 4)) shouldBe false
    number.EquaSet(1, 2, 3, 4, 5).sameElements(List(1, 2, 3, 4, 5, 6)) shouldBe false
    number.EquaSet(1, 2, 3, 4, 5).sameElements(List(1, 2, 3, 4, 4)) shouldBe false
    number.EquaSet(3).sameElements(List(1, 2, 3, 4, 5)) shouldBe false
    number.EquaSet(3).sameElements(List(1)) shouldBe false
    number.EquaSet(3).sameElements(List(3)) shouldBe true
  }
  it should "have an into.scanLeft method" in {

    // EquaSet into EquaSets => EquaSet
    val result1 = number.EquaSet(7, 8, 9).into(lower).scanLeft("z")(_ + _)
    result1 shouldBe lower.EquaSet("z", "z7", "z78", "z789")
    result1.shouldHaveExactType[lower.EquaSet]

    // EquaSet into SortedEquaSets => EquaSet
    val result2 = number.EquaSet(7, 8, 9).into(sortedLower).scanLeft("z")(_ + _)
    result2 shouldBe sortedLower.EquaSet("z", "z7", "z78", "z789")
    result2.shouldHaveExactType[sortedLower.EquaSet]

    // FastEquaSet into EquaSets => FastEquaSet
    val result3 = number.FastEquaSet(7, 8, 9).into(lower).scanLeft("z")(_ + _)
    result3 shouldBe lower.FastEquaSet("z", "z7", "z78", "z789")
    result3.shouldHaveExactType[lower.FastEquaSet]

    // FastEquaSet into SortedEquaSets => FastEquaSet
    val result4 = number.FastEquaSet(7, 8, 9).into(sortedLower).scanLeft("z")(_ + _)
    result4 shouldBe sortedLower.FastEquaSet("z", "z7", "z78", "z789")
    result4.shouldHaveExactType[sortedLower.FastEquaSet]

    // Extra stuff from oldInto test
    number.EquaSet(1, 2, 3).into(lower).scanLeft("z")(_ + _) shouldBe lower.EquaSet("z", "z1", "z12", "z123")
    number.EquaSet(0).into(lower).scanLeft("z")(_ + _) shouldBe lower.EquaSet("z", "z0")
  }
  it should "have a scanLeft method" in {
    number.EquaSet(1).scanLeft(0)(_ + _) shouldBe number.EquaSet(0, 1)
    number.EquaSet(1, 2, 3).scanLeft(0)(_ + _) shouldBe number.EquaSet(0, 1, 3, 6)
  }
  it should "have a scanRight method" in {
    number.EquaSet(1).scanRight(0)(_ + _) shouldBe number.EquaSet(1, 0)
    number.EquaSet(1, 2, 3).scanRight(0)(_ + _) shouldBe number.EquaSet(6, 5, 3, 0)
  }
  it should "have an into.scanRight method" in {

    // EquaSet into EquaSets => EquaSet
    val result1 = number.EquaSet(7, 8, 9).into(lower).scanRight("z")(_ + _)
    result1 shouldBe lower.EquaSet("789z", "89z", "9z", "z")
    result1.shouldHaveExactType[lower.EquaSet]

    // EquaSet into SortedEquaSets => EquaSet
    val result2 = number.EquaSet(7, 8, 9).into(sortedLower).scanRight("z")(_ + _)
    result2 shouldBe sortedLower.EquaSet("789z", "89z", "9z", "z")
    result2.shouldHaveExactType[sortedLower.EquaSet]

    // FastEquaSet into EquaSets => FastEquaSet
    val result3 = number.FastEquaSet(7, 8, 9).into(lower).scanRight("z")(_ + _)
    result3 shouldBe lower.FastEquaSet("789z", "89z", "9z", "z")
    result3.shouldHaveExactType[lower.FastEquaSet]

    // FastEquaSet into SortedEquaSets => FastEquaSet
    val result4 = number.FastEquaSet(7, 8, 9).into(sortedLower).scanRight("z")(_ + _)
    result4 shouldBe sortedLower.FastEquaSet("789z", "89z", "9z", "z")
    result4.shouldHaveExactType[sortedLower.FastEquaSet]

    // Extra stuff from oldInto test
    number.EquaSet(1, 2, 3).into(lower).scanRight("z")(_ + _) shouldBe lower.EquaSet("123z", "23z", "3z", "z")
    number.EquaSet(0).into(lower).scanRight("z")(_ + _) shouldBe lower.EquaSet("0z", "z")
  }
  it should "have a slice method" in {
    number.EquaSet(3).slice(0, 0) shouldBe number.EquaSet()
    number.EquaSet(1, 2, 3).slice(2, 1) shouldBe number.EquaSet()
    number.EquaSet(1, 2, 3).slice(1, 3) shouldBe number.EquaSet(2, 3)
  }
  it should "have 2 sliding methods" in {

    val seq = number.EquaSet(1, 2, 3, 4, 5).toSet.toSeq

    number.EquaSet(1).sliding(1).toList shouldBe List(number.EquaSet(1))
    number.EquaSet(1).sliding(2).toList shouldBe List(number.EquaSet(1))
    number.EquaSet(1, 2, 3).sliding(2).toList shouldBe List(number.EquaSet(1, 2), number.EquaSet(2, 3))
    number.EquaSet(1, 2, 3).sliding(1).toList shouldBe List(number.EquaSet(1), number.EquaSet(2), number.EquaSet(3))
    number.EquaSet(1, 2, 3).sliding(3).toList shouldBe List(number.EquaSet(1, 2, 3))
    number.EquaSet(1, 2, 3, 4, 5).sliding(3).toList shouldBe List(number.EquaSet(seq(0).value, seq(1).value, seq(2).value), number.EquaSet(seq(1).value, seq(2).value, seq(3).value), number.EquaSet(seq(2).value, seq(3).value, seq(4).value))
    number.EquaSet(1, 2, 3, 4, 5).sliding(2).toList shouldBe List(number.EquaSet(seq(0).value, seq(1).value), number.EquaSet(seq(1).value, seq(2).value), number.EquaSet(seq(2).value, seq(3).value), number.EquaSet(seq(3).value, seq(4).value))
    number.EquaSet(1, 2, 3, 4, 5).sliding(1).toList shouldBe List(number.EquaSet(seq(0).value), number.EquaSet(seq(1).value), number.EquaSet(seq(2).value), number.EquaSet(seq(3).value), number.EquaSet(seq(4).value))
    number.EquaSet(1, 2, 3, 4, 5).sliding(4).toList shouldBe List(number.EquaSet(seq(0).value, seq(1).value, seq(2).value, seq(3).value), number.EquaSet(seq(1).value, seq(2).value, seq(3).value, seq(4).value))
    number.EquaSet(1, 2, 3, 4, 5).sliding(5).toList shouldBe List(number.EquaSet(seq(0).value, seq(1).value, seq(2).value, seq(3).value, seq(4).value))

    number.EquaSet(1).sliding(1, 1).toList shouldBe List(number.EquaSet(1))
    number.EquaSet(1).sliding(1, 2).toList shouldBe List(number.EquaSet(1))
    number.EquaSet(1, 2, 3).sliding(1, 1).toList shouldBe List(number.EquaSet(1), number.EquaSet(2), number.EquaSet(3))
    number.EquaSet(1, 2, 3).sliding(2, 1).toList shouldBe List(number.EquaSet(1, 2), number.EquaSet(2, 3))
    number.EquaSet(1, 2, 3).sliding(2, 2).toList shouldBe List(number.EquaSet(1, 2), number.EquaSet(3))
    number.EquaSet(1, 2, 3).sliding(3, 2).toList shouldBe List(number.EquaSet(1, 2, 3))
    number.EquaSet(1, 2, 3).sliding(3, 1).toList shouldBe List(number.EquaSet(1, 2, 3))
    number.EquaSet(1, 2, 3, 4, 5).sliding(3, 1).toList shouldBe List(number.EquaSet(seq(0).value, seq(1).value, seq(2).value), number.EquaSet(seq(1).value, seq(2).value, seq(3).value), number.EquaSet(seq(2).value, seq(3).value, seq(4).value))
    number.EquaSet(1, 2, 3, 4, 5).sliding(2, 2).toList shouldBe List(number.EquaSet(seq(0).value, seq(1).value), number.EquaSet(seq(2).value, seq(3).value), number.EquaSet(seq(4).value))
    number.EquaSet(1, 2, 3, 4, 5).sliding(2, 3).toList shouldBe List(number.EquaSet(seq(0).value, seq(1).value), number.EquaSet(seq(3).value, seq(4).value))
    number.EquaSet(1, 2, 3, 4, 5).sliding(2, 4).toList shouldBe List(number.EquaSet(seq(0).value, seq(1).value), number.EquaSet(seq(4).value))
    number.EquaSet(1, 2, 3, 4, 5).sliding(3, 1).toList shouldBe List(number.EquaSet(seq(0).value, seq(1).value, seq(2).value), number.EquaSet(seq(1).value, seq(2).value, seq(3).value), number.EquaSet(seq(2).value, seq(3).value, seq(4).value))
    number.EquaSet(1, 2, 3, 4, 5).sliding(3, 2).toList shouldBe List(number.EquaSet(seq(0).value, seq(1).value, seq(2).value), number.EquaSet(seq(2).value, seq(3).value, seq(4).value))
    number.EquaSet(1, 2, 3, 4, 5).sliding(3, 3).toList shouldBe List(number.EquaSet(seq(0).value, seq(1).value, seq(2).value), number.EquaSet(seq(3).value, seq(4).value))
    number.EquaSet(1, 2, 3, 4, 5).sliding(3, 4).toList shouldBe List(number.EquaSet(seq(0).value, seq(1).value, seq(2).value), number.EquaSet(seq(4).value))
  }
  it should "have a span method" in {
    number.EquaSet(1, 2, 3).span(_ < 3) shouldBe (number.EquaSet(1, 2), number.EquaSet(3))
    number.EquaSet(1, 2, 3).span(_ > 3) shouldBe (number.EquaSet(), number.EquaSet(1, 2, 3))
  }
  it should "have a splitAt method" in {
    number.EquaSet(1, 2, 3).splitAt(0) shouldBe (number.EquaSet(), number.EquaSet(1, 2, 3))
    number.EquaSet(1, 2, 3).splitAt(1) shouldBe (number.EquaSet(1), number.EquaSet(2, 3))
    number.EquaSet(1, 2, 3).splitAt(2) shouldBe (number.EquaSet(1, 2), number.EquaSet(3))
    number.EquaSet(1, 2, 3).splitAt(3) shouldBe (number.EquaSet(1, 2, 3), number.EquaSet())
  }
  it should "have a stringPrefix method" in {
    number.EquaSet(1).stringPrefix shouldBe "EquaSet"
    number.EquaSet(1, 2, 3).stringPrefix shouldBe "EquaSet"
    lower.EquaSet("1").stringPrefix shouldBe "EquaSet"
    lower.EquaSet("1", "2", "3").stringPrefix shouldBe "EquaSet"
  }
  it should "have a subsetOf method" in {
    number.EquaSet(2, 3).subsetOf(number.EquaSet(1, 2, 3, 4, 5)) shouldBe true
    number.EquaSet(2).subsetOf(number.EquaSet(1, 2, 3, 4, 5)) shouldBe true
    number.EquaSet(2, 0).subsetOf(number.EquaSet(1, 2, 3, 4, 5)) shouldBe false
    lower.EquaSet("aa", "bb").subsetOf(lower.EquaSet("aa", "bb", "cc")) shouldBe true
    lower.EquaSet("aA", "Bb").subsetOf(lower.EquaSet("aa", "bb", "cc")) shouldBe true
    lower.EquaSet("aa", "bb").subsetOf(lower.EquaSet("aA", "Bb", "cC")) shouldBe true
    lower.EquaSet("aa", "bc").subsetOf(lower.EquaSet("aa", "bb", "cc")) shouldBe false
  }
  it should "have a 2 subsets method" in {
    val subsets = number.EquaSet(1, 2, 3).subsets.toList
    subsets should have length 8
    subsets should contain (number.EquaSet())
    subsets should contain (number.EquaSet(1))
    subsets should contain (number.EquaSet(2))
    subsets should contain (number.EquaSet(3))
    subsets should contain (number.EquaSet(1, 2))
    subsets should contain (number.EquaSet(1, 3))
    subsets should contain (number.EquaSet(2, 3))
    subsets should contain (number.EquaSet(1, 2, 3))

    val subsets2 = number.EquaSet(1, 2, 3).subsets(2).toList
    subsets2 should have length 3
    subsets2 should contain (number.EquaSet(1, 2))
    subsets2 should contain (number.EquaSet(1, 3))
    subsets2 should contain (number.EquaSet(2, 3))
  }
  it should "have a sum method" in {
    number.EquaSet(1).sum shouldBe 1
    number.EquaSet(5).sum shouldBe 5
    number.EquaSet(1, 2, 3).sum shouldBe 6
    number.EquaSet(1, 2, 3, 4, 5).sum shouldBe 15
  }
  it should "have an tail method" in {
    number.EquaSet(1, 2, 3).tail shouldBe number.EquaSet(2, 3)
  }
  it should "have an tails method" in {
    number.EquaSet(1, 2, 3).tails.toList shouldBe List(number.EquaSet(1,2,3), number.EquaSet(2,3), number.EquaSet(3), number.EquaSet())
  }
  it should "have a take method" in {
    number.EquaSet(1, 2, 3).take(0) shouldBe number.EquaSet()
    number.EquaSet(1, 2, 3).take(1) shouldBe number.EquaSet(1)
    number.EquaSet(1, 2, 3).take(2) shouldBe number.EquaSet(1, 2)
    number.EquaSet(1, 2, 3).take(3) shouldBe number.EquaSet(1, 2, 3)
  }
  it should "have a takeRight method" in {
    number.EquaSet(1, 2, 3).takeRight(0) shouldBe number.EquaSet()
    number.EquaSet(1, 2, 3).takeRight(1) shouldBe number.EquaSet(3)
    number.EquaSet(1, 2, 3).takeRight(2) shouldBe number.EquaSet(2, 3)
    number.EquaSet(1, 2, 3).takeRight(3) shouldBe number.EquaSet(1, 2, 3)
  }
  it should "have a to method" in {
    number.EquaSet(1).to[List] shouldBe List(number.EquaBox(1))
    number.EquaSet(1, 2, 3).to[List] shouldBe List(number.EquaBox(1), number.EquaBox(2), number.EquaBox(3))
    number.EquaSet(1, 2, 3).to[scala.collection.mutable.ListBuffer] shouldBe ListBuffer(number.EquaBox(1), number.EquaBox(2), number.EquaBox(3))
    number.EquaSet(1, 2, 3).to[Vector] shouldBe Vector(number.EquaBox(1), number.EquaBox(2), number.EquaBox(3))
  }
  it should "have a toArray method" in {
    number.EquaSet(1, 2, 3).toArray shouldBe (Array(number.EquaBox(1), number.EquaBox(2), number.EquaBox(3)))
    lower.EquaSet("a", "b").toArray shouldBe (Array(lower.EquaBox("a"), lower.EquaBox("b")))
    number.EquaSet(1).toArray shouldBe (Array(number.EquaBox(1)))
  }
  it should "have a toBuffer method" in {
    number.EquaSet(1, 2, 3).toBuffer shouldBe (Buffer(number.EquaBox(1), number.EquaBox(2), number.EquaBox(3)))
    lower.EquaSet("a", "b").toBuffer shouldBe (Buffer(lower.EquaBox("a"), lower.EquaBox("b")))
    number.EquaSet(1).toBuffer shouldBe (Buffer(number.EquaBox(1)))
  }
  it should "have a toIndexedSeq method" in {
    number.EquaSet(1, 2, 3).toIndexedSeq shouldBe (IndexedSeq(number.EquaBox(1), number.EquaBox(2), number.EquaBox(3)))
    lower.EquaSet("a", "b").toIndexedSeq shouldBe (IndexedSeq(lower.EquaBox("a"), lower.EquaBox("b")))
    number.EquaSet(1).toIndexedSeq shouldBe (IndexedSeq(number.EquaBox(1)))
  }
  it should "have a toIterable method" in {
    number.EquaSet(1, 2, 3).toIterable shouldBe (Set(number.EquaBox(1), number.EquaBox(2), number.EquaBox(3)))
    lower.EquaSet("a", "b").toIterable shouldBe (Set(lower.EquaBox("a"), lower.EquaBox("b")))
    number.EquaSet(1).toIterable shouldBe (Set(number.EquaBox(1)))
  }
  it should "have a toIterator method" in {
    number.EquaSet(1, 2, 3).toIterator.toList shouldBe (Iterator(number.EquaBox(1), number.EquaBox(2), number.EquaBox(3)).toList)
    lower.EquaSet("a", "b").toIterator.toList shouldBe (Iterator(lower.EquaBox("a"), lower.EquaBox("b")).toList)
    number.EquaSet(1).toIterator.toList shouldBe (Iterator(number.EquaBox(1)).toList)
    number.EquaSet(1, 2, 3).toIterator shouldBe an [Iterator[_]]
    lower.EquaSet("a", "b").toIterator shouldBe an [Iterator[_]]
    number.EquaSet(1).toIterator shouldBe an [Iterator[_]]
  }
  it should "have a toList method" in {
    number.EquaSet(1, 2, 3).toList shouldBe (List(number.EquaBox(1), number.EquaBox(2), number.EquaBox(3)))
    lower.EquaSet("a", "b").toList shouldBe (List(lower.EquaBox("a"), lower.EquaBox("b")))
    number.EquaSet(1).toList shouldBe (List(number.EquaBox(1)))
  }
  it should "have a toSeq method" in {
    number.EquaSet(1, 2, 3).toSeq shouldBe (Seq(number.EquaBox(1), number.EquaBox(2), number.EquaBox(3)))
    lower.EquaSet("a", "b").toSeq shouldBe (Seq(lower.EquaBox("a"), lower.EquaBox("b")))
    number.EquaSet(1).toSeq shouldBe (Seq(number.EquaBox(1)))
  }
  it should "have a toStream method" in {
    number.EquaSet(1, 2, 3).toStream shouldBe (Stream(number.EquaBox(1), number.EquaBox(2), number.EquaBox(3)))
    lower.EquaSet("a", "b").toStream shouldBe (Stream(lower.EquaBox("a"), lower.EquaBox("b")))
    number.EquaSet(1).toStream shouldBe(Stream(number.EquaBox(1)))
  }
  it should "have a toTraversable method" in {
    number.EquaSet(1, 2, 3).toTraversable should === (Set(number.EquaBox(1), number.EquaBox(2), number.EquaBox(3)))
    lower.EquaSet("a", "b").toTraversable should === (Set(lower.EquaBox("a"), lower.EquaBox("b")))
    number.EquaSet(1).toTraversable should === (Set(number.EquaBox(1)))
  }
  it should "have a toVector method" in {
    number.EquaSet(1, 2, 3).toVector should === (Vector(number.EquaBox(1), number.EquaBox(2), number.EquaBox(3)))
    lower.EquaSet("a", "b").toVector should === (Vector(lower.EquaBox("a"), lower.EquaBox("b")))
    number.EquaSet(1).toVector should === (Vector(number.EquaBox(1)))
  }
  it should "have a transpose method" in {
    numberList.EquaSet(List(1, 2, 3), List(4, 5, 6), List(7, 8, 9)).transpose shouldBe numberList.EquaSet(List(1, 4, 7), List(2, 5, 8), List(3, 6, 9))
    numberList.EquaSet(List(1, 2), List(3, 4), List(5, 6), List(7, 8)).transpose shouldBe numberList.EquaSet(List(1, 3, 5, 7), List(2, 4, 6, 8))
    numberList.EquaSet(List(1, 2), List(3, 4), List(5, 6), List(7, 8)).transpose.transpose shouldBe numberList.EquaSet(List(1, 2), List(3, 4), List(5, 6), List(7, 8))
    numberList.EquaSet(List(1, 2, 3), List(4, 5, 6), List(7, 8, 9)).transpose.transpose shouldBe numberList.EquaSet(List(1, 2, 3), List(4, 5, 6), List(7, 8, 9))
  }
  it should "have an unzip method" in {
    numberLower.EquaSet((1, "2")).unzip(number, lower) shouldBe ((number.EquaSet(1), lower.EquaSet("2")))
    numberLower.EquaSet((1, "2"), (3, "4")).unzip(number, lower) shouldBe ((number.EquaSet(1, 3), lower.EquaSet("2", "4")))
    numberLower.EquaSet((1, "2"), (3, "4"), (5, "6")).unzip(number, lower) shouldBe ((number.EquaSet(1, 3, 5), lower.EquaSet("2", "4", "6")))
  }
  it should "have an unzip3 method" in {
    numberLowerTrimmed.EquaSet((1, "2", "3")).unzip3(number, lower, trimmed) shouldBe (number.EquaSet(1), lower.EquaSet("2"), trimmed.EquaSet("3"))
    numberLowerTrimmed.EquaSet((1, "2", "3"), (4, "5", "6")).unzip3(number, lower, trimmed) shouldBe (number.EquaSet(1, 4), lower.EquaSet("2", "5"), trimmed.EquaSet("3", "6"))
    numberLowerTrimmed.EquaSet((1, "2", "3"), (4, "5", "6"), (7, "8", "9")).unzip3(number, lower, trimmed) shouldBe (number.EquaSet(1, 4, 7), lower.EquaSet("2", "5", "8"), trimmed.EquaSet("3", "6", "9"))
  }
  it should "have 2 views method" in {
    number.EquaSet(3).view(0, 0).toList shouldBe List()
    number.EquaSet(1, 2, 3).view(2, 1).toList shouldBe List()
    number.EquaSet(1, 2, 3).view(1, 3).toList shouldBe List(number.EquaBox(2), number.EquaBox(3))
    number.EquaSet(1, 2, 3).view.toList shouldBe List(number.EquaBox(1), number.EquaBox(2), number.EquaBox(3))
  }
  it should "have a zip method" in {
    number.EquaSet(1, 2, 3).zip(List("4", "5", "6")) shouldBe Set((1, "4"), (2, "5"), (3, "6"))
    number.EquaSet(1, 2, 3).zip(List("4", "5")) shouldBe Set((1, "4"), (2, "5"))
  }
  it should "have a zipAll method" in {
    number.EquaSet(1, 2, 3).zipAll(List("4", "5", "6"), 0, "0") shouldBe Set((1, "4"), (2, "5"), (3, "6"))
    number.EquaSet(1, 2, 3).zipAll(List("4", "5"), 0, "0") shouldBe Set((1, "4"), (2, "5"), (3, "0"))
    number.EquaSet(1, 2).zipAll(List("4", "5", "6"), 0, "0") shouldBe Set((1, "4"), (2, "5"), (0, "6"))
  }
  it should "have a zipWithIndex method" in {
    number.EquaSet(99).zipWithIndex shouldBe Set((99,0))
    number.EquaSet(1, 2, 3).zipWithIndex shouldBe Set((1,0), (2,1), (3,2))
  }
/*
abstract def contains(elem: A): Boolean
abstract def iterator: Iterator[A] 
def &(that: GenSet[A]): Set[A]
def &~(that: GenSet[A]): Set[A]
def ++(elems: GenTraversableOnce[A]): Set[A]
def ++[B](that: GenTraversableOnce[B]): Set[B]
def ++:[B >: A, That](that: Traversable[B])(implicit bf: CanBuildFrom[Set[A], B, That]): That
def ++:[B](that: TraversableOnce[B]): Set[B]
def -(elem1: A, elem2: A, elems: A*): Set[A]
def --(xs: GenTraversableOnce[A]): Set[A]
def /:[B](z: B)(op: (B, A) ⇒ B): B
def :\[B](z: B)(op: (A, B) ⇒ B): B
def addString(b: StringBuilder): StringBuilder
def addString(b: StringBuilder, sep: String): StringBuilder
def addString(b: StringBuilder, start: String, sep: String, end: String): StringBuilder
def aggregate[B](z: ⇒ B)(seqop: (B, A) ⇒ B, combop: (B, B) ⇒ B): B
def andThen[A](g: (Boolean) ⇒ A): (A) ⇒ A
def apply(elem: A): Boolean
def canEqual(that: Any): Boolean
def collect[B](pf: PartialFunction[A, B]): Set[B]
def collectFirst[B](pf: PartialFunction[A, B]): Option[B]
def companion: GenericCompanion[Set]
def compose[A](g: (A) ⇒ A): (A) ⇒ Boolean
def copyToArray(xs: Array[A], start: Int, len: Int): Unit
def copyToArray(xs: Array[A]): Unit
def copyToArray(xs: Array[A], start: Int): Unit
def copyToBuffer[B >: A](dest: Buffer[B]): Unit
def count(p: (A) ⇒ Boolean): Int
def diff(that: GenSet[A]): Set[A]
def drop(n: Int): Set[A]
def dropRight(n: Int): Set[A]
def dropWhile(p: (A) ⇒ Boolean): Set[A]
def empty: Set[A]
def equals(that: Any): Boolean
def exists(p: (A) ⇒ Boolean): Boolean
def filter(p: (A) ⇒ Boolean): Set[A]
def filterNot(p: (A) ⇒ Boolean): Set[A]
def find(p: (A) ⇒ Boolean): Option[A]
def flatMap[B](f: (A) ⇒ GenTraversableOnce[B]): Set[B]
def flatten[B]: Set[B]
def fold[A1 >: A](z: A1)(op: (A1, A1) ⇒ A1): A1
def foldLeft[B](z: B)(op: (B, A) ⇒ B): B
def foldRight[B](z: B)(op: (A, B) ⇒ B): B
def forall(p: (A) ⇒ Boolean): Boolean
def foreach(f: (A) ⇒ Unit): Unit
def genericBuilder[B]: Builder[B, Set[B]]
def groupBy[K](f: (A) ⇒ K): immutable.Map[K, Set[A]]
def grouped(size: Int): Iterator[Set[A]]
def hasDefiniteSize: Boolean
def hashCode(): Int
def head: A
def headOption: Option[A]
def init: Set[A]
def inits: Iterator[Set[A]]
def intersect(that: GenSet[A]): Set[A]
def isEmpty: Boolean
final def isTraversableAgain: Boolean
def last: A
def lastOption: Option[A]
def map[B](f: (A) ⇒ B): Set[B]
def max: A
def maxBy[B](f: (A) ⇒ B): A
def min: A
def minBy[B](f: (A) ⇒ B): A
def mkString: String
def mkString(sep: String): String
def mkString(start: String, sep: String, end: String): String
def nonEmpty: Boolean
def par: ParSet[A]
def partition(p: (A) ⇒ Boolean): (Set[A], Set[A])
def product: A
def reduce[A1 >: A](op: (A1, A1) ⇒ A1): A1
def reduceLeft[B >: A](op: (B, A) ⇒ B): B
def reduceLeftOption[B >: A](op: (B, A) ⇒ B): Option[B]
def reduceOption[A1 >: A](op: (A1, A1) ⇒ A1): Option[A1]
def reduceRight[B >: A](op: (A, B) ⇒ B): B
def reduceRightOption[B >: A](op: (A, B) ⇒ B): Option[B]
def repr: Set[A]
def sameElements(that: GenIterable[A]): Boolean
def scan[B >: A, That](z: B)(op: (B, B) ⇒ B)(implicit cbf: CanBuildFrom[Set[A], B, That]): That
def scanLeft[B, That](z: B)(op: (B, A) ⇒ B)(implicit bf: CanBuildFrom[Set[A], B, That]): That
def scanRight[B, That](z: B)(op: (A, B) ⇒ B)(implicit bf: CanBuildFrom[Set[A], B, That]): That
def seq: Set[A]
def size: Int
def slice(from: Int, until: Int): Set[A]
def sliding(size: Int, step: Int): Iterator[Set[A]]
def sliding(size: Int): Iterator[Set[A]]
def span(p: (A) ⇒ Boolean): (Set[A], Set[A])
def splitAt(n: Int): (Set[A], Set[A])
def stringPrefix: String
def subsetOf(that: GenSet[A]): Boolean
def subsets: Iterator[Set[A]]
def subsets(len: Int): Iterator[Set[A]]
def sum: A
def tail: Set[A]
def tails: Iterator[Set[A]]
def take(n: Int): Set[A]
def takeRight(n: Int): Set[A]
def takeWhile(p: (A) ⇒ Boolean): Set[A]
def to[Col[_]]: Col[A]
def toArray: Array[A]
def toBuffer[A1 >: A]: Buffer[A1]
def toIndexedSeq: immutable.IndexedSeq[A]
def toIterable: Iterable[A]
def toIterator: Iterator[A]
def toList: List[A]
def toMap[T, U]: Map[T, U]
def toParArray: ParArray[T]
def toSeq: Seq[A]
def toSet[B >: A]: immutable.Set[B]
def toStream: immutable.Stream[A]
def toString(): String
def toTraversable: Traversable[A]
def toVector: Vector[A]
def transpose[B](implicit asTraversable: (A) ⇒ GenTraversableOnce[B]): Set[Set[B]]
def union(that: GenSet[A]): Set[A]
def unzip[A1, A2](implicit asPair: (A) ⇒ (A1, A2)): (Set[A1], Set[A2])
def unzip3[A1, A2, A3](implicit asTriple: (A) ⇒ (A1, A2, A3)): (Set[A1], Set[A2], Set[A3])
def view(from: Int, until: Int): IterableView[A, Set[A]]
def view: IterableView[A, Set[A]]
def withFilter(p: (A) ⇒ Boolean): FilterMonadic[A, Set[A]]
def zip[B](that: GenIterable[B]): Set[(A, B)]
def zipAll[B](that: Iterable[B], thisElem: A, thatElem: B): Set[(A, B)]
def zipWithIndex: Set[(A, Int)]
*/
}

