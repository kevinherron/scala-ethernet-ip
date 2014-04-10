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

import scala.concurrent.ExecutionContext

object EtherNetIpClientMain extends App {

  implicit val ec = ExecutionContext.global

  val config = new EtherNetIpClientConfig("10.20.4.57")
  val client = new EtherNetIpClient(config)

  val future = for {
    identity <- client.listIdentity()
    register <- client.registerSession()
  } yield {
    println(s"identity=$identity")
    println(s"register=$register")
  }

  future.onFailure {
    case ex => println(ex)
  }

  future.onComplete {
    case _ =>
      client.unRegisterSession()
      EtherNetIp.shutdown()
  }

}
