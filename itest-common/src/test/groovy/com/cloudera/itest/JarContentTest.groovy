/*
 * Copyright (c) 2011, Cloudera, Inc. All Rights Reserved.
 *
 * Cloudera, Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"). You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */
package com.cloudera.itest

import org.junit.Test
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertEquals

class JarContentTest {
  @Test
  void testJarContent() {
    def env = System.getenv();
    def list = JarContent.listContent(env['JAVA_HOME'] + '/lib/tools.jar');
    assertTrue("Jar content should be greater than 10", list.size() > 10);
  }
  @Test(expected = IOException.class)
  void testJarContentNeg() {
    def env = System.getenv();
    try {
      JarContent.listContent(env['JAVA_HOME'] + '/lib/nofilelikethat.jar').each {
        println it;
      }
    } catch (IOException e) {
      assert e.getMessage().startsWith('Could not open')
      throw e;
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
      if (it.name.endsWith(".class"))
        count++
    }
    assertTrue('Expect more than one file', count > 1);
    dir.deleteDir();
  }

  @Test
  void testGetJarName() {
    assertEquals("Should've find tools.jar file",
        'tools.jar',
        JarContent.getJarName(System.getenv()['JAVA_HOME'] + '/lib/', 't.*.jar'));
    assertEquals("Should not have found tools.jar file", null,
        JarContent.getJarName(System.getenv()['JAVA_HOME'] + '/lib/', 'nosuch-file.*.jar'));
  }

  // ClassNotException is expected to be thrown in case of non-existing class
  @Test(expected = ClassNotFoundException.class)
  void testUnpackJarContainerNeg() {
    def destination = 'target/local.unpack.dir';
    JarContent.unpackJarContainer('com.lang.NoString', destination, 'visitor');
  }
  // IOException is expected in case of a class not loaded from a jar
  @Test(expected = IOException.class)
  void testUnpackJarContainerNoJar() {
    def destination = 'target/local.unpack.dir';
    JarContent.unpackJarContainer(JarContentTest, destination, 'visitor');
  }
}
