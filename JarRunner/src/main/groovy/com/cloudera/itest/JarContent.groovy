package com.cloudera.itest

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.zip.ZipInputStream

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

  static {
    // Calling MOP hooks
    bootstrapPlugins();
  }

  private static List<String> patterns = null;
  private static List<String> content = null;
  // Exclude META* and inner classes
  def static defaultExclPattern = ['.*META.*', '.*\\$.*.class']

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

  /**
   * Finds JAR URL of an object's class belongs to
   * @param ref Class reference of a class belongs to a jar file
   * @return JAR URL or <code>null</code> if class doesn't belong
   * to a JAR in the classpath
   */
  public static URL getJarURL(Class ref) {
    URL clsUrl = ref.getResource(ref.getSimpleName() + ".class");
    if (clsUrl != null) {
      try {
        URLConnection conn = clsUrl.openConnection();
        if (conn instanceof JarURLConnection) {
          JarURLConnection connection = (JarURLConnection) conn;
          return connection.getJarFileURL();
        }
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return null;
  }

  /**
   * Finds JAR URL of an object's class belongs to
   * @param className is full name of the class e.g. java.lang.String
   * @return JAR URL or <code>null</code> if class doesn't belong
   * to a JAR in the classpath
   * @throws ClassNotFoundException if class specified by className wasn't found
   */
  public static URL getJarURL(String className) throws ClassNotFoundException {
    Class cl = Class.forName(className)
    return getJarURL(cl)
  }

  /**
   * Finds and unpack a jar file by locating to what jar file a given class belongs
   * and unpacking jar content to desalination according to given includes
   * @param ref
   * @param destination
   * @param includes
   * @return true if unpacked successfully, false otherwise
   */
  public static boolean unpackJarContainer (Class ref,
    String destination, String includes) {
    URL connection = JarContent.getJarURL(ref);
    if (connection == null) {
      return false;
    }
    ZipInputStream fis =
      new ZipInputStream(connection.openConnection().getInputStream());
    fis.unzip (destination, includes);
    return true;
  }

  public static boolean unpackJarContainer (String className,
    String destination, String includes) {
    Class cl = Class.forName(className)
    return unpackJarContainer (cl, destination, includes)
  }

  private static void bootstrapPlugins() {
    /**
     * Adding an ability to uppack a content of an given ZipInputStream
     * to specified destination with given pattern
     * @param dest directory where the content will be unpacked
     * @param includes regexps to include resources to be unpacked
     */
    ZipInputStream.metaClass.unzip = { String dest, String includes ->
      //in metaclass added methods, 'delegate' is the object on which
      //the method is called. Here it's the file to unzip
      if (includes == null) includes = "";
      def result = delegate
      def destFile = new File(dest)
      if (!destFile.exists()) {
        destFile.mkdir();
      }
      result.withStream {
        def entry
        while (entry = result.nextEntry) {
          if (!entry.name.contains(includes)) {
            continue
          };
          if (!entry.isDirectory()) {
            new File(dest + File.separator + entry.name).parentFile?.mkdirs()
            def output = new FileOutputStream(dest + File.separator
                + entry.name)
            output.withStream {
              int len;
              byte[] buffer = new byte[4096]
              while ((len = result.read(buffer)) > 0) {
                output.write(buffer, 0, len);
              }
            }
          }
          else {
            new File(dest + File.separator + entry.name).mkdir()
          }
        }
      }
    }
  }
}

