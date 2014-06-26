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

import com.digitalpetri.ethernetip.cip.epath.PaddedEPath
import io.netty.channel.EventLoopGroup
import io.netty.util.HashedWheelTimer

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

case class EtherNetIpClientConfig(hostname: String,
                                  port: Int = 44818,
                                  vendorId: Int,
                                  serialNumber: Int,
                                  concurrency: Int = 2,
                                  connectionPath: PaddedEPath,
                                  connectionTimeout: Duration = 15.seconds,
                                  requestTimeout: Duration = 5.seconds,
                                  instanceId: Option[String] = None,
                                  executionContext: ExecutionContext = ExecutionContext.global,
                                  eventLoop: EventLoopGroup = EtherNetIpShared.SharedEventLoop,
                                  wheelTimer: HashedWheelTimer = EtherNetIpShared.SharedWheelTimer)
