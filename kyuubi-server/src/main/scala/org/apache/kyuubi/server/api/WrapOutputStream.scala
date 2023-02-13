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

import java.io.ByteArrayOutputStream
import javax.servlet.{ServletOutputStream, WriteListener}

class WrapOutputStream(stream: ByteArrayOutputStream) extends ServletOutputStream {
  var out: ByteArrayOutputStream = stream

  override def write(b: Int): Unit = {
    out.write(b)
  }

  override def isReady: Boolean = {
    false
  }

  override def setWriteListener(writeListener: WriteListener): Unit = {}

  override def flush(): Unit = {
    out.flush()
  }

  override def close(): Unit = {
    out.flush()
  }
}
