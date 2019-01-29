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

package com.vip.pallas.console.controller.plugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.processor.plugin.AbstractPluginFileProcessor;

@Validated
@RestController
@RequestMapping("/plugin/upgrade")
public class PluginFileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginFileController.class);
    
    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    @Autowired
    private AbstractPluginFileProcessor pluginFileProcessor;

    @RequestMapping(path="/fileUpload.json")
    public String uploadFile(@RequestParam @NotNull(message = "file不能为空") MultipartFile file,
            @RequestParam @NotBlank(message = "clusterId不能为空") String clusterId,
            @RequestParam @NotBlank(message = "pluginName不能为空") String pluginName,
            @RequestParam @NotBlank(message = "pluginVersion不能为空") String pluginVersion,
            @RequestParam(required = false, defaultValue = "") String justForJenkinsTest) {
        
        String pluginFileName = pluginName + "-" + pluginVersion;
        String path = TMP_DIR + "/" + pluginFileName + ".zip";
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new BusinessLevelException(500, "删除旧的临时文件发生错误：" + e.getMessage());
        }
        String packagePath = "";
        File tmpFile = new File(path);
        try(BufferedInputStream bis = new BufferedInputStream(file.getInputStream())) {
            FileUtils.copyInputStreamToFile(bis, tmpFile);

            if(!validateZipFileFormat(pluginFileName, tmpFile)) {
                throw new BusinessLevelException(500, "上传文件不符合格式,文件夹结构必须按照格式：" + pluginFileName);
            }

            if(!"test".equals(justForJenkinsTest)) {
                packagePath = pluginFileProcessor.upload(clusterId, pluginName, pluginVersion, tmpFile);
                FileUtils.deleteQuietly(tmpFile);
            }
        } catch (BusinessLevelException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new BusinessLevelException(500, "读取上传文件发生错误：" + e.getMessage());
        } finally {
            try {
                Files.deleteIfExists(Paths.get(path));
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                throw new BusinessLevelException(500, "删除上传临时文件发生错误：" + e.getMessage()); //NOSONAR
            }
        }
        return packagePath;
    }

    @RequestMapping(path="/fileDownload.json")
    public void donwloadFile(@RequestParam @NotBlank(message = "clusterId不能为空") String clusterId,
                             @RequestParam @NotBlank(message = "pluginName不能为空") String pluginName,
                             @RequestParam @NotBlank(message = "pluginVersion不能为空") String pluginVersion,
                               HttpServletResponse response) throws Exception {
        File file = null;
        try{
            file = pluginFileProcessor.download(clusterId, pluginName, pluginVersion);

            String fileName = pluginName + "-" + pluginVersion + ".zip";

            response.setStatus(200);
            response.setContentType("application/zip;charset=UTF-8");
            response.setHeader("Content-Disposition","attachment; filename=" + fileName);

            FileUtils.copyFile(file, response.getOutputStream());
        }finally {
            FileUtils.deleteQuietly(file);
        }
    }
    
    private boolean validateZipFileFormat(String format, File tmpFile) throws Exception {
        if (tmpFile.exists()) {
            try (InputStream in = Files.newInputStream(tmpFile.toPath());
                 ZipInputStream zis = new ZipInputStream(in)) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    String entryName = entry.getName();
                    if(entryName.contains("/")) {
                        String folder = entryName.substring(0, entryName.indexOf('/'));
                        return format.equals(folder);
                    }
                }
                return false;
            }
        }
        return false;
    }
}