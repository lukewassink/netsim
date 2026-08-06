package com.lukewassink.runner.config

sealed class ConfigValidationException(message: String)
    extends RuntimeException(message)

final class DuplicateNodeNamesException(name: String, indices: List[Int])
    extends ConfigValidationException(
      s"Nodes at indices $indices use the name: $name. Node names must be unique."
    )

case class MissingReferenceNameException(referenceLocation: ReferenceLocation)
    extends ConfigValidationException(
      s"There is no node with name ${referenceLocation.nodeName}, as referenced at ${referenceLocation.location.prettyPrint}."
    )
