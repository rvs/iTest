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

abstract class PackageInstance {
  PackageManager mgr;

  String name;
  String version;
  String release;
  String arch;

  Map meta = [:];
  String installMessages;

  protected List<String> files;
  protected List<String> docs;
  protected List<String> configs;
  protected List<Service> services;

  abstract public boolean isInstalled();
  abstract public int install();
  abstract public int remove();
  abstract public List<Service> getServices();
  abstract public List<String> getFiles();
  abstract public List<String> getDocs();
  abstract public List<String> getConfigs();

  /**
   * Factory method for creating an instance of a Package that can reside in
   * a particular instance of a PackageManager.
   * NOTE: For now only 'natural' pairing is supported (e.g. deb with apt, rpm
   * with yum/zypper)
   *
   * @param mgr package manager that is expected to manage this type of package
   * @param name package manager dependent name of a package
   */
  static public PackageInstance getPackageInstance(PackageManager mgr, String name) {
    PackageInstance pkg = new ManagedPackage();
    pkg.mgr = mgr;
    pkg.name = name;
    return pkg;
  }
}
