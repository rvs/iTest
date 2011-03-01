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
package org.apache.itest.pmanager

import org.apache.itest.shell.Shell
import org.apache.itest.posix.Service

class AptCmdLinePackageManager extends PackageManager {
  // FIXME: NB export DEBIAN_FRONTEND=noninteractive
  static String type  = "apt"

  public void setDefaults(String defaults) {
     shRoot.exec("debconf-set-selections <<__EOT__\n${defaults}\n__EOT__")
  }

  public void addBinRepo(String record, String url, String key, String cookie) {
    if (!url)
      url = "http://us.archive.ubuntu.com/ubuntu/"

    shRoot.exec("add-apt-repository 'deb ${url} ${cookie}'")

    if (key) {
      def text = key.toURL().text
      shRoot.exec("apt-key add - <<__EOT__\n${text}\n__EOT__")
    }
  }

  public void refresh() {
    shRoot.exec("apt-get update")
  }

  public List<PackageInstance> search(String name, String version) {
    def packages = new ArrayList<PackageInstance>();
    shUser.exec("apt-cache search --names-only $name").out.each {
      packages.add(PackageInstance.getPackageInstance (this, ((it =~ /^(.*)( - .*)$/)[0][1])))
    }
    return packages
  }

  public void install(PackageInstance pkg) {
    shRoot.exec("env DEBIAN_FRONTEND=noninteractive apt-get -y install ${pkg.name}")
  }
  public void remove(PackageInstance pkg) {
    shRoot.exec("env DEBIAN_FRONTEND=noninteractive apt-get -y remove ${pkg.name}")
  }

  public boolean isInstalled(PackageInstance pkg) {
    def text = shUser.exec("env DEBIAN_FRONTEND=noninteractive apt-get -s remove ${pkg.name}").out.join('\n')
    return (text =~ /\nRemv ${pkg.name} /).find()
  }

  public List<Service> getServices(PackageInstance pkg) {
    shUser.exec("env DEBIAN_FRONTEND=noninteractive dpkg -L ${pkg.name} | sed -ne '/^.etc.init.d./s#^.etc.init.d.##p'")
    return shUser.out.collect({new Service("$it")})
  }
}
