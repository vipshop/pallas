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

package com.vip.pallas.console.controller.api.version;

import com.vip.pallas.console.vo.IndexRampupVO;
import com.vip.pallas.mybatis.entity.Index;
import com.vip.pallas.mybatis.entity.IndexVersion;
import com.vip.pallas.service.IndexService;
import com.vip.pallas.service.IndexVersionService;
import com.vip.pallas.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/version")
public class VersionApiController {

    private static volatile Map<Long, Object> RAMPUP_LOCK_MAP = new ConcurrentHashMap<>();

    @Autowired
    private IndexVersionService versionService;

    @Autowired
    private IndexService indexService;

    @RequestMapping(value = "/list/all.json", method = RequestMethod.GET)
    public List<IndexVersion> listAll() {
        return versionService.findAll();
    }

    @RequestMapping(value = "/rampup/increment.json", method = RequestMethod.GET)
    public void increment(@RequestParam @NotNull(message = "versionId不能为空") Long versionId,
                          @RequestParam @NotNull(message = "increment不能为空") @Min(value = 1, message = "increment必须为正数") Long increment) throws Exception {
        String rampupInfo = versionService.getRampupByVersionId(versionId);

        if(rampupInfo != null && StringUtils.isNotBlank(rampupInfo)){
            IndexRampupVO rampupVO = JsonUtil.readValue(rampupInfo, IndexRampupVO.class);

            if(rampupVO.needRampup()){
                RAMPUP_LOCK_MAP.putIfAbsent(versionId, new Object());

                synchronized (RAMPUP_LOCK_MAP.get(versionId)){
                    long rampupTarget = rampupVO.getRampupTarget();
                    Date rampupEndTime = rampupVO.getEndTime();

                    rampupVO.setRampupNow(rampupVO.getRampupNow() + increment);

                    //按条数/时间预热
                    if((rampupTarget > -1 && rampupVO.getRampupNow() >= rampupTarget)
                            || (rampupEndTime != null && System.currentTimeMillis() >= rampupEndTime.getTime())){
                        rampupVO.setState(IndexRampupVO.STATE_FINISH);
                        rampupVO.setEndTime(new Date());
                    }

                    IndexVersion indexVersion = versionService.findById(versionId);
                    indexVersion.setRampUp(JsonUtil.toJson(rampupVO));
                    versionService.update(indexVersion);
                }
            }
        }
    }

    @RequestMapping(value = "/rampup/start.json", method = RequestMethod.GET)
    public void start(@RequestParam @NotNull(message = "versionId不能为空") Long versionId,
                      String endTime, Long rampupTarget) throws Exception {
        if(StringUtils.isBlank(endTime) && rampupTarget == 0){
            throw new Exception("预热结束时间与目标预热条数不能同时为空");
        }

        IndexVersion indexVersion = versionService.findById(versionId);

        Index index = indexService.findById(indexVersion.getIndexId());

        IndexRampupVO rampupVO = new IndexRampupVO();
        rampupVO.setIndexId(index.getId());
        rampupVO.setVersionId(versionId);
        rampupVO.setFullIndexName(index.getIndexName() + "_" + versionId);
        rampupVO.setBeginTime(new Date());
        rampupVO.setState(IndexRampupVO.STATE_DOING);

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        if(StringUtils.isNotBlank(endTime)){
            try{
                rampupVO.setEndTime(format.parse(endTime.replaceAll("\"", "")));
                rampupVO.setEndTime(new Date(rampupVO.getEndTime().getTime() + 8 * 60 * 60 * 1000));
            }catch(Exception ignore){
                rampupVO.setEndTime(new Date(Long.parseLong(endTime)));
            }
        }

        if(rampupTarget > 0){
            rampupVO.setRampupTarget(rampupTarget);
        }

        indexVersion.setRampUp(JsonUtil.toJson(rampupVO));
        versionService.update(indexVersion);
    }

    @RequestMapping(value = "/rampup/stop.json", method = RequestMethod.GET)
    public void stop(@RequestParam @NotNull(message = "versionId不能为空") Long versionId) throws Exception {
        String rampupInfo = versionService.getRampupByVersionId(versionId);

        if(rampupInfo != null && StringUtils.isNotBlank(rampupInfo)){
            IndexRampupVO rampup = JsonUtil.readValue(rampupInfo, IndexRampupVO.class);
            rampup.setState(IndexRampupVO.STATE_STOP);

            IndexVersion indexVersion = versionService.findById(versionId);
            indexVersion.setRampUp(JsonUtil.toJson(rampup));
            versionService.update(indexVersion);
        }
    }

    @RequestMapping(value = "/rampup/id.json", method = RequestMethod.GET)
    public IndexRampupVO findById(@RequestParam @NotNull(message = "versionId不能为空") Long versionId) throws Exception {
        String rampupInfo = versionService.getRampupByVersionId(versionId);

        if(rampupInfo != null && StringUtils.isNotBlank(rampupInfo)){
            return JsonUtil.readValue(rampupInfo, IndexRampupVO.class);
        }

        return null;
    }
}