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

import org.springframework.stereotype.Repository;

import com.vip.pallas.mybatis.entity.Index;
import com.vip.pallas.mybatis.entity.Page;

@Repository
public interface IndexRepository {
	
    int deleteByPrimaryKey(Long id);

    int insert(Index record);

    int insertSelective(Index record);

    Index selectByPrimaryKey(Long id);
    
    Index selectByid(Long id);

    int updateByPrimaryKeySelective(Index record);

    int updateByPrimaryKeyWithBLOBs(Index record);

    int updateByPrimaryKey(Index record);
    
    List<Index> selectAll();

    List<Index> selectAllSpecificdFiled();
    
    List<Index> selectPage(Page<Index> page);
    
    int getVersionCountByIndexId(Long indexId);

    Index findByClusterNameAndIndexName(String clusterName, String indexName);
    
	List<Index> findByIndexName(String indexName);
}