package com.rackspace.kafka.http

import java.util.Properties
import java.util.Random

import javax.servlet.http.HttpServlet

import joptsimple._
import kafka.utils._
import kafka.serializer._
import kafka.consumer._
import kafka.message._

import org.apache.log4j.Logger

object Configurator {
  val logger = Logger.getLogger("kafka.http.config")

  def getServlet(args:Array[String]):HttpServlet = {
    if(args.length < 1) {
      throw new Exception("Provide first parameter as 'consumer' or 'producer'")
    }

    var endpoint = args(0)
    if(endpoint == "consumer") {
      return getConsumerServlet(args)
    } else if (endpoint == "producer") {
      return getProducerServlet(args)
    } else {
      throw new Exception("Provide first parameter as 'consumer' or 'producer'")
    }
  }

  def getConsumerServlet(args:Array[String]):HttpServlet = {
    val parser = new OptionParser

    val topic = parser.accepts(
      "topic",
      "The topic to consume from")
      .withRequiredArg
      .describedAs("topic")
      .ofType(classOf[String])

    val group = parser.accepts(
      "group",
      "The group id of this consumer")
      .withRequiredArg
      .describedAs("gid")
      .defaultsTo("rest-" + new Random().nextInt(100000))
      .ofType(classOf[String])

    val zookeeper = parser.accepts(
      "zookeeper",
      "Comma separated of zookeeper nodes")
      .withRequiredArg
      .describedAs("urls")
      .ofType(classOf[String])

    val socketBufferSize = parser.accepts(
      "socket-buffer-size",
      "The size of the tcp RECV size.")
      .withRequiredArg
      .describedAs("size")
      .ofType(classOf[java.lang.Integer])
      .defaultsTo(2 * 1024 * 1024)

    val socketTimeoutMs = parser.accepts(
      "socket-timeout-ms",
      "The socket timeout used for the connection to the broker")
      .withRequiredArg
      .describedAs("ms")
      .ofType(classOf[java.lang.Integer])
      .defaultsTo(ConsumerConfig.SocketTimeout)

    val minFetchBytes = parser.accepts(
      "min-fetch-bytes",
      "The min number of bytes each fetch request waits for.")
      .withRequiredArg
      .describedAs("bytes")
      .ofType(classOf[java.lang.Integer])
      .defaultsTo(1)

    val maxWaitMs = parser.accepts(
      "max-wait-ms",
      "The max amount of time each fetch request waits.")
      .withRequiredArg
      .describedAs("ms")
      .ofType(classOf[java.lang.Integer])
      .defaultsTo(100)

    val autoCommit = parser.accepts(
      "autocommit",
      "If set offsets will be commited automatically during consuming")

    val autoCommitInterval = parser.accepts(
      "autocommit-interval-ms",
      "The time interval at which to save the current offset in ms")
      .withRequiredArg
      .describedAs("ms")
      .ofType(classOf[java.lang.Integer])
      .defaultsTo(ConsumerConfig.AutoCommitInterval)

    val consumerTimeoutMs = parser.accepts(
      "consumer-timeout-ms",
      "consumer throws timeout exception after waiting this much of time without incoming messages")
      .withRequiredArg
      .describedAs("prop")
      .ofType(classOf[java.lang.Integer])
      .defaultsTo(5000)

    val resetBeginning = parser.accepts(
      "from-beginning",
      "If the consumer does not already have an established offset to consume from, " +
      "start with the earliest message present in the log rather than the latest message.")

    val statsdHost = parser.accepts(
      "statsd-host",
      "Statsd host")
      .withRequiredArg
      .ofType(classOf[java.lang.String])
      .defaultsTo("localhost")

    val statsdPrefix = parser.accepts(
      "statsd-prefix",
      "Statsd prefix")
      .withRequiredArg
      .ofType(classOf[java.lang.String])
      .defaultsTo("kafka.http.consumer")

    val statsdPort = parser.accepts(
      "statsd-port",
      "Statsd port")
      .withRequiredArg
      .ofType(classOf[java.lang.Integer])
      .defaultsTo(8125)

    val options: OptionSet = tryParse(parser, args)
    CommandLineUtils.checkRequiredArgs(parser, options, topic, group, zookeeper)

    val props = new Properties()

    props.put("zookeeper.connect", options.valueOf(zookeeper).toString)
    props.put("zookeeper.session.timeout.ms", "400")
    props.put("zookeeper.sync.time.ms", "200")

    props.put("group.id", options.valueOf(group).toString)

    props.put("socket.receive.buffer.bytes", options.valueOf(socketBufferSize).toString)
    props.put("socket.timeout.ms", options.valueOf(socketTimeoutMs).toString)

    props.put("auto.commit.enable", if(options.has(autoCommit)) "true" else "false")
    props.put("auto.commit.interval.ms", options.valueOf(autoCommitInterval).toString)
    props.put("auto.offset.reset", if(options.has(resetBeginning)) "smallest" else "largest")

    props.put("fetch.min.bytes", options.valueOf(minFetchBytes).toString)
    props.put("fetch.wait.max.ms", options.valueOf(maxWaitMs).toString)

    props.put("consumer.timeout.ms", options.valueOf(consumerTimeoutMs).toString)

    logger.info("Consumer Properties: %s".format(props.toString))

    val reportingProps = new Properties()

    reportingProps.put("statsd.host", options.valueOf(statsdHost).toString)
    reportingProps.put("statsd.port", options.valueOf(statsdPort).toString)
    reportingProps.put("statsd.prefix", options.valueOf(statsdPrefix).toString)

    logger.info("Reporting Properties: %s".format(reportingProps.toString))

    return new ConsumerServlet(options.valueOf(topic).toString, props, reportingProps)
  }

  def getProducerServlet(args:Array[String]):HttpServlet = {
    val parser = new OptionParser

    val brokers = parser.accepts(
      "broker-list", "Comma separated of kafka nodes")
      .withRequiredArg
      .describedAs("brokers")
      .ofType(classOf[String])

    val compress = parser.accepts(
      "compress",
      "If set, messages batches are sent compressed")

    val sync = parser.accepts(
      "sync",
      "If set message send requests to the brokers are synchronously, one at a time as they arrive.")

    val requestRequiredAcks = parser.accepts(
      "request-required-acks",
      "The required acks of the producer requests")
      .withRequiredArg
      .describedAs("request required acks")
      .ofType(classOf[java.lang.Integer])
      .defaultsTo(0)

    val requestTimeoutMs = parser.accepts(
      "request-timeout-ms",
      "The ack timeout of the producer requests. Value must be non-negative and non-zero")
      .withRequiredArg
      .describedAs("request timeout ms")
      .ofType(classOf[java.lang.Integer])
      .defaultsTo(1500)

    val socketBufferSize = parser.accepts(
      "socket-buffer-size",
      "The size of the tcp RECV size.")
      .withRequiredArg
      .describedAs("size")
      .ofType(classOf[java.lang.Integer])
      .defaultsTo(1024*100)

    val statsdHost = parser.accepts(
      "statsd-host",
      "Statsd host")
      .withRequiredArg
      .ofType(classOf[java.lang.String])
      .defaultsTo("localhost")

    val statsdPrefix = parser.accepts(
      "statsd-prefix",
      "Statsd prefix")
      .withRequiredArg
      .ofType(classOf[java.lang.String])
      .defaultsTo("kafka.http.producer")

    val statsdPort = parser.accepts(
      "statsd-port",
      "Statsd port")
      .withRequiredArg
      .ofType(classOf[java.lang.Integer])
      .defaultsTo(8125)

    val options: OptionSet = tryParse(parser, args)
    CommandLineUtils.checkRequiredArgs(parser, options, brokers)

    val props = new Properties()

    props.put("metadata.broker.list", options.valueOf(brokers).toString)
    props.put("compression.codec", (if(options.has(compress)) DefaultCompressionCodec.codec else NoCompressionCodec.codec).toString)
    props.put("producer.type", if(options.has(sync)) "sync" else "async")
    props.put("request.required.acks", options.valueOf(requestRequiredAcks).toString)
    props.put("request.timeout.ms", options.valueOf(requestTimeoutMs).toString)
    props.put("key.serializer.class", classOf[StringEncoder].getName)
    props.put("serializer.class", classOf[DefaultEncoder].getName)
    props.put("send.buffer.bytes", options.valueOf(socketBufferSize).toString)

    logger.info("Producer Properties: %s".format(props.toString))

    val reportingProps = new Properties()

    reportingProps.put("statsd.host", options.valueOf(statsdHost).toString)
    reportingProps.put("statsd.port", options.valueOf(statsdPort).toString)
    reportingProps.put("statsd.prefix", options.valueOf(statsdPrefix).toString)

    logger.info("Reporting Properties: %s".format(reportingProps.toString))

    return new ProducerServlet(props, reportingProps)
  }

  def tryParse(parser: OptionParser, args: Array[String]) = {
    try {
      parser.parse(args : _*)
    } catch {
      case e: OptionException => {
        Utils.croak(e.getMessage)
        null
      }
    }
  }
}
