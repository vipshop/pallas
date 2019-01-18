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

package com.vip.pallas.mybatis.repository;

import com.vip.pallas.mybatis.entity.FlowRecord;
import com.vip.pallas.mybatis.entity.Index;
import com.vip.pallas.mybatis.entity.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlowRecordRepository {
    int deleteByPrimaryKey(Long id);

    int insert(FlowRecord record);

    int insertSelective(FlowRecord record);

    FlowRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FlowRecord record);

    int updateByPrimaryKey(FlowRecord record);

	List<FlowRecord> selectAll();

	List<FlowRecord> selectByClusterAndIndex(String clusterName, String indexName);

    List<FlowRecord> selectFlowRecordByPage(Page<FlowRecord> page);

    List<FlowRecord> selectFlowRecordByConfig(Page<FlowRecord> page);

    FlowRecord selectFlowRecordById(Long id);

    /**
     * 获取所有可以被记录采集的信息
     * @return
     */
    List<FlowRecord> selectAllRecording();

    /**
     * 根据配置ID获取所有可以被记录采集的信息
     * @return
     */
    List<FlowRecord> selectRecordingByConfigId(@Param("configId")Long configId);

    void increRecordTotal(@Param("id")Long id, @Param("increment")Integer increment);
}