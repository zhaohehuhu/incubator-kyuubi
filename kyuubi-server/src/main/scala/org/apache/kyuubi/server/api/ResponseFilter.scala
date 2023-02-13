/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.kyuubi.server.api

import javax.servlet._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import org.eclipse.jetty.proxy.ProxyServlet

import org.apache.kyuubi.Logging

class ResponseFilter extends Filter with Logging {

  val ATTR_TARGET_IP = classOf[ProxyServlet].getSimpleName + ".ipAddress"
  val ATTR_TARGET_PORT = classOf[ProxyServlet].getSimpleName + ".port"

  override def init(filterConfig: FilterConfig): Unit = {
    super.init(filterConfig)
  }

  override def doFilter(
      request: ServletRequest,
      response: ServletResponse,
      filterChain: FilterChain): Unit = {
    val httpRequest = request.asInstanceOf[HttpServletRequest]
    val ipAddress = httpRequest.getAttribute(ATTR_TARGET_IP)
    val port = httpRequest.getAttribute(ATTR_TARGET_PORT)
    val httpResponse = response.asInstanceOf[HttpServletResponse]
    val wrapResponse = new WrapResponse(httpResponse)
    filterChain.doFilter(request, wrapResponse)
    logger.info("content type is {}", httpResponse.getContentType)
    if (httpResponse != null &&
      httpResponse.getContentType != null && httpResponse.getContentType.contains("text/html")
      && ipAddress != null) {
      val content = wrapResponse.getCaptureAsString()
      logger.info("content is {}", content)
      val firstReplacement = "href=\"/proxy/" + s"$ipAddress:$port/"
      val secondReplacement = "src=\"/proxy/" + s"$ipAddress:$port/"
      val thirdReplacement = s"/proxy/$ipAddress:$port/api/v1/"
      val fourthReplacement = s"/proxy/$ipAddress:$port/static/"
      val newContent = content.replaceAll("href=\"/", firstReplacement)
        .replaceAll("src=\"/", secondReplacement)
        .replaceAll("/api/v1/", thirdReplacement)
        .replaceAll("/static/", fourthReplacement)
      logger.info("new content is {}", newContent)
      val out = httpResponse.getWriter
      out.write(newContent)
      httpResponse.setContentLength(newContent.getBytes().length)
    }
  }

  override def destroy(): Unit = {}
}
