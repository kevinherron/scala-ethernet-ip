/*
 * EtherNet/IP
 * Copyright (C) 2014 Kevin Herron
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.digitalpetri.ethernetip.client

import com.digitalpetri.ethernetip.cip.CipClassCodes
import com.digitalpetri.ethernetip.cip.epath._
import com.digitalpetri.ethernetip.cip.services.GetAttributeList.AttributeRequest
import com.digitalpetri.ethernetip.client.cip.CipClient
import com.digitalpetri.ethernetip.client.cip.services.{GetAttributeSingleService, GetAttributeListService}
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import scala.util.Failure
import scala.util.Success

object EtherNetIpClientMain extends App {

  implicit val ec = ExecutionContext.global

  val config = new EtherNetIpClientConfig(
    hostname = "10.20.4.57",
    vendorId = 0,
    serialNumber = 0,
    concurrency = 2,
    connectionPath = PaddedEPath(PortSegment(1, Array[Byte](0))),
    connectionTimeout = Duration(15, TimeUnit.SECONDS))

  val client = new CipClient(config)

  val future = for {
    identity    <- client.listIdentity()
    services    <- client.listServices()
    interfaces  <- client.listInterfaces()
  } yield {
    println(s"identity=$identity")
    println(s"services=$services")
    println(s"interfaces=$interfaces")
  }

  future.onComplete {
    case Success(_) => for (i <- 0 to 1) testGetAttributeSingle()
    case Failure(ex) => println("Error: " + ex)
  }

//  future.onComplete {
//    case _ =>
//      client.unRegisterSession()
//      EtherNetIp.shutdown()
//  }

  def testGetAttributeList() {
    val service = new GetAttributeListService(
      attributes      = Seq(1),
      attributeSizes  = Seq(2),
      requestPath     = PaddedEPath(ClassId(CipClassCodes.MessageRouterObject), InstanceId(0x01)))

    val serviceFuture = client.invokeService(service)

    serviceFuture.onComplete {
      case Success(response) => println(response)
      case Failure(ex) => println(ex)
    }
  }

  def testGetAttributeSingle() {
    val requestPath = PaddedEPath(
      ClassId(CipClassCodes.MessageRouterObject),
      InstanceId(0x01),
      AttributeId(1))

    val service = new GetAttributeSingleService(requestPath)

    val serviceFuture = client.invokeService(service, connected = true)

    serviceFuture.onComplete {
      case Success(response) =>
        val data = response.attributeData
        val count = data.readUnsignedShort()
        println(s"class count: $count")

//        for {i <- 0 until count} yield println(f"class code: 0x${data.readUnsignedShort()}%02X")

      case Failure(ex) => println("GAS error: " + ex)
    }
  }

}
