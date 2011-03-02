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

import org.apache.itest.posix.Service

class YumCmdLinePackageManager extends PackageManager {
  static String type  = "yum"

  public void setDefaults(String defaults) {}

  public int addBinRepo(String record, String url, String key, String cookie) {
    def repoFile = new File("/etc/yum.repos.d/${record}");
    try {
      repoFile.write("""[${cookie.replaceAll(/\s+/, '-')}]
name="${cookie}"
baseurl=${url}
gpgkey=${key}
gpgcheck=0""");
    } catch (IOException) {
      // If write wasn't successful return error code
      return 1;
    }
    return 0;
  }

  public int refresh() {
    // FIXME: really?
    return 0;
  }

  public List<PackageInstance> search(String name, String version) {
    def packages = new ArrayList<PackageInstance>();
    shUser.exec("yum --color=never -d 0 search $name").out.each {
      if (!(it =~ /^(===================| +: )/)) {
        packages.add(PackageInstance.getPackageInstance (this, it.replaceAll(/\.(noarch|i386|x86_64).*$/, '')))
      }
    }
    return packages
  }

  public int install(PackageInstance pkg) {
    shRoot.exec("yum -y install ${pkg.name}");
    return shRoot.getRet();
  }
  public int remove(PackageInstance pkg) {
    shRoot.exec("yum -y erase ${pkg.name}");
    return shRoot.getRet();
  }

  public boolean isInstalled(PackageInstance pkg) {
    def text = shUser.exec("yum --color=never -d 0 list ${pkg.name}").out.join('\n')
    return (text =~ /(?m)^${pkg.name}.*installed$/).find()
  }

  public List<Service> getServices(PackageInstance pkg) {
    shUser.exec("rpm -ql ${pkg.name} | sed -ne '/^.etc.rc.d.init.d./s#^.etc.rc.d.init.d.##p'")
    return shUser.out.collect({new Service("$it")})
  }
}
