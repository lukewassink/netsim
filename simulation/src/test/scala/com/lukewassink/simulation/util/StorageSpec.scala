package com.lukewassink.simulation.util

import com.lukewassink.simulation.test_utils.UnitSpec

class StorageSpec extends UnitSpec {
  enum TestData:
    case DataTypeOne(i: Int)
    case DataTypeTwo(i: Int)

  import TestData.*

  private val storage = Store.empty[TestData]

  describe("withValue") {
    val storage2 = storage.withValue(DataTypeOne(1)).withValue(DataTypeTwo(0))

    it("adds a value to the storage") {
      assert(storage.get[DataTypeOne] === None)
      assert(storage.get[DataTypeTwo] === None)

      assert(storage2.get[DataTypeOne] === Some(DataTypeOne(1)))
      assert(storage2.get[DataTypeTwo] === Some(DataTypeTwo(0)))
    }

    it("overwrites the previous value of the same type") {
      val storage3 = storage2.withValue(DataTypeOne(3))
      assert(storage3.get[DataTypeOne] === Some(DataTypeOne(3)))
      assert(storage3.get[DataTypeTwo] === Some(DataTypeTwo(0)))
    }
  }

  describe("get") {
    val storage2 = storage.withValue(DataTypeOne(1))

    it("returns the value of the requested type") {
      assert(storage2.get[DataTypeOne] === Some(DataTypeOne(1)))
    }

    it("returns None if the type is not present") {
      assert(storage2.get[DataTypeTwo] === None)
    }
  }

  describe("getOrElse") {
    val storage2 = storage.withValue(DataTypeOne(1))

    it("returns the value of the requested type") {
      assert(storage2.getOrElse(DataTypeOne(0)) === DataTypeOne(0))
    }

    it("returns the fallback if the type is not present") {
      assert(storage2.getOrElse(DataTypeTwo(0)) === DataTypeTwo(0))
    }
  }

  describe("without") {
    val storage2 = storage.withValue(DataTypeOne(1))
    it("removes the value of the requested type") {
      assert(storage2.get[DataTypeOne] === Some(DataTypeOne(1)))
      assert(storage2.without[DataTypeOne].get[DataTypeOne] === None)
    }

    it("does nothing when no value of the requested type was present") {
      assert(storage2.get[DataTypeTwo] === None)
      assert(storage2.without[DataTypeTwo].get[DataTypeTwo] === None)
    }
  }

  describe("update") {
    val storage2 = storage.withValue(DataTypeOne(1))
    it("replaces the value of the requested type with a modified version") {
      assert(storage2.get[DataTypeOne] === Some(DataTypeOne(1)))
      assert(storage2.update[DataTypeOne] {
        case None    => None
        case Some(d) => Some(DataTypeOne(2 * d.i))
      }.get[DataTypeOne] === Some(DataTypeOne(2)))
    }

    it("adds the value if it was not present") {
      assert(storage2.get[DataTypeTwo] === None)
      assert(storage2.update[DataTypeTwo] {
        case None    => Some(DataTypeTwo(1))
        case Some(d) => Some(d)
      }.get[DataTypeTwo] === Some(DataTypeTwo(1)))
    }

    it("deletes the value if f returns None") {
      assert(storage2.get[DataTypeOne] === Some(DataTypeOne(1)))
      assert(storage2.update[DataTypeOne] {
        case None    => None
        case Some(_) => None
      }.get[DataTypeOne] === None)
    }
  }

  describe("contains") {
    val storage2 = storage.withValue(DataTypeOne(1))
    it("returns true if Storage contains a value with the type") {
      assert(storage2.contains[DataTypeOne])
    }

    it("returns false if Storage does not contain a value with the type") {
      assert(!storage2.contains[DataTypeTwo])
    }
  }

  describe("map") {
    val storage2 = storage.withValue(DataTypeOne(1))
    it("transforms the value if its present") {
      assert(
        storage2.map[DataTypeOne](x => x.copy(i = x.i * 2)).get[DataTypeOne] ===
          Some(DataTypeOne(2))
      )
    }

    it("does nothing if it isn't") {
      assert(
        storage2.map[DataTypeTwo](x => DataTypeTwo(x.i * 2))
          .get[DataTypeTwo] === None
      )
    }
  }

  describe("list constructor") {
    it("adds the elements to the store") {
      val storage2 = Store[TestData](DataTypeOne(1), DataTypeTwo(2))
      assert(storage2.get[DataTypeOne] === Some(DataTypeOne(1)))
      assert(storage2.get[DataTypeTwo] === Some(DataTypeTwo(2)))
    }
  }
}
