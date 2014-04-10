package com.digitalpetri.ethernetip.cip

import io.netty.buffer.ByteBuf
import scala.concurrent.duration.Duration


class UnconnectedSend {

}

case class UnconnectedSendRequest(desiredTimeout: Duration,
                                  embedded: MessageRouterRequest,
                                  connectionPath: EPath)

object UnconnectedSendRequest {

  def encode(request: UnconnectedSendRequest, buffer: ByteBuf): ByteBuf = {
    // TODO
    buffer
  }

  def decode(buffer: ByteBuf): UnconnectedSendRequest = {
    ??? // TODO
  }

}
