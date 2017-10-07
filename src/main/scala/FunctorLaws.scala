import scala.language.higherKinds

import Algebra.{Tree, Branch, Leaf}
import Algebra.FuncFromIntTo

object FunctorLaws {

  trait Functor[F[_]] {
    def map[A, B]: F[A] => (A => B) => F[B]
  }

  object Functor {
    def apply[F[_]](implicit INST: Functor[F]): Functor[F] = INST
  }

  implicit final class FunctorSyntax[F[_]: Functor, A](fa: F[A]) {
    def map[B]: (A => B) => F[B] =
      f => Functor[F].map(fa)(f)
  }

  sealed trait Laws {
    
    def mapPreservesIdentity[F[_]: Functor, A]: F[A] => Boolean =
      fa => (fa map (a => a)) == fa

    def mapPreservesComposition[F[_]: Functor, A, B, C]: F[A] => (A => B) => (B => C) => Boolean =
      fa => f => g => (fa map (g compose f)) == (fa map f map g)
  }

  sealed trait LawsNoInfix {
  
    def mapPreservesIdentity[F[_], A](implicit FF: Functor[F]): F[A] => Boolean =
      fa => FF.map(fa)(a => a) == fa

    def mapPreservesComposition[F[_], A, B, C](implicit FF: Functor[F]): F[A] => (A => B) => (B => C) => Boolean =
      fa => f => g => FF.map(fa)(g compose f) == FF.map(FF.map(fa)(f))(g)
  }

  object Laws extends Laws
  object LawsNoInfix extends LawsNoInfix

  object FunctorInstances {

    implicit lazy val seqFunctor: Functor[Seq] = 
      new Functor[Seq] {
        def map[A, B]: Seq[A] => (A => B) => Seq[B] =
          fa => f => fa map f
      }

    implicit lazy val optionFunctor: Functor[Option] =
      new Functor[Option] {
        def map[A, B]: Option[A] => (A => B) => Option[B] =
          fa => f => fa map f
      }

    implicit lazy val functionFromIntFunctor: Functor[FuncFromIntTo] =
      new Functor[FuncFromIntTo] {
        def map[A, B]: FuncFromIntTo[A] => (A => B) => FuncFromIntTo[B] =
          fa => f => FuncFromIntTo[B](f compose fa.apply)
      }

    implicit lazy val treeFunctor: Functor[Tree] =
      new Functor[Tree] {
        def map[A, B]: Tree[A] => (A => B) => Tree[B] =
          fa => f => fa match {
            case Branch(l, r) => Branch(map(l)(f), map(r)(f))
            case Leaf(v) => (Leaf.apply[B] _ compose f)(v)
          }
      }
  }
}