/**
 * Copyright 2019 vip.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.vip.pallas.plugin.utils;

import com.vip.pallas.plugin.PallasPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ScriptExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptExecutor.class);

    public static void execute(String cmd) {
        try {
            new ProcessBuilder().command("/bin/bash", "-c", cmd)
                .directory(new File(cmd.substring(0, cmd.lastIndexOf("/"))))
                .redirectOutput(ProcessBuilder.Redirect.appendTo(new File(PallasPlugin.logsPath + "/pallas-es-restart-log.log")))
                .redirectError(ProcessBuilder.Redirect.appendTo(new File(PallasPlugin.logsPath + "/pallas-es-restart-error.log")))
                .start().waitFor();
        } catch (Exception e) {
            LOGGER.error("Execute: {} script error", cmd, e);
        }
    }
}