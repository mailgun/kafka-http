package com.rackspace.kafka.http

import scala.collection.JavaConversions._

import java.io.IOException
import java.util.Properties
import java.util.HashMap

import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.apache.log4j.Logger;

import kafka.consumer._
import kafka.message._
import kafka.serializer._

import org.bson.BSONObject
import org.bson.BasicBSONDecoder
import org.bson.BasicBSONEncoder
import org.bson.BasicBSONObject
import org.bson.types.BasicBSONList
import com.mongodb.util.JSON

class ConsumerServlet(topic:String, properties: Properties, reportingProps: Properties) extends HttpServlet with ReplyFormatter
{
  val consumer = Consumer.create(new ConsumerConfig(properties))
  val logger = Logger.getLogger("kafka.rest.consumer")
  val stream  = consumer.createMessageStreamsByFilter(
    new Whitelist(topic), 1, new StringDecoder(), new DefaultDecoder()).get(0)

  override def doPost(request:HttpServletRequest, response:HttpServletResponse)
  {
    logger.info("Started commiting offsets")
    this.synchronized {
      consumer.commitOffsets
    }
    logger.info("Done commiting offsets")

    var obj = new BasicBSONObject()
    obj.append("commited", "OK")
    replyWith(obj, request, response)
  }

  def getBatchSize(request:HttpServletRequest):Int = {
    val batchSizeParam = request.getParameter("batchSize")
    if(batchSizeParam == null) {
      return 1
    }

    var batchSize = 0
    try {
      batchSize = batchSizeParam.toInt
    } catch {
      case _:Exception => throw new Exception("Parameter 'batchSize' should be int")
    }

    if(batchSize <=0) {
      throw new Exception("Parameter 'batchSize' should be > 0")
    } else if (batchSize > 1000) {
      throw new Exception("Parameter 'batchSize' should be < 1000")
    }
    batchSize
  }

  def replyWithMessages(messages:BasicBSONList, request:HttpServletRequest, response:HttpServletResponse) {
    var responseObj = new BasicBSONObject()
    responseObj.append("messages", messages)
    replyWith(responseObj, request, response)
  }

  override def doGet(request:HttpServletRequest, response:HttpServletResponse)
  {
    val batchSize = getBatchSize(request)
    this.synchronized {
      logger.info("Initiated get, batchSize: %s".format(batchSize))
      var messages = new BasicBSONList()
      try {
        for (item <- stream) {
          var key = new String(if (item.key == null) "" else item.key)
          var message = new BasicBSONDecoder().readObject(item.message)
          var obj = new BasicBSONObject()
          obj.append("key", key)
          obj.append("value", message)
          messages.append(obj)
          if(messages.size()  >= batchSize) {
            logger.info("Collected batch size objects")
            replyWithMessages(messages, request, response)
            return
          }
        }
      } catch {
        case e:kafka.consumer.ConsumerTimeoutException => {
          logger.info("Consumer timeout, returning stuff that I have collected")
          replyWithMessages(messages, request, response)
        }
      }
    }
  }
}
