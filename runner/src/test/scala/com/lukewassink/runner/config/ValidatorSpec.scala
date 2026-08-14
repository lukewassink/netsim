package com.lukewassink.runner.config

import com.lukewassink.runner.config.BehaviorNode.{
  SimpleResponderNode, SimpleSenderNode
}
import com.lukewassink.runner.config.DistributionNode.UniformDistributionNode
import com.lukewassink.runner.config.InterceptorNode.{
  MessageDropInterceptorNode, RandomLatencyInterceptorNode
}
import com.lukewassink.runner.config.Validator.validate
import com.lukewassink.runner.config.{
  DuplicateNodeNamesException, MissingReferenceNameException
}
import com.lukewassink.simulation.test_utils.UnitSpec
import com.lukewassink.runner.util.{Failure, Success}

class ValidatorSpec extends UnitSpec {
  describe("validate") {
    it("handles a valid syntax tree") {
      val validTree = SimulationNode(
        "simulation-name",
        10,
        1,
        List.empty,
        List(
          NodeNode("node-name-1", List.empty),
          NodeNode("node-name-2", List(SimpleResponderNode())),
          NodeNode(
            "node-name-3",
            List(
              SimpleResponderNode(),
              SimpleResponderNode(),
              SimpleSenderNode(10, "node-name-2", "Hi!")
            )
          )
        )
      )

      assert(validate(validTree) === Success(validTree))
    }

    it("catches duplicate node names") {
      val tree = SimulationNode(
        "simulation-name",
        10,
        1,
        List.empty,
        List(
          NodeNode("node-name-1", List.empty),
          NodeNode("node-name-1", List(SimpleResponderNode())),
          NodeNode(
            "node-name-3",
            List(
              SimpleResponderNode(),
              SimpleResponderNode(),
              SimpleSenderNode(10, "node-name-1", "Hi!")
            )
          )
        )
      )

      val result = validate(tree)

      inside(result) { case Failure(List(e)) =>
        e shouldBe a[DuplicateNodeNamesException]
      }
    }

    it("catches missing references") {
      val tree = SimulationNode(
        "simulation-name",
        10,
        1,
        List.empty,
        List(
          NodeNode("node-name-1", List.empty),
          NodeNode("node-name-2", List(SimpleResponderNode())),
          NodeNode(
            "node-name-3",
            List(
              SimpleResponderNode(),
              SimpleResponderNode(),
              SimpleSenderNode(10, "node-name-5", "Hi!")
            )
          )
        )
      )

      val result = validate(tree)

      inside(result) { case Failure(List(e)) =>
        e shouldBe a[MissingReferenceNameException]
      }
    }

    it("returns a list of all validation errors") {
      val tree = SimulationNode(
        "simulation-name",
        10,
        1,
        List.empty,
        List(
          NodeNode("node-name-1", List.empty),
          NodeNode(
            "node-name-1",
            List(
              SimpleResponderNode(),
              SimpleSenderNode(10, "node-name-12", "Hi!")
            )
          ),
          NodeNode(
            "node-name-3",
            List(
              SimpleResponderNode(),
              SimpleResponderNode(),
              SimpleSenderNode(10, "node-name-5", "Hi!")
            )
          ),
          NodeNode("node-name-3", List.empty),
          NodeNode("node-name-3", List.empty)
        )
      )

      val result = validate(tree)

      inside(result) { case Failure(List(e1, e2, e3, e4)) =>
        e1 shouldBe a[DuplicateNodeNamesException]
        e2 shouldBe a[DuplicateNodeNamesException]
        e3 shouldBe a[MissingReferenceNameException]
        e4 shouldBe a[MissingReferenceNameException]
      }
    }

    it("catches negative ticksPerMillisecond") {
      val tree = SimulationNode(
        "simulation-name",
        10,
        -1,
        List.empty,
        List.empty
      )

      val result = validate(tree)

      inside(result) { case Failure(List(e)) =>
        e shouldBe a[IllegalConfigValueException]
      }
    }

    it("catches invalid chances") {
      val tree = SimulationNode(
        "simulation-name",
        10,
        1,
        List(
          MessageDropInterceptorNode(-1),
          MessageDropInterceptorNode(0),
          MessageDropInterceptorNode(0.5),
          MessageDropInterceptorNode(1),
          MessageDropInterceptorNode(5)
        ),
        List.empty
      )

      val result = validate(tree)

      inside(result) { case Failure(List(e1, e2, e3, e4)) =>
        e1 shouldBe a[IllegalConfigValueException]
        e2 shouldBe a[IllegalConfigValueException]
        e3 shouldBe a[IllegalConfigValueException]
        e4 shouldBe a[IllegalConfigValueException]
      }
    }

    it("catches invalid ranges") {
      val tree = SimulationNode(
        "simulation-name",
        10,
        1,
        List(RandomLatencyInterceptorNode(UniformDistributionNode(5, 3))),
        List.empty
      )

      val result = validate(tree)

      inside(result) { case Failure(List(e)) =>
        e shouldBe a[IllegalConfigValueException]
      }
    }
  }
}
