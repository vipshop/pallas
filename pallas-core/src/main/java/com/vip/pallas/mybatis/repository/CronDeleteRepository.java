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

import com.vip.pallas.mybatis.entity.CronDelete;
import com.vip.pallas.mybatis.entity.CronDeleteExample;

@Repository
public interface CronDeleteRepository {
    long countByExample(CronDeleteExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CronDelete record);

    int insertSelective(CronDelete record);

    List<CronDelete> selectByExampleWithBLOBs(CronDeleteExample example);

    List<CronDelete> selectByExample(CronDeleteExample example);

    CronDelete selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CronDelete record, @Param("example") CronDeleteExample example);

    int updateByExampleWithBLOBs(@Param("record") CronDelete record, @Param("example") CronDeleteExample example);

    int updateByExample(@Param("record") CronDelete record, @Param("example") CronDeleteExample example);

    int updateByPrimaryKeySelective(CronDelete record);

    int updateByPrimaryKeyWithBLOBs(CronDelete record);

    int updateByPrimaryKey(CronDelete record);

	List<CronDelete> selectByVersionId(Long versionId);

	List<CronDelete> selectByIndexId(Long indexId);

}