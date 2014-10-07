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

import java.util.concurrent.TimeUnit

import com.digitalpetri.ethernetip.cip.CipClassCodes
import com.digitalpetri.ethernetip.cip.epath._
import com.digitalpetri.ethernetip.cip.services.ForwardOpen
import com.digitalpetri.ethernetip.cip.services.ForwardOpen.NetworkConnectionParameters
import com.digitalpetri.ethernetip.cip.services.ForwardOpen.NetworkConnectionParameters.ConnectionType.PointToPoint
import com.digitalpetri.ethernetip.cip.services.ForwardOpen.NetworkConnectionParameters.Priority.Low
import com.digitalpetri.ethernetip.cip.services.ForwardOpen.NetworkConnectionParameters.Size.VariableSize
import com.digitalpetri.ethernetip.cip.services.LargeForwardOpen.LargeForwardOpenRequest
import com.digitalpetri.ethernetip.client.cip.services.{GetAttributeListService, GetAttributeSingleService, LargeForwardOpenService}
import com.digitalpetri.ethernetip.client.cip.{CipClient, CipConnection, CipConnectionManager}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import scala.util.{Failure, Random, Success}

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
  val connectionManager = new CipConnectionManager(client, 500)

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
    case Success(_) => testLargeForwardOpen()
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

    val serviceFuture = client.invokeService(service)(None)

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

    val serviceFuture = client.invokeService(service)(None)

    serviceFuture.onComplete {
      case Success(response) =>
        val data = response.attributeData
        val count = data.readUnsignedShort()
        println(s"class count: $count")

//        for {i <- 0 until count} yield println(f"class code: 0x${data.readUnsignedShort()}%02X")

      case Failure(ex) => println("GAS error: " + ex)
    }
  }

  def testLargeForwardOpen(): Unit = {
    val segments = config.connectionPath.segments ++ ForwardOpen.MessageRouterConnectionPoint.segments
    val connectionPath = PaddedEPath(segments: _*)

    val parameters = NetworkConnectionParameters(
      connectionSize  = 4000,
      sizeType        = VariableSize,
      priority        = Low,
      connectionType  = PointToPoint,
      redundantOwner  = false)

    val request = LargeForwardOpenRequest(
      timeout                 = config.connectionTimeout,
      connectionSerialNumber  = Random.nextInt(),
      vendorId                = 0,
      vendorSerialNumber      = 0,
      connectionPath          = connectionPath,
      o2tNetworkConnectionParameters = parameters,
      t2oNetworkConnectionParameters = parameters)

    val service = new LargeForwardOpenService(request)

    client.sendUnconnectedData(service.getRequestData).onComplete {
      case Success(responseData) => service.setResponseData(responseData)
      case Failure(ex) => service.setResponseFailure(ex)
    }

    service.response.onComplete {
      case Success(response) =>
        val connection = CipConnection(
          o2tConnectionId         = response.o2tConnectionId,
          t2oConnectionId         = response.t2oConnectionId,
          serialNumber            = response.connectionSerialNumber,
          originatorVendorId      = response.originatorVendorId,
          originatorSerialNumber  = response.originatorSerialNumber,
          timeout                 = config.connectionTimeout)

        println(s"CipConnection allocated: $connection")

      case Failure(ex) =>
        println(s"Failed to open CipConnection: ${ex.getMessage}")
    }
  }

}
