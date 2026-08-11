package com.lukewassink.runner.core

import com.lukewassink.runner.config.{SyntaxTree, Transformer, Validator}
import com.lukewassink.runner.util.{Failure, Result, Success}
import io.github.edadma.hocon.{Config, Hocon, HoconException}

object Runner:
  def run(configString: String): Result[Simulation] = parse(configString)
    .flatMap(SyntaxTree.fromConfig).flatMap(Validator.validate)
    .flatMap(Transformer.transform)

  private def parse(configString: String): Result[Config] =
    try Success(Hocon.parse(configString))
    catch { case e: HoconException => Failure(e) }
