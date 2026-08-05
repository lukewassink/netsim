package com.lukewassink.runner.util

sealed trait Result[T]

final case class Success[T](value: T) extends Result[T]

final case class Failure[T](errors: List[Throwable]) extends Result[T]:
  def messages: String = errors
    .map(error => error.getClass.toString + ": " + error.getMessage + "\n")
    .foldLeft("")(_ + _)

case object Failure:
  def apply[T](errors: Throwable*): Failure[T] = Failure(errors.toList)
