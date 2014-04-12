package com.digitalpetri.ethernetip.cip

import scala.concurrent.duration.Duration


case class CipConnection(o2tConnectionId: Int,
                         t2oConnectionId: Int,
                         serialNumber: Int,
                         originatorVendorId: Int,
                         originatorSerialNumber: Long,
                         timeout: Duration)
