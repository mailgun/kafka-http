package com.rackspace.kafka.http

import scala.collection.JavaConversions._

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.apache.log4j.Logger;

import org.bson.BSONObject
import org.bson.BasicBSONDecoder
import org.bson.BasicBSONEncoder
import org.bson.BasicBSONObject
import com.mongodb.util.JSON

trait ReplyFormatter {
  def getReplyContentType(request:HttpServletRequest):String = {
    val replyType = request.getHeader("Accept") match {
      case "application/json" => "application/json"
      case "application/bson" => "application/bson"
      case "" | "*/*"=> "application/json"
      case _ => throw new Exception("Unsupported content type in Accept: '%s'".format(request.getHeader("Accept")))
    }
    replyType
  }

  def replyWithJson(obj:BSONObject, response:HttpServletResponse, status:Int){
    response.setContentType("application/json")
    response.setStatus(status)
    response.getWriter().print(JSON.serialize(obj))
  }

  def replyWithBson(obj:BSONObject, response:HttpServletResponse, status:Int){
    response.setContentType("application/bson")
    response.setStatus(status)
    response.getOutputStream().write(new BasicBSONEncoder().encode(obj))
  }

  def replyWithStatus(obj:BSONObject, request:HttpServletRequest, response:HttpServletResponse, status:Int) {
    getReplyContentType(request) match {
      case "application/json" => replyWithJson(obj, response, status)
      case "application/bson" => replyWithBson(obj, response, status)
    }
  }

  def replyWith(obj:BSONObject, request:HttpServletRequest, response:HttpServletResponse){
   replyWithStatus(obj, request, response, HttpServletResponse.SC_OK)
  }

}
