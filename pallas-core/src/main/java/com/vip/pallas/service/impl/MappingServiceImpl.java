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

package com.vip.pallas.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vip.pallas.mybatis.entity.Mapping;
import com.vip.pallas.mybatis.repository.MappingRepository;
import com.vip.pallas.service.MappingService;

@Service
@Transactional(rollbackFor=Exception.class)
public class MappingServiceImpl implements MappingService {

	@Resource
	private MappingRepository mappingRepository;
	
	@Override
	public void insertMappingField(String schemaJson) {

	}
	@Override
    public List<String> getFieldsByVersionId(Long versionId) {
    	List<Mapping> mappingList = mappingRepository.selectByVersionId(versionId);
    	List<String> fieldList = new ArrayList<>();
    	if (mappingList != null && !mappingList.isEmpty()) {
			for (Mapping mapping : mappingList) {
				fieldList.add(mapping.getFieldName());
			}
		}
    	return fieldList;
    }

}