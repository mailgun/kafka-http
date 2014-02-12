package com.rackspace.kafka.http

import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.RequestDispatcher

import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.ErrorHandler
import org.eclipse.jetty.http.HttpMethods
import org.eclipse.jetty.server.AbstractHttpConnection

import org.bson.BasicBSONObject


class ServletErrorHandler extends ErrorHandler with ReplyFormatter {
  override def handle(target:String, baseRequest:Request, request:HttpServletRequest, response:HttpServletResponse){
    var connection = AbstractHttpConnection.getCurrentConnection();
    var method = request.getMethod();
    if(!method.equals(HttpMethods.GET) && !method.equals(HttpMethods.POST) && !method.equals(HttpMethods.HEAD)){
      connection.getRequest().setHandled(true);
      return;
    }
    connection.getRequest().setHandled(true);
    var obj = new BasicBSONObject()
    var err = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
    if(err != null) {
      obj.append("error", err.asInstanceOf[Throwable].getMessage());
    } else {
      obj.append("error", "Internal server error");
    }

    replyWithStatus(obj, request, response, connection.getResponse().getStatus())
  }
}
