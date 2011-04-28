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

import com.cloudera.itest.posix.Service

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
  protected Map<String, Service> services;

  /**
   * Check if this package is installed on the system
   *
   * @return true if the package is installed and can be used, false otherwise
   */
  abstract public boolean isInstalled();
  /**
   * Install this package (from collection of all the packages available in all the repositories)
   *
   * @return int return code of the operation: o in case of success, non-zero otherwise
   */
  abstract public int install();
  /**
   * Remove this package from a system
   *
   * @return int return code of the operation: o in case of success, non-zero otherwise
   */
  abstract public int remove();
  /**
   * Re-sync metadata associated with this package with the underlying package management system
   *
   * @return int return code of the operation: o in case of success, non-zero otherwise
   */
  abstract public void refresh();
  /**
   * Get a list of services (System V init scripts) provided by this package
   *
   * @return list of Service instances
   */
  abstract public Map<String, Service> getServices();
  /**
   * Get a list of files provided by this package. This list will include ALL the files regardless
   * of whether they are also marked as configs or documentation.
   *
   * @return list file and directory names belong to the package.
   */
  abstract public List<String> getFiles();
  /**
   * Get a list of documentation files provided by this package (if the underlying package
   * management system doesn't support a notion of a documentation file -- empty list is expected
   * to be returned.
   *
   * @return list config file names that belong to the package.
   */
  abstract public List<String> getDocs();
  /**
   * Get a list of configuration files provided by this package (if the underlying package
   * management system doesn't support a notion of a configuration file -- empty list is expected
   * to be returned.
   *
   * @return list config file names that belong to the package.
   */
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
    PackageInstance pkg = (mgr.type == "apt" ) ? new DEBPackage() :
                                                 new RPMPackage();
    pkg.mgr = mgr;
    pkg.name = name;
    return pkg;
  }
}
