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

package com.vip.pallas.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.vip.pallas.bean.IndexParam;
import com.vip.pallas.exception.PallasException;
import com.vip.pallas.mybatis.entity.Cluster;
import com.vip.pallas.mybatis.entity.DataSource;
import com.vip.pallas.mybatis.entity.Index;
import com.vip.pallas.mybatis.entity.Page;
import com.vip.pallas.mybatis.repository.DataSourceRepository;
import com.vip.pallas.mybatis.repository.IndexRepository;
import com.vip.pallas.mybatis.repository.IndexVersionRepository;
import com.vip.pallas.mybatis.repository.MappingRepository;
import com.vip.pallas.utils.JdbcUtil;
import com.vip.pallas.utils.PallasConsoleProperties;

public abstract class IndexService {

	private static final Logger logger = LoggerFactory.getLogger(IndexService.class);

	@Resource
	private IndexRepository indexRepository;

	@Resource
	public MappingRepository mappingRepository;

	@Resource
	public IndexVersionRepository indexVersionRepository;

	@Resource
	private DataSourceRepository dataSourceRepository;

	@Resource
	private ClusterService clusterService;

	@Autowired
	private PrivilegeService privilegeService;

	public void insert(Index index, List<DataSource> dataSourceList) throws PallasException {

		Date date = new Date();
		
		for (DataSource ds : dataSourceList) {
			if (!JdbcUtil.testDBConnect(ds.getIp(), ds.getPort(), ds.getDbname(), ds.getUsername(),
					decodePassword(ds.getPassword()))) {
				throw new PallasException("测试DB连接失败，ip:" + ds.getIp() + ", port:" + ds.getPort() + ", db:"
						+ ds.getDbname() + ", user:" + ds.getUsername());
			}
			ds.setCreateTime(date);
			ds.setUpdateTime(date);
		}

		index.setCreateTime(date);
		index.setUpdateTime(date);
		index.setSlowerThan(PallasConsoleProperties.DEFAULT_INDEX_SLOWER_THAN);

		try {
			indexRepository.insert(index);
			for (DataSource ds : dataSourceList) {
				ds.setIndexId(index.getId());
				dataSourceRepository.insert(ds);
			}

			String privilegeName = index.getId() + "-" + index.getIndexName();
			privilegeService.createIndexPrivilege(privilegeName);
			privilegeService.createVersionPrivilege(privilegeName);
			privilegeService.createTemplatePrivilege(privilegeName);
		} catch (Exception e) {
		    logger.error(e.toString(), e);
		    String message = e.toString();

			if (StringUtils.isNotEmpty(message) && message.indexOf("Duplicate entry") >= 0) {
				throw new PallasException("索引" + index.getId() + "已存在");
			} else {
				throw new PallasException(message);
			}
		}
	}

	public void update(Index index, List<DataSource> dataSourceList, boolean confirm) throws PallasException {
		if (!confirm) {
			int versionCount = indexRepository.getVersionCountByIndexId(index.getId());
			if (versionCount > 0) {
				throw new IllegalArgumentException("索引" + index.getId() + "目前存在" + versionCount + "个关联版本，你确认要更新吗");
			}
		}
		for (DataSource ds : dataSourceList) {
			if (!JdbcUtil.testDBConnect(ds.getIp(), ds.getPort(), ds.getDbname(), ds.getUsername(),
					decodePassword(ds.getPassword()))) {
				throw new PallasException("测试DB连接失败，ip:" + ds.getIp() + ", port:" + ds.getPort() + ", db:"
						+ ds.getDbname() + ", user:" + ds.getUsername());
			}
		}
		Date date = new Date();
		List<DataSource> dsListInDb = dataSourceRepository.selectByIndexId(index.getId());
		// delete all the datasources in db.
		for (DataSource dsInDb : dsListInDb) {
			dataSourceRepository.deleteByPrimaryKey(dsInDb.getId());
		}
		for (DataSource ds : dataSourceList) {
			ds.setUpdateTime(date);
			ds.setIndexId(index.getId());
			ds.setCreateTime(date);
			dataSourceRepository.insert(ds);
		}

		index.setUpdateTime(date);
		indexRepository.updateByPrimaryKeySelective(index);
	}

	public Index findById(Long id) {
		return indexRepository.selectByid(id);
	}

	public List<Index> findAll() {
		return indexRepository.selectAll();
	}

	public List<Index> findAllSpecificdFiled() {
		return indexRepository.selectAllSpecificdFiled();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Index> findPage(Page page, String indexName, String clusterId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("indexName", indexName);
		params.put("clusterId", clusterId);
		page.setParams(params);

		return indexRepository.selectPage(page);
	}


	public void deleteById(Long id) throws PallasException {
		int versionCount = indexRepository.getVersionCountByIndexId(id);

		if (versionCount > 0) {
			throw new PallasException("索引" + id + "目前存在" + versionCount + "个关联版本，不允许删除索引");
		}
		Index index = indexRepository.selectByid(id);
		String privilegeKey = index.getId() + "-" + index.getIndexName();

		indexRepository.deleteByPrimaryKey(id);
		dataSourceRepository.deleteByIndexId(id);

		try {
			privilegeService.deleteIndexPrivilege(privilegeKey);
			privilegeService.deleteIndexAsset(privilegeKey);
		} catch (Exception e) {
			logger.error(e.getClass() + " " + e.getMessage(), e);
		}
	}

	public Index findByClusterNameAndIndexName(String clusterName, String indexName) {
		return indexRepository.findByClusterNameAndIndexName(clusterName, indexName);
	}

	public List<Index> findByIndexName(String indexName) {
		return indexRepository.findByIndexName(indexName);
	}

	public void updateById(Index index) {
		indexRepository.updateByPrimaryKeySelective(index);
	}

	public IndexParam getIndexParamByVersionId(Long versionId) {
		return null;
	}

	public String decodePassword(String password) {
		return password;
	}

	public boolean isLogicalIndex(Long indexId) {
		Index index = findById(indexId);
		Cluster cluster = clusterService.findByName(index.getClusterName());
		return cluster.isLogicalCluster();
	}

	public Index findByQueueName(String queueName){
		Long indexId = indexVersionRepository.findIndexIdByVdpQueue(queueName);
		return indexId == null ? null : indexRepository.selectByid(indexId);
	}
}