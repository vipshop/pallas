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

import com.vip.pallas.mybatis.entity.SearchServer;
import com.vip.pallas.mybatis.entity.SearchServerExample;
@Repository
public interface SearchServerRepository {
    long countByExample(SearchServerExample example);

    int deleteByExample(SearchServerExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SearchServer record);

    int insertSelective(SearchServer record);

	List<SearchServer> selectByExampleWithBLOBsAndHealthyInterval(SearchServerExample example);

    List<SearchServer> selectByExample(SearchServerExample example);

    List<SearchServer> selectHealthyServers(Long healthyInterval);

	List<SearchServer> selectHealthyServersByCluster(Long healthyInterval, String cluster);

    SearchServer selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SearchServer record, @Param("example") SearchServerExample example);

    int updateByExampleWithBLOBs(@Param("record") SearchServer record, @Param("example") SearchServerExample example);

    int updateByExample(@Param("record") SearchServer record, @Param("example") SearchServerExample example);

    int updateByPrimaryKeySelective(SearchServer record);

    int updateByPrimaryKeyWithBLOBs(SearchServer record);

    int updateByPrimaryKey(SearchServer record);

    void setTakeTraffic(@Param("id") Long id, @Param("takeTraffic") Boolean takeTraffic);
    
    List<String> selectDistictCluster();

    List<SearchServer> selectAll();

	void deleteNDaysOldServer(int n);
}