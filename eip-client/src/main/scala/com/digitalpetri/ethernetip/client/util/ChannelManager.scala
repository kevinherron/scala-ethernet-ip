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

package com.digitalpetri.ethernetip.client.util

import com.digitalpetri.ethernetip.client.{EtherNetIpClientConfig, EtherNetIpClient}
import com.digitalpetri.ethernetip.encapsulation.layers.{DispatchLayer, PacketLayer}
import io.netty.bootstrap.Bootstrap
import io.netty.channel._
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import scala.concurrent.{ExecutionContext, Promise}

class ChannelManager(client: EtherNetIpClient, config: EtherNetIpClientConfig) extends AbstractChannelManager {

  implicit val executionContext: ExecutionContext = ExecutionContext.global // TODO

  /**
   * Make a connection, completing the Promise with the resulting Channel.
   */
  override def connect(channelPromise: Promise[Channel]): Unit = {
    val bootstrap = new Bootstrap

    val initializer = new ChannelInitializer[SocketChannel] {
      def initChannel(channel: SocketChannel) {
        channel.pipeline.addLast(new LoggingHandler(s"${classOf[EtherNetIpClient]}.ByteLogger", LogLevel.TRACE))
        channel.pipeline.addLast(new PacketLayer)
        channel.pipeline.addLast(new DispatchLayer(client))
      }
    }

    bootstrap.group(config.eventLoop)
      .channel(classOf[NioSocketChannel])
      .handler(initializer)

    bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Int.box(config.timeout.toMillis.toInt))

    bootstrap.connect(config.host, config.port).addListener(new ChannelFutureListener {
      def operationComplete(future: ChannelFuture): Unit = {
        if (future.isSuccess) {
          channelPromise.success(future.channel())
        } else {
          channelPromise.failure(future.cause())
        }
      }
    })
  }

}
