package com.rackspace.kafka.http

import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler
import org.eclipse.jetty.server.HttpConnection;
import org.eclipse.jetty.http.HttpMethods;

import org.bson.BasicBSONObject

class ServletErrorHandler extends ErrorHandler with ReplyFormatter {
  override def handle(target:String, baseRequest:Request, request:HttpServletRequest, response:HttpServletResponse){
    var connection = HttpConnection.getCurrentConnection();
    var method = request.getMethod();
    if(!method.equals(HttpMethods.GET) && !method.equals(HttpMethods.POST) && !method.equals(HttpMethods.HEAD)){
      connection.getRequest().setHandled(true);
      return;
    }
    connection.getRequest().setHandled(true);
    var obj = new BasicBSONObject()
    obj.append("error", connection.getResponse().getReason())
    replyWithStatus(obj, request, response, connection.getResponse().getStatus())
  }
}
