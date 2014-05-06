package com.digitalpetri.ethernetip.client.util

import com.codahale.metrics.{Metric, MetricSet}
import scala.collection.JavaConversions._

class ScalaMetricSet(val metrics: Map[String, Metric]) extends MetricSet {

  def getMetrics: java.util.Map[String, Metric] = metrics

}
