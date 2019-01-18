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
import com.vip.pallas.mybatis.entity.FlowRecordConfig;
import com.vip.pallas.mybatis.entity.Page;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlowRecordConfigRepository {
    int deleteByPrimaryKey(Long id);

    int insert(FlowRecordConfig record);

    int insertSelective(FlowRecordConfig record);

    FlowRecordConfig selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FlowRecordConfig record);

    int updateByPrimaryKey(FlowRecordConfig record);

    List<FlowRecordConfig> selectAll();

    List<FlowRecordConfig> selectByClusterAndIndex(String clusterName, String indexName);

    List<FlowRecordConfig> selectFlowRecordConfigByPage(Page<FlowRecordConfig> page);
}