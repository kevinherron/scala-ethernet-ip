package com.digitalpetri.ethernetip.client.cip

import scala.concurrent.duration.Duration


case class CipConnection(o2tConnectionId: Int,
                         t2oConnectionId: Int,
                         serialNumber: Int,
                         originatorVendorId: Int,
                         originatorSerialNumber: Long,
                         timeout: Duration) {

  /** If/when this connection times out valid will be `false`. */
  @volatile var valid = true

}
