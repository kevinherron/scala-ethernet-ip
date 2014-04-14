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
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{FiniteDuration, Duration}

case class EtherNetIpClientConfig(host: String,
                                  port: Int = 44818,
                                  connections: Int = 2,
                                  connectionPath: PaddedEPath,
                                  vendorId: Int,
                                  serialNumber: Int,
                                  timeout: Duration = FiniteDuration(5, TimeUnit.SECONDS),
                                  executionContext: ExecutionContext = ExecutionContext.global,
                                  eventLoop: EventLoopGroup = EtherNetIp.SharedEventLoop,
                                  wheelTimer: HashedWheelTimer = EtherNetIp.SharedWheelTimer)
