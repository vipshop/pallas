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

import com.vip.pallas.mybatis.entity.SearchAuthorization;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by owen on 08/01/2018.
 */
@Repository
public interface SearchAuthorizationRepository {

    int insert(SearchAuthorization authorization);

    int update(SearchAuthorization authorization);

    int delete(@Param("id") Long id);

    void setEnable(@Param("id") Long id, @Param("enabled") boolean enabled);

    List<SearchAuthorization> selectAll();

    SearchAuthorization findByToken(@Param("token") String token);

    SearchAuthorization findById(@Param("id") Long id);

}