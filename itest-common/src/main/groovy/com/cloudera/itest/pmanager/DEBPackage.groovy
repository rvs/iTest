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

package com.cloudera.itest.pmanager

import com.cloudera.itest.shell.Shell
import com.cloudera.itest.posix.Service

class DEBPackage extends ManagedPackage {
  Shell shRoot = new Shell("/bin/bash -s", "root");
  Shell shUser = new Shell();

  private static String translateMeta(String key) {
    def mapping = [Homepage: "url", Origin: "vendor", Section: "group"];
    return mapping[key] ?: key.toLowerCase();
  }

  /**
   * A helper method that parses a stream of metadata describing one or more packages.
   *
   * NOTE: the design of this is a bit ugly, we need to rethink how it is done.
   *
   * @param givenPkg a package to which the beginning of the metadata stream applies
   * @param text stream of metadata as a list of Strings
   * @param pm a package manager that this package belong to
   * @return list of EXTRA packages that were detected (and created!) during metadata parsing
   */
  public static List parseMetaOutput(PackageInstance givenPkg, List<String> text, PackageManager pm) {
    def packages = new ArrayList<PackageInstance>();
    PackageInstance pkg = givenPkg;
    String curMetaKey = "";
    text.each {
      if ((it =~ /^ /).find()) {
        pkg.meta[curMetaKey] <<= "\n$it";
      } else {
        def m = (it =~ /(\S+):(.*)/);
        if (m.size()) {
          String[] matcher = m[0];
          if ("Package" == matcher[1] && !givenPkg) {
            pkg = PackageInstance.getPackageInstance(pm, matcher[2]);
            packages.add(pkg);
          } else {
            curMetaKey = translateMeta(matcher[1]);
            pkg.meta[curMetaKey] = matcher[2].trim();
          }
        }
      }
    }

    (packages.size() == 0 ? [givenPkg] : packages).each {
      it.version = it.meta["version"] ?: it.version;
      it.arch = it.meta["architecture"] ?: it.arch;
      if (it.meta["conffiles"]) {
        it.configs = it.meta["conffiles"].toString().split("\n").collect { lines ->
          lines.replaceAll(/ [^ ]*$/, "").trim();
        }.findAll { l -> (l != ""); };
      }
    };
    return packages;
  }

  public void refresh() {
    parseMetaOutput(this, shUser.exec("env DEBIAN_FRONTEND=noninteractive dpkg -s $name").out, mgr);
  }

  public Map<String, Service> getServices() {
    Map res = [:];
    shUser.exec("env DEBIAN_FRONTEND=noninteractive dpkg -L $name | sed -ne '/^.etc.init.d./s#^.etc.init.d.##p'");
    shUser.out.collect {
      res[it] = new Service(it);
    }
    return res;
  }

  public List<String> getFiles() {
    shUser.exec("env DEBIAN_FRONTEND=noninteractive dpkg -L $name");
    return shUser.out.collect({"$it"});
  }

  public List<String> getConfigs() {
    shUser.exec("""
    env DEBIAN_FRONTEND=noninteractive dpkg -s $name | sed -ne '/Conffiles:/,/^[^ ]*:/{/:/!s# [^ ]*\$##p}'
    """);
    return shUser.out.collect({ it.toString().trim() });
  }

  public List<String> getDocs() {
    return [];
  }
}
