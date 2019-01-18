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

package com.vip.pallas.console.controller.index;

/**
 * Created by owen on 28/11/2018.
 */
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

@RestController
public class IndexDsImportController {


    @RequestMapping(value = "/ds/import.json", method = RequestMethod.POST)
    public void importSchema(MultipartFile file, HttpServletResponse response, HttpServletRequest request)
            throws Exception {

        String jsonStr = IOUtils.toString(file.getInputStream(), StandardCharsets.UTF_8);
        response.setStatus(200);
        response.setContentType("text/html");
        response.getWriter().write(jsonStr);
        response.flushBuffer();
    }


}