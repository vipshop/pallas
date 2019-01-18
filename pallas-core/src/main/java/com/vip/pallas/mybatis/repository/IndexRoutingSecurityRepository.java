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

import com.vip.pallas.mybatis.entity.IndexRoutingSecurity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by owen on 02/11/2017.
 */
@Repository
public interface IndexRoutingSecurityRepository {

    int insert(IndexRoutingSecurity targetGroup);

    int update(IndexRoutingSecurity targetGroup);

    IndexRoutingSecurity selectByIndexId(@Param("indexId") Long indexId);

    void setEnable(@Param("id") Long id, @Param("enable") boolean enable);

    List<IndexRoutingSecurity> selectAll();
}