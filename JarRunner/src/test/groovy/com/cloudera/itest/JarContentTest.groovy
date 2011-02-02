package com.cloudera.itest

import org.junit.Test
import static org.junit.Assert.assertTrue
import org.junit.rules.ExpectedException

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
class JarContentTest {
  @Test
  void testJarContent() {
    def env = System.getenv();
    JarContent.listContent(env['JAVA_HOME'] + '/lib/tools.jar').each {
      println it;
    };
  }

  @Test
  void testUnpackJarContainer() {
    def destination = 'target/local.unpack.dir';
    JarContent.unpackJarContainer('java.lang.String', destination, 'visitor');
    // expect to find a number of sun/reflect/generics/visitor
    // under destination folder
    File dir = new File(destination);
    int count = 0
    dir.eachFileRecurse {
      println it;
      if (it.name.endsWith(".class"))
        count++
    }
    assertTrue('Expect more than one file', count > 1);
    dir.deleteDir();
  }

  // ClassNotException is expected to be thrown in case of non-existing class
  @Test(expected = ClassNotFoundException.class)
  void testUnpackJarContainerNeg() {
    def destination = 'target/local.unpack.dir';
    JarContent.unpackJarContainer('com.lang.NoString', destination, 'visitor');
  }
}