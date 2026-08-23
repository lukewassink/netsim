package com.lukewassink.simulation.util

import scala.reflect.ClassTag

// Stores at most a single instance of any subtype of T.
//
// NOTE: because of type erasure, Storage can not see type parameters on subtypes of T,
// so if you set a value of type A[X], it will overwrite a value of type A[Y] even if
// x != y.
case class Store[T](private val store: Map[Class[?], T]) {
  def withValue[A <: T](a: A): Store[T] = Store(store.updated(a.getClass, a))

  def contains[A <: T](using ct: ClassTag[A]): Boolean = store
    .contains(ct.runtimeClass)

  def get[A <: T](using ct: ClassTag[A]): Option[A] =
    if contains[A] then Some(store(ct.runtimeClass).asInstanceOf[A]) else None

  def getOrElse[A <: T](using ct: ClassTag[A])(fallback: A): A =
    if contains[A] then store(ct.runtimeClass).asInstanceOf[A] else fallback

  def without[A <: T](using ct: ClassTag[A]): Store[T] = Store(
    store.removed(ct.runtimeClass)
  )

  // A flexible update method. You can insert the value of type A
  // if it isn't already present. Return None to delete it.
  def update[A <: T](using
      ct: ClassTag[A]
  )(f: Option[A] => Option[A]): Store[T] =
    val updated = f(get[A])
    updated match {
      case None       => this.without[A]
      case Some(a: A) => this.withValue(a)
    }

  // Updates the value of A if it is present, otherwise does nothing.
  // Not as flexible as update, but more convenient when it does what you want.
  def map[A <: T](using ct: ClassTag[A])(f: A => A): Store[T] =
    get[A] match {
      case None    => this
      case Some(a) => this.withValue(f(a))
    }
}

object Store:
  def empty[T]: Store[T] = Store[T](Map.empty[Class[?], T])

  def apply[T](elements: List[T]): Store[T] =
    elements.foldLeft(empty[T])((s, e) => s.withValue(e))

  def apply[T](elements: T*): Store[T] = this(elements.toList)
