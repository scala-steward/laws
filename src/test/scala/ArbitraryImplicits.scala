import org.scalacheck.Arbitrary
import org.scalacheck.Cogen
import org.scalacheck.Gen

import shapeless.Lazy

import Algebra.Func
import Algebra.{Tree, Branch, Leaf}
import Algebra.Show
import Algebra.Box
import Algebra.Symbol
import Algebra.Codec
import ContravariantModule.Contravariant
import ContravariantModule.ContravariantSyntax

object ArbitraryImplicits {

  implicit def funcToArb[X, R](
    implicit 
      AX: Arbitrary[X],
      AR: Arbitrary[R]): Arbitrary[Func[X, R]] =
    Arbitrary {
      for {
        x <- AX.arbitrary
        r <- AR.arbitrary
      } yield Func(x => r)
    }

  implicit def treeChooser: Arbitrary[Boolean] =
    Arbitrary{ Gen.choose(1, 10) map (_ <= 6) }

  implicit def branchArb[A](
    implicit 
      LEFT : Lazy[Arbitrary[Tree[A]]],
      RIGHT: Lazy[Arbitrary[Tree[A]]]): Arbitrary[Branch[A]] =
    Arbitrary {
      for {
        l <- LEFT.value.arbitrary
        r <- RIGHT.value.arbitrary
      } yield Branch(l, r)
    }

  implicit def leafArb[A](
    implicit 
      AR: Arbitrary[A]): Arbitrary[Leaf[A]] =
    Arbitrary {
      AR.arbitrary map Leaf.apply
    }

  implicit def treeArb[A](
    implicit
      LAR: Arbitrary[Leaf[A]],
      BAR: Lazy[Arbitrary[Branch[A]]]): Arbitrary[Tree[A]] =
    Arbitrary {
      treeChooser.arbitrary flatMap { 
        if(_) LAR.arbitrary 
        else  BAR.value.arbitrary 
      } 
    }

  implicit def showArb[A](
    implicit 
      SH: Show[A]): Arbitrary[Show[A]] =
    Arbitrary(SH)

  implicit def boxArb[A](
    implicit
      AS: Arbitrary[Show[A]],
      CA: Contravariant[Show]): Arbitrary[Show[Box[A]]] =
    Arbitrary {
      AS.arbitrary map { sa => sa contramap (_.value) }
    }

  implicit def aToBox[A](
    implicit
      AB: Arbitrary[Boolean]): Arbitrary[A => Box[Boolean]] =
    Arbitrary {
      AB.arbitrary map { b => (a: A) => Box(b) }
    }

  implicit def symbol(implicit AS: Arbitrary[String]): Arbitrary[Symbol] =
    Arbitrary { AS.arbitrary map Symbol.apply }

  implicit def symbolCodec(
    implicit
      CS: Codec[Symbol]): Arbitrary[Codec[Symbol]] =
    Arbitrary(CS)

  implicit def symbolCogen: Cogen[Symbol] =
    Cogen[Symbol]((s: Symbol) => s.name.size.toLong)

  implicit def symbolToInt(
    implicit
      AI: Arbitrary[Int]): Arbitrary[Symbol => Int] =
    Arbitrary.arbFunction1[Symbol, Int]

  implicit def intToSymbol: Arbitrary[Int => Symbol] =
    Arbitrary.arbFunction1[Int, Symbol]
}
