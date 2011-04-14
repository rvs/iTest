/*
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
package org.apache.itest.pmanager

import org.apache.itest.posix.Service

class AptCmdLinePackageManager extends PackageManager {
  // FIXME: NB export DEBIAN_FRONTEND=noninteractive
  static String type  = "apt";
  static String repository_registry = "/etc/apt/sources.list.d/%s.list";

  private static final ROOT_URL = "http://us.archive.ubuntu.com/ubuntu/";

  public void setDefaults(String defaults) {
     shRoot.exec("debconf-set-selections <<__EOT__\n${defaults}\n__EOT__");
  }

  public int addBinRepo(String record, String url, String key, String cookie) {
    if (!url)
      url = ROOT_URL;

    if (key) {
        def text = key.toURL().text;
        shRoot.exec("apt-key add - <<__EOT__\n${text}\n__EOT__");
        if (shRoot.getRet()) {
          return shRoot.getRet();
        }
    }

    return addBinRepo(record, "deb ${url} ${cookie}\ndeb-src ${url} ${cookie}");
  }

  public int refresh() {
    shRoot.exec("apt-get update");
    return shRoot.getRet();
  }

  public int cleanup() {
    shRoot.exec("apt-get -y clean", "apt-get -y autoremove");
    return shRoot.getRet();
  }

  public List<PackageInstance> search(String name) {
    def packages = new ArrayList<PackageInstance>();
    shUser.exec("apt-cache search --names-only $name").out.each {
      packages.add(PackageInstance.getPackageInstance (this, ((it =~ /^(.*)( - .*)$/)[0][1])))
    }
    return packages
  }

  private String translateMeta(String key) {
    def mapping = [Homepage: "url", Origin: "vendor", Section: "group"];
    def k = key.trim();
    return mapping[k] ?: k.toLowerCase();
  }

  private List parseMetaOutput(PackageInstance p, List<String> text) {
    def packages = new ArrayList<PackageInstance>();
    PackageInstance pkg = p;
    String curMetaKey = "";
    text.each {
      if ((it =~ /^ /).find()) {
        pkg.meta[curMetaKey] <<= "\n$it";
      } else {
        def m = (it =~ /(\S+):(.*)/);
        if (m.size()) {
          String[] matcher = m[0];
          if ("Package" == matcher[1] && !p) {
            pkg = PackageInstance.getPackageInstance(this, matcher[2]);
            packages.add(pkg);
          } else {
            curMetaKey = translateMeta(matcher[1]);
            pkg.meta[curMetaKey] = matcher[2];
          }
        }
      }
    }

    (packages.size() == 0 ? [p] : packages).each {
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

  public List<PackageInstance> lookup(String name) {
    return parseMetaOutput(null, shUser.exec("apt-cache show $name").out);
  }

  public int install(PackageInstance pkg) {
    shRoot.exec("env DEBIAN_FRONTEND=noninteractive apt-get -y install ${pkg.name}");
    if (shRoot.getRet() == 0) {
      pkg.installMessages = shRoot.getOut().join('\n');
      parseMetaOutput(pkg, shUser.exec("env DEBIAN_FRONTEND=noninteractive dpkg -s ${pkg.name}").out);
    }
    return shRoot.getRet();
  }

  public int remove(PackageInstance pkg) {
    // All config files need to be removed as well.
    shRoot.exec("env DEBIAN_FRONTEND=noninteractive apt-get -y purge ${pkg.name}");
    return shRoot.getRet();
  }

  public boolean isInstalled(PackageInstance pkg) {
    def text = shUser.exec("env DEBIAN_FRONTEND=noninteractive apt-get -s remove ${pkg.name}").out.join('\n')
    return (text =~ /\nRemv ${pkg.name} /).find()
  }

  public List<Service> getServices(PackageInstance pkg) {
    shUser.exec("env DEBIAN_FRONTEND=noninteractive dpkg -L ${pkg.name} | sed -ne '/^.etc.init.d./s#^.etc.init.d.##p'")
    return shUser.out.collect({new Service("$it")})
  }

  @Override
  public List<String> getContentList(PackageInstance pkg) {
    shUser.exec("env DEBIAN_FRONTEND=noninteractive dpkg -L ${pkg.name}");
    return shUser.out.collect({"$it"});
  }

  @Override
  public List<String> getConfigs(PackageInstance pkg) {
    shUser.exec("""
    env DEBIAN_FRONTEND=noninteractive dpkg -s ${pkg.name} | sed -ne '/Conffiles:/,/^[^ ]*:/{/:/!s# [^ ]*\$##p}'
    """);
    return shUser.out.collect({"$it"});
  }

  @Override
  public List<String> getDocs(PackageInstance pkg) {
    return [];
  }
}