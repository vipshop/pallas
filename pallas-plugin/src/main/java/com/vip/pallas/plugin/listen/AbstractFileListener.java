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

package com.vip.pallas.plugin.listen;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;

/**
 * Created by jamin.li on 15/06/2017.
 */
public abstract class AbstractFileListener implements FileAlterationListener {

    private boolean isInit = false;

    public abstract void onInit(File file);

    @Override
    public void onStart(FileAlterationObserver fileAlterationObserver) {
        if(!isInit){
            onInit(fileAlterationObserver.getDirectory());
            isInit = true;
        }
    }
}