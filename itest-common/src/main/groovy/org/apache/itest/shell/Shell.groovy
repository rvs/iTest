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

package org.apache.itest.shell

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class Shell {
  static private Log LOG = LogFactory.getLog(Shell.class)

  String shell;
  String user;

  String script;
  List<String> out;
  List<String> err;
  int ret;

  Shell(String sh) {
    shell = sh
  }

  Shell(String sh, String u) {
    shell = sh
    user = u
  }

  void setUser(String u) {
    user = u
  }

  /**
   * Execute shell script consisting of as many Strings as we have arguments,
   * possibly under an explicit username (requires sudoers privileges).
   * NOTE: individual strings are concatenated into a single script as though
   * they were delimited with new line character. All quoting rules are exactly
   * what one would expect in standalone shell script.
   *
   * After executing the script its return code can be accessed as getRet(),
   * stdout as getOut() and stderr as getErr(). The script itself can be accessed
   * as getScript()
   *
   * @param args shell script split into multiple Strings
   * @return Shell object for chaining
   */
  Shell exec(Object... args) {
    def proc = user ? "sudo -u $user PATH=${System.getenv('PATH')} $shell".execute() :
                                    "$shell".execute()
    script = args.join("\n")
    if (LOG.isTraceEnabled()) {
        LOG.trace("${shell} << __EOT__\n${script}\n__EOT__");
    }

    Thread.start {
      def writer = new PrintWriter(new BufferedOutputStream(proc.out))
      writer.println(script)
      writer.close()
    }
    proc.waitFor()

    ret = proc.exitValue()
    out = proc.in.readLines()
    err = proc.err.readLines()

    if (LOG.isTraceEnabled()) {
        if (ret != 0) {
           LOG.trace("return: $ret");
        }
        if (out.size() != 0) {
           LOG.trace("\n<stdout>\n${out.join('\n')}\n</stdout>");
        }
        if (err.size() != 0) {
           LOG.trace("\n<stderr>\n${err.join('\n')}\n</stderr>");
        }
    }

    return this
  }
}
