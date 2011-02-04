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

class YumCmdLinePackageManager extends PackageManager {
  static String type  = "yum"

  public void setDefaults(String defaults) {}

  public void addBinRepo(String record, String url, String key, String cookie) {
    def repoFile = new File("/etc/yum.repos.d/${record}")
    repoFile.write("""[${cookie.replaceAll(/\s+/, '-')}]
name="${cookie}"
baseurl=${url}
gpgkey=${key}
gpgcheck=0""")
  }

  public void refresh() {
    // FIXME: really?
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

  public void install(PackageInstance pkg) {
    shRoot.exec("yum -y install ${pkg.name}")
  }
  public void remove(PackageInstance pkg) {
    shRoot.exec("yum -y erase ${pkg.name}")
  }

  public boolean isInstalled(PackageInstance pkg) {
    def text = shUser.exec("yum --color=never -d 0 list ${pkg.name}").out.join('\n')
    return (text =~ /(?m)^${pkg.name}.*installed$/).find()
  }

  public void svc_do(PackageInstance pkg, String action) {
    shUser.exec("rpm -ql ${pkg.name} | sed -ne '/^.etc.rc.d.init.d./s#^.etc.rc.d.init.d.##p'")
    shUser.out.each {
      shRoot.exec("/sbin/service $it $action")
    }
  }
}
