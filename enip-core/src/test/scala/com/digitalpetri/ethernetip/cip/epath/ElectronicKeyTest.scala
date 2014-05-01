package com.digitalpetri.ethernetip.cip.epath

import com.digitalpetri.ethernetip.cip.epath.LogicalSegment.ElectronicKey
import org.scalatest.FunSuite

class ElectronicKeyTest extends FunSuite {

  test("ElectronicKey is round-trip encodable/decodable") {
    for {
      strictMajor <- Seq(true, false)
    } yield {
      val key = ElectronicKey(42, 1, 13, strictMajor = true, 7, 11)
      val decoded = ElectronicKey.decode(ElectronicKey.encode(key))

      assert(key == decoded)
    }
  }

}
