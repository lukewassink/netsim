package com.lukewassink.runner.util

sealed trait Result[T]:
  def flatMap[S](f: T => Result[S]): Result[S]

  def map[S](f: T => S): Result[S]

final case class Success[T](value: T) extends Result[T]:
  def flatMap[S](f: T => Result[S]): Result[S] = f(value)

  def map[S](f: T => S): Success[S] = Success(f(value))

final case class Failure[T](errors: List[Throwable]) extends Result[T]:
  override def toString: String = errors
    .map(error => s"${error.getClass.getSimpleName}: ${error.getMessage} \n")
    .mkString("\n")

  def flatMap[S](f: T => Result[S]): Result[S] = Failure[S](errors)

  def map[S](f: T => S): Failure[S] = Failure[S](errors)

case object Failure:
  def apply[T](errors: Throwable*): Failure[T] = Failure(errors.toList)
