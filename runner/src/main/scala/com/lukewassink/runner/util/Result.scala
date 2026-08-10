package com.lukewassink.runner.util

sealed trait Result[T]:
  def flatMap[S](f: T => Result[S]): Result[S]

  def map[S](f: T => S): Result[S]

  def isSuccess: Boolean

  def isFailure: Boolean = !isSuccess

  def getOrElse(default: T): T

final case class Success[T](value: T) extends Result[T]:
  def flatMap[S](f: T => Result[S]): Result[S] = f(value)

  def map[S](f: T => S): Success[S] = Success(f(value))

  override def isSuccess: Boolean = true

  override def getOrElse(default: T): T = value

final case class Failure[T](errors: List[Throwable]) extends Result[T]:
  override def toString: String = errors
    .map(error => s"${error.getClass.getSimpleName}: ${error.getMessage} \n")
    .mkString("\n")

  def flatMap[S](f: T => Result[S]): Result[S] = Failure[S](errors)

  def map[S](f: T => S): Failure[S] = Failure[S](errors)

  override def isSuccess: Boolean = false

  override def getOrElse(default: T): T = default

case object Failure:
  def apply[T](errors: Throwable*): Failure[T] = Failure(errors.toList)
