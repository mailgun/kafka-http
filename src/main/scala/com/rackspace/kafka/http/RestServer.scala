package com.rackspace.kafka.http

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.util.thread.QueuedThreadPool

import kafka.utils._

import org.apache.log4j.Logger

object RestServer
{
  val logger = Logger.getLogger("kafka.rest.server")

  def main(args:Array[String]){
    try{
      var servlet = Configurator.getServlet(args)
      var server = new Server(8080)

      server.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize", 35 * 1024 * 1024)

      for(connector <- server.getConnectors()){
        connector.setRequestBufferSize(35 * 1024 * 1024);
      }

      // This is to serialize access to endpoints
      var threadPool = new QueuedThreadPool(40)
      server.setThreadPool(threadPool)
      var context = new ServletContextHandler(ServletContextHandler.SESSIONS)

      context.setContextPath("/")
      context.setErrorHandler(new ServletErrorHandler())
      server.setHandler(context)
 
      context.addServlet(new ServletHolder(servlet),"/*")

      server.start()
      server.join()

    } catch {
      case e: Exception => {
        logger.error(e.toString)
        System.exit(1)
      }
    }

  }
}
