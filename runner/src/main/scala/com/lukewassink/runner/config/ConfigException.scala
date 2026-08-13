package com.lukewassink.runner.config

sealed class ConfigException(message: String) extends RuntimeException(message)

final class DuplicateNodeNamesException(name: String, indices: List[Int])
    extends ConfigException(
      s"Nodes at indices ${indices.mkString(", ")} use the name $name. Node names must be unique."
    )

case class MissingReferenceNameException(referenceLocation: ReferenceLocation)
    extends ConfigException(
      s"There is no node with name ${referenceLocation.nodeName}, as referenced at ${referenceLocation.location.prettyPrint}."
    )

case class MissingBehaviorTypeException(behaviorType: String)
    extends ConfigException(s"Behavior type $behaviorType does not exist.")

case class MissingInterceptorTypeException(interceptorType: String)
    extends ConfigException(s"Interceptor type $interceptorType does not exist.")

case class MissingDistributionTypeException(distributionType: String)
    extends ConfigException(
      s"Distribution type $distributionType does note exist."
    )

case class InvalidChanceException(path: String, e: IllegalArgumentException)
    extends ConfigException(
      s"The Double value at $path can not be parsed as a Chance: ${e.getMessage}"
    )
