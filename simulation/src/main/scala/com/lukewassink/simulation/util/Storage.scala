package com.lukewassink.simulation.util

import scala.reflect.ClassTag

// Stores at most a single instance of any subtype of T.
// NOTE: because of type erasure, Storage can not see type parameters on subtypes of T,
// so if you set a value of type A[X], it will overwrite a value of type A[Y] even if
// x != y.
case class Storage[T](private val store: Map[Class[?], T]) {
  def withValue[A <: T](a: A): Storage[T] = Storage(store.updated(a.getClass, a))

  def contains[A <: T](using ct: ClassTag[A]): Boolean = store
    .contains(ct.runtimeClass)

  def get[A <: T](using ct: ClassTag[A]): Option[A] =
    if contains[A] then Some(store(ct.runtimeClass).asInstanceOf[A]) else None

  def without[A <: T](using ct: ClassTag[A]): Storage[T] = Storage(
    store.removed(ct.runtimeClass)
  )

  // A very flexible update method. You can insert the value of type A
  // if it isn't already present. Return None to delete it.
  def update[A <: T](using
      ct: ClassTag[A]
  )(f: Option[A] => Option[A]): Storage[T] =
    val updated = f(get[A])
    updated match {
      case None       => this.without[A]
      case Some(a: A) => this.withValue(a)
    }

  // Updates the value of A if it is present, otherwise does nothing.
  // Not as flexible as update, but more convenient when it does what you want.
  def map[A <: T](using ct: ClassTag[A])(f: A => A): Storage[T] =
    get[A] match {
      case None    => this
      case Some(a) => this.withValue(f(a))
    }
}

object Storage:
  def empty[T]: Storage[T] = Storage[T](Map.empty[Class[?], T])
