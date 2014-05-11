/*
 * Copyright 2014 Kevin Herron
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
