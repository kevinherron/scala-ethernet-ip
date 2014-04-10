package com.digitalpetri.ethernetip.cip

import scala.concurrent.duration.Duration


case class CipConnection(o2tConnectionId: Long,
                         t2oConnectionId: Long,
                         serialNumber: Int,
                         originatorVendorId: Int,
                         originatorSerialNumber: Long,
                         timeout: Duration)
