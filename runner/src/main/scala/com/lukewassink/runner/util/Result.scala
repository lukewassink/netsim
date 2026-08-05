package com.lukewassink.runner.util

sealed trait Result[T]

final case class Success[T](value: T) extends Result[T]

final case class Failure(errors: List[Error]) extends Result:
  def messages: String = errors
    .map(error => error.getClass.toString + ": " + error.message + "\n")
    .foldLeft("")(_ + _)

// Maybe add line no. if that's possible
sealed trait Error:
  def message: String

final case class MissingField(field: String, requiredBy: String) extends Error:
  def message: String =
    s"missing the field ($field), which is required by ($requiredBy)"
