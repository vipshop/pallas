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

import com.vip.pallas.mybatis.entity.Page;
import com.vip.pallas.mybatis.entity.PluginRuntime;
import com.vip.pallas.mybatis.entity.PluginUpgrade;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Owen.LI on 9/12/2017.
 */
@Repository
public interface PluginUpgradeRepository {

    int insert(PluginUpgrade pluginUpgrade);

    int updateUpgradeState(@Param("id") long id, @Param("state") int status);

    int updateUpgrade(PluginUpgrade upgrade);

    List<PluginUpgrade> selectPage(Page<PluginUpgrade> page);

    PluginUpgrade findLatestUpgrade(@Param("clusterId") String clusterId, @Param("pluginName") String pluginName);

    PluginUpgrade getById(Long id);
}