package com.lukewassink.runner.util

sealed trait Result[T]:
  def flatMap(f: T => Result[T]): Result[T]

final case class Success[T](value: T) extends Result[T]:
  def flatMap(f: T => Result[T]): Result[T] = f(value)

final case class Failure[T](errors: List[Throwable]) extends Result[T]:
  def messagesToString: String = errors
    .map(error => s"${error.getClass.getSimpleName}: ${error.getMessage} \n")
    .foldLeft("")(_ + _)

  def flatMap(f: T => Result[T]): Result[T] = this

case object Failure:
  def apply[T](errors: Throwable*): Failure[T] = Failure(errors.toList)
