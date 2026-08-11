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
    extends ConfigException(
      s"Behavior type $behaviorType is not a valid behavior type"
    )
