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

import io.netty.channel.nio.NioEventLoopGroup
import io.netty.util.HashedWheelTimer

object EtherNetIpShared {

  lazy val SharedEventLoop = new NioEventLoopGroup()
  lazy val SharedWheelTimer = new HashedWheelTimer()

  def shutdown() {
    SharedEventLoop.shutdownGracefully()
    SharedWheelTimer.stop()
  }

  Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
    def run(): Unit = shutdown()
  }))

}
