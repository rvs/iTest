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

class ZypperCmdLinePackageManager extends PackageManager {
  static String type  = "zypper"

  public void setDefaults(String defaults) {}

  public void addBinRepo(String record, String url, String key, String cookie) {
    def repoFile = new File("/etc/yum.repos.d/${record}")
    
    repoFile.write("""[${cookie.tr("\t\n\r ",'-')}]
                      name="${cookie}"
                      baseurl=${url}
                      gpgkey=${key}
                      gpgcheck=0
                      autorefresh=1""")
  }

  public void refresh() {
    shRoot.exec("zypper refresh")
  }

  public List<PackageInstance> search(String name, String version) {
    def packages = new ArrayList<PackageInstance>();
    shUser.exec("zypper search $name").out.each {
      packages.add(PackageInstance.getPackageInstance (this, ((it =~ /^(.*|)(.*)(|.*)$/)[0][2])))
    }
    return packages
  }

  public void install(PackageInstance pkg) {
    shRoot.exec("zypper -l install ${pkg.name}")
  }
  public void remove(PackageInstance pkg) {
    shRoot.exec("zypper remove ${pkg.name}")
  }

  public boolean isInstalled(PackageInstance pkg) {
    def text = shUser.exec("zypper info ${pkg.name}").out.join('\n')
    return (text =~ /(?m)^Installed: Yes$/).find()
  }

  public void svc_do(PackageInstance pkg, String action) {
//    shUser.exec("env DEBIAN_FRONTEND=noninteractive dpkg -L ${pkg.name} | sed -ne '/^.etc.init.d./s#^.etc.init.d.##p'")
//    shUser.out.each {
//      shRoot.exec("service $it $action")
//    }
  }
}
