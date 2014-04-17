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

package com.digitalpetri.ethernetip.util

import scala.concurrent.duration.Duration

object TimeoutCalculator {

  private val MinTimeout = 1
  private val MaxTimeout = 8355840

  def calculateTimeoutBytes(timeout: Duration): Int = {
    var desiredTimeout = timeout.toMillis.toInt

    if (desiredTimeout < MinTimeout) desiredTimeout = MinTimeout
    if (desiredTimeout > MaxTimeout) desiredTimeout = MaxTimeout

    var precisionLost = false
    var shifts = 0
    var multiplier = desiredTimeout

    while (multiplier > 255) {
      precisionLost |= (multiplier & 1) == 1
      multiplier >>= 1
      shifts += 1
    }

    if (precisionLost) {
      multiplier += 1
      if (multiplier > 255) {
        multiplier >>= 1
        shifts += 1
      }
    }

    assert(shifts <= 15)

    val tick = Math.pow(2, shifts)

    assert(tick >= 1 && tick <= 32768)
    assert(multiplier >= 1 && multiplier <= 255)

    shifts << 8 | multiplier
  }

}
