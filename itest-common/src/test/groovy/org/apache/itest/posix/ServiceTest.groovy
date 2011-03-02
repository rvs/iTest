/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.itest.posix

import org.apache.itest.shell.Shell
import org.junit.Test
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertEquals

class ServiceTest {
  private final String name = "ssh";
  Service ssh = new Service(name);

  @Test
  void testStatus() {
    assertTrue("Expected a non-empty string as an ssh service status", ssh.status() != "")
    assertEquals("wrong service name", name, ssh.getName());
  }
}
