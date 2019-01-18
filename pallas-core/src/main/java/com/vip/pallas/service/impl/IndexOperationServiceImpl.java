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

import java.util.List;

import javax.annotation.Resource;

import com.vip.pallas.bean.IndexOperationParams;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vip.pallas.mybatis.entity.IndexOperation;
import com.vip.pallas.mybatis.entity.IndexOperationExample;
import com.vip.pallas.mybatis.repository.IndexOperationRepository;
import com.vip.pallas.service.IndexOperationService;

@Service
@Transactional(rollbackFor=Exception.class)  
public class IndexOperationServiceImpl implements IndexOperationService{
	
	@Resource
    private IndexOperationRepository indexOperationRepository;

	@Override
	public long countByExample(IndexOperationExample example) {
		return indexOperationRepository.countByExample(example);
	}

	@Override
	public int deleteByPrimaryKey(Long id) {
		return indexOperationRepository.deleteByPrimaryKey(id);
	}

	@Override
	public int insert(IndexOperation record) {
		return indexOperationRepository.insert(record);
	}


	@Override
	public List<IndexOperation> selectByExampleWithBLOBs(
			IndexOperationExample example) {
		return indexOperationRepository.selectByExampleWithBLOBs(example);
	}

	@Override
	public int deleteByCondition(IndexOperationParams params) {
		return indexOperationRepository.deleteByCondition(params);
	}

}