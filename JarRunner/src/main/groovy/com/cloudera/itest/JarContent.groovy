package com.cloudera.itest

import java.util.jar.JarEntry
import java.util.jar.JarFile

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
class JarContent {
  def List<String> patterns = new ArrayList<String>();
  def List<String> content = new ArrayList<String>();
  // Exclude META* and inner classes
  def defaultExclPattern = [ '.*META.*', '.*\\$.*.class' ]


  def List<String> listContent(String jarFileName) throws IOException {
    if (jarFileName == null) {
      System.out.println("Specify a jar file name");
      System.exit(1);
    }
    JarFile jarFile = new JarFile(jarFileName);

    Enumeration<JarEntry> entries = jarFile.entries();
    while (entries.hasMoreElements()) {
      def en = process(entries.nextElement());
      en != null ? content.add(en) : null;
    }
    setPatterns(defaultExclPattern);

    content = applyExcludeFilter(content, patterns);
    return content;
  }

  private static String process(JarEntry jarEntry) throws IOException {
    JarEntry entry = jarEntry;
    String name = entry.getName();
    if (!entry.isDirectory())
      return name;
    return null;
  }

  def List<String> getPatterns() {
    return patterns;
  }

  def setPatterns(List<String> filterPatters) {
    filterPatters.each {
      patterns.add(it);
    }
  }

  /**
   * Filter out any entries which match given patterns
   * @param list of entries
   * @param filters list of patterns
   * @return filtered-out list of entries
   */
  def List<String> applyExcludeFilter(final List<String> list, final List<String> filters) {
    List<String> filtered = list.asList();
    ArrayList<String> toRemove = new ArrayList<String>();

    filters.each {
      def pattern = ~/${it}/
      for (l in list) {
        if (pattern.matcher(l).matches())
          toRemove.add(l);
      }
    }
      filtered.removeAll(toRemove);
      return filtered;
  }
}