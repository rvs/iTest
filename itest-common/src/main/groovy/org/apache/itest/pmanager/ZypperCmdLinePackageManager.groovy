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

class ZypperCmdLinePackageManager extends PackageManager {
  static String type  = "zypper"

  public void setDefaults(String defaults) {}

  public int addBinRepo(String record, String url, String key, String cookie) {
    shRoot.exec("zypper -q -n ar -c -n '${record}' $url ${cookie.replaceAll(/\s+/, '-')}");
    return shRoot.getRet();
  }

  public int refresh() {
    shRoot.exec("""expect -f - << __EOT__
spawn zypper refresh
expect "yes/no] (no):"
send "yes\\r"
expect eof
__EOT__""");
    return shRoot.getRet();
  }

  public List<PackageInstance> search(String name, String version) {
    def packages = new ArrayList<PackageInstance>();
    shUser.exec("zypper search $name").out.each {
      packages.add(PackageInstance.getPackageInstance (this, ((it =~ /^(.*|)(.*)(|.*)$/)[0][2])))
    }
    return packages
  }

  public int install(PackageInstance pkg) {
    shRoot.exec("zypper -q -n install -l -y ${pkg.name}");
    return shRoot.getRet();
  }

  public int remove(PackageInstance pkg) {
    shRoot.exec("zypper -q -n remove -y ${pkg.name}");
    return shRoot.getRet();
  }

  public boolean isInstalled(PackageInstance pkg) {
    def text = shUser.exec("zypper -q -n info ${pkg.name}").out.join('\n')
    return (text =~ /(?m)^Installed: Yes$/).find()
  }

  public List<Service> getServices(PackageInstance pkg) {
    shUser.exec("rpm -ql ${pkg.name} | sed -ne '/^.etc.rc.d./s#^.etc.rc.d.##p'")
    return shUser.out.collect({new Service("$it")})
  }
}
