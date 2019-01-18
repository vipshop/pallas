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

package com.vip.pallas.console.destroy;

import com.vip.pallas.utils.SystemExitListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
public class ConsolePreDestroy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsolePreDestroy.class);

    @PreDestroy
    public void destory() {
        LOGGER.error("本应用开始退出...准备通知所有相关线程.");
        SystemExitListener.notifyExit();
    }
}