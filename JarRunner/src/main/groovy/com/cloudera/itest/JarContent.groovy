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
public abstract class JarContent {
  private static List<String> patterns = null;
  private static List<String> content = null;
  // Exclude META* and inner classes
  def static defaultExclPattern = [ '.*META.*', '.*\\$.*.class' ]

  def static List<String> listContent(String jarFileName) throws IOException {
    content = new ArrayList<String>();
    if (jarFileName == null) {
      throw new IOException("Specify a jar file name");
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

  /**
   * Set a list of new patterns to be applied in the processing of a jar file
   * @param filterPatters list of pattern strings
   * @return list of currently set patterns. Next call of this method will
   * reset the content of patterns' list.
   */
  def static List<String> setPatterns(List<String> filterPatters) {
    patterns = new ArrayList<String>();
    filterPatters.each {
      patterns.add(it);
    }
    return patterns;
  }

  /**
   * Filter out any entries which match given patterns
   * @param list of entries
   * @param filters list of patterns
   * @return filtered-out list of entries
   */
  def static List<String> applyExcludeFilter(final List<String> list, final List<String> filters) {
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