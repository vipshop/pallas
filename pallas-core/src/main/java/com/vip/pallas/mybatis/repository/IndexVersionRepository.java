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

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.vip.pallas.bean.IndexParam;
import com.vip.pallas.mybatis.entity.IndexVersion;
import com.vip.pallas.mybatis.entity.Page;

@Repository
public interface IndexVersionRepository {
    int deleteByPrimaryKey(Long id);

    int insert(IndexVersion record);

    IndexVersion selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(IndexVersion record);

    List<IndexVersion> selectAll();
    
    IndexParam selectIndexParamById(Long id);
    
    List<IndexVersion> selectPage(Page<IndexVersion> page);
    
    void updateSyncState(@Param("id")Long id, @Param("isSync")boolean isSync);
    
    void enableVersion(@Param("id")Long id);

    //禁用Index下的所有version
    void disableSameIndexVersion(@Param("indexId")Long indexId, @Param("clusterId")Long clusterId);
    
    void disableVersion(@Param("id")Long id);
    
    void deleteVersion(@Param("id")Long id);
    
    Long getUsedVersionByIndexIdAndClusterId(@Param("indexId")Long indexId, @Param("clusterId")Long clusterId);
    
    List<IndexParam> selectUsed();

    List<IndexVersion> findAllByClusterIdAndIndexId( @Param("clusterId")Long clusterId, @Param("indexId")Long indexId);

    IndexVersion findUsedIndexVersionByIndexId(@Param("indexId") Long indexId);
}