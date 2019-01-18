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

package com.vip.pallas.console.converter;

import java.lang.reflect.Type;
import java.util.HashMap;

import org.springframework.http.HttpOutputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.Nullable;

import com.vip.pallas.console.vo.base.ConsoleResponse;
import com.vip.pallas.utils.JsonUtil;

public class ConsoleMessageConverter extends MappingJackson2HttpMessageConverter {

    private static final String OK = "ok";

    @Override
    protected void writeInternal(Object object, @Nullable Type type, HttpOutputMessage outputMessage) throws HttpMessageNotWritableException{
        try {
            int status = ((ServletServerHttpResponse) outputMessage).getServletResponse().getStatus();
            if(status >= HttpStatus.OK.value() && status < HttpStatus.MULTIPLE_CHOICES.value()){ //2xx，正常
                if(!OK.equals(object)){
                    ConsoleResponse response = new ConsoleResponse(object);
                    if(type.getTypeName().startsWith("java.util.Map")){
                        Object customStatus = ((HashMap) object).get("status");
                        if(customStatus != null){
                            response.setStatus(Integer.parseInt(String.valueOf(customStatus)));
                        }
                    }
                    outputMessage.getBody().write(JsonUtil.toJson(response).getBytes("UTF-8"));
                }else{
                    outputMessage.getBody().write(OK.getBytes());
                }
            }else{
                outputMessage.getBody().write(JsonUtil.toJson(object).getBytes("UTF-8"));
            }
        } catch (Exception e) {
            logger.error(e.toString(), e);
        }
    }
}