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

package com.vip.pallas.console.controller.index;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vip.pallas.bean.IndexOperationEventName;
import com.vip.pallas.bean.IndexOperationEventType;
import com.vip.pallas.console.utils.AuditLogUtil;
import com.vip.pallas.console.utils.AuthorizeUtil;
import com.vip.pallas.console.utils.SessionUtil;
import com.vip.pallas.console.vo.IndexBaseVO;
import com.vip.pallas.console.vo.IndexOp;
import com.vip.pallas.console.vo.IndexVO;
import com.vip.pallas.console.vo.PageResultVO;
import com.vip.pallas.console.vo.base.BaseIndexOp;
import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.exception.PallasException;
import com.vip.pallas.mybatis.entity.Cluster;
import com.vip.pallas.mybatis.entity.DataSource;
import com.vip.pallas.mybatis.entity.Index;
import com.vip.pallas.mybatis.entity.IndexOperation;
import com.vip.pallas.mybatis.entity.IndexRouting;
import com.vip.pallas.mybatis.entity.IndexRoutingTargetGroup;
import com.vip.pallas.mybatis.entity.Page;
import com.vip.pallas.orika.OrikaBeanMapper;
import com.vip.pallas.service.ClusterService;
import com.vip.pallas.service.IndexOperationService;
import com.vip.pallas.service.IndexRoutingService;
import com.vip.pallas.service.IndexService;
import com.vip.pallas.utils.ObjectMapTool;

@Validated
@RestController
@RequestMapping("/index")
public class IndexController {
    private static Logger logger = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    private IndexService indexService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private IndexRoutingService routingService;

    @Autowired
    private IndexOperationService indexOperationService;

    @Autowired
    private OrikaBeanMapper beanMapper;

    @RequestMapping(value = "/page.json", method = RequestMethod.GET)
    public PageResultVO<Index> page(@RequestParam(required = false, defaultValue = "1") @Min(value = 0, message = "currentPage必须为正数") Integer currentPage,
                     @RequestParam(required = false, defaultValue = "10") @Min(value = 0, message = "pageSize必须为正数") Integer pageSize,
                     @RequestParam(required = false) String clusterId,
                     @RequestParam(required = false) String indexName) {
        Page<Index> page = new Page<>();
        page.setPageNo(currentPage);
        page.setPageSize(pageSize);

        PageResultVO<Index> resultVO = new PageResultVO<>();

        List<Index> list = indexService.findPage(page, indexName, clusterId);
        List<String> privileges = AuthorizeUtil.loadPrivileges();

        if(CollectionUtils.isNotEmpty(list)){
            if(CollectionUtils.isNotEmpty(privileges)){
                for(Index index : list){
                    if(privileges.contains("index.all") || privileges.contains("index." + index.getId() + "-" + index.getIndexName())){
                        index.setHasPrivilege(true);
                    }
                    if(privileges.contains("cluster.all") || privileges.contains("cluster."+ index.getClusterName())){
                        index.setHasClusterPrivilege(true);
                    }
                }
            }
        }

        if(privileges != null && privileges.contains("index.all")){
            resultVO.setAllPrivilege(true);
        }

        resultVO.setList(list);
        resultVO.setTotal(page.getTotalRecord());
        resultVO.setPageCount(page.getTotalPage());

        return resultVO;
    }

    @RequestMapping(value = "/id.json", method = RequestMethod.GET)
	public Index findById(@RequestParam @NotNull(message = "indexId不能为空") @Min(value = 1, message = "indexId必须大于0") Long indexId) {
        return indexService.findById(indexId);
    }

    @RequestMapping(value = "add.json", method = RequestMethod.POST)
    public String insert(@RequestBody @Validated IndexVO params, HttpServletRequest request) throws Exception{
        if (!AuthorizeUtil.authorizeIndexPrivilege(request, null, null)){
        	throw new BusinessLevelException(403, "无权限操作");
        }
        // check if there are indexes with the same name.
        if (!params.getConfirm()) {
            List<Index> findByIndexName = indexService.findByIndexName(params.getIndexName());
            if (findByIndexName != null && !findByIndexName.isEmpty()) {
                StringBuilder sb = new StringBuilder(64);
                for (Index idx : findByIndexName) {
                    sb.append(idx.getClusterName()).append(' ');
                }
                return "同名索引: " + params.getIndexName() + " 存在于以下域中 "+ sb.toString()+"，你确定要添加?";
            }
        }
        List<DataSource> dsList = params.getDataSourceList() == null ? new ArrayList<>(): params.getDataSourceList();

        String createUser = SessionUtil.getLoginUser(request);
        Index index = getIndex(params, createUser);

        Index findByClusterNameAndIndexName = indexService.findByClusterNameAndIndexName(index.getClusterName(), index.getIndexName());
        if (findByClusterNameAndIndexName != null) {
            throw new BusinessLevelException(500, "indexName=" + index.getIndexName() + ", cluserName=" + index.getClusterName() + " 的index已经存在，插入失败。");
        }
        indexService.insert(index, dsList);
        //#337 创建好索引创建默认路由
        insertDefaultRoutingForIndex(index);
        try {
            AuditLogUtil.log("add index: indexName - {0}, cluster - {1}, data source list: {2}",
                    index.getIndexName(),
                    index.getClusterName(),
                    dsList);

            IndexOperation record = new IndexOperation();
            record.setEventDetail(index + "  " + dsList);
            record.setEventName(IndexOperationEventName.CREATE_INDEX);
            record.setEventType(IndexOperationEventType.INDEX_EVENT);
            record.setOperationTime(new Date());
            record.setIndexId(index.getId());
            record.setOperator(createUser);
            indexOperationService.insert(record);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

	@RequestMapping(value = "/loadDbList.json", method = RequestMethod.POST)
	public Map loadDbList(@RequestBody Map<String, Object> params) throws SQLException, PallasException { // NOSONAR
		Long indexId = ObjectMapTool.getLong(params, "indexId");
		Index index = indexService.findById(indexId);
		Map<Long, String> idDbTbMap = new HashMap<>();
		if (index.getDataSourceList() != null) {
			for (DataSource ds : index.getDataSourceList()) {
				idDbTbMap.put(ds.getId(),
						ds.getIp() + ":" + ds.getPort() + "/" + ds.getDbname() + "/" + ds.getTableName());
			}
		}
		return idDbTbMap;
	}

    @RequestMapping(value = "/update.json", method = RequestMethod.POST)
	public String update(@RequestBody @Validated IndexVO params, HttpServletRequest request) throws Exception {
        if(params.getIndexId() == null) {
            throw new BusinessLevelException(500, "indexId不能为空");
        }
        Index dbIndex = indexService.findById(params.getIndexId());
        if (dbIndex == null) {
            throw new BusinessLevelException(500, "index不存在");
        }
        if (!AuthorizeUtil.authorizeIndexPrivilege(request, dbIndex.getId(), dbIndex.getIndexName())) {
        	throw new BusinessLevelException(403, "无权限操作");
        }
        
        String createUser = SessionUtil.getLoginUser(request);
        Index index = getIndex(params, createUser);
        index.setId(params.getIndexId());

        List<DataSource> dsList = params.getDataSourceList() == null ? new ArrayList<>(): params.getDataSourceList();
        try {
			indexService.update(index, dsList, params.getConfirm());
            AuditLogUtil.log("update index: id - {0}, indexName - {1}, cluster - {2}, dsList - {3}",
                    index.getId(),
                    index.getIndexName(),
                    index.getClusterName(),
                    dsList);

            IndexOperation record = new IndexOperation();
            record.setEventDetail(index + "  " + dsList);
            record.setEventName(IndexOperationEventName.UPDATE_INDEX);
            record.setEventType(IndexOperationEventType.INDEX_EVENT);
            record.setOperationTime(new Date());
            record.setIndexId(params.getIndexId());
            record.setOperator(createUser);
            indexOperationService.insert(record);
		} catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
			return e.getMessage();
        }
		return "更新成功";
    }

    @RequestMapping(value = "/delete/id.json", method = RequestMethod.POST)
    public void deleteById(@RequestBody BaseIndexOp params, HttpServletRequest request) throws PallasException {
        Long indexId = params.getIndexId();
        Index index = indexService.findById(indexId);
        if (index == null) {
            throw new BusinessLevelException(500, "index不存在");
        }
        if (!AuthorizeUtil.authorizeIndexPrivilege(request, index.getId(), index.getIndexName())) {
        	throw new BusinessLevelException(403, "无权限操作");
        }
        
        indexService.deleteById(indexId);
        AuditLogUtil.log("delete index: id - {0}, name - {1}, cluster - {2}", indexId, index.getIndexName(), index.getClusterName());

        IndexOperation record = new IndexOperation();
        record.setEventDetail("delete index: id - " + indexId +", name - " + index.getIndexName() + ", cluster - " + index.getClusterName());
        record.setEventName(IndexOperationEventName.DELETE_INDEX);
        record.setEventType(IndexOperationEventType.INDEX_EVENT);
        record.setOperationTime(new Date());
        record.setIndexId(indexId);
        record.setOperator(SessionUtil.getLoginUser(request));
        indexOperationService.insert(record);

    }

    @RequestMapping(value = "/update/timeout_retry.json", method = RequestMethod.POST)
    public void updateTimeoutRetry(@RequestBody IndexOp params, HttpServletRequest request) {
        Long indexId =  params.getIndexId();
        if(ObjectUtils.isEmpty(indexId)){
            throw new BusinessLevelException(500, "indexId不能为空");
        }
        Integer retry = params.getRetry();
        if(ObjectUtils.isEmpty(retry)){
            throw new BusinessLevelException(500, "retry不能为空");
        }
        Integer timeout = params.getTimeout();
        if(ObjectUtils.isEmpty(timeout)){
            throw new BusinessLevelException(500, "timeout不能为空");
        }
        Integer slowerThan = params.getSlowerThan();
        if(ObjectUtils.isEmpty(slowerThan)){
            throw new BusinessLevelException(500, "slowerThan不能为空");
        }
        
        Index dbIndex = indexService.findById(indexId);
        if (dbIndex == null) {
            throw new BusinessLevelException(500, "index不存在");
        }
        if (!AuthorizeUtil.authorizeIndexPrivilege(request, dbIndex.getId(), dbIndex.getIndexName())) {
        	throw new BusinessLevelException(403, "无权限操作");
        }

        Index index = new Index();
        index.setId(indexId);
        index.setTimeout(timeout);
        index.setRetry(retry);
        index.setSlowerThan(slowerThan);

        indexService.updateById(index);

        try {
            AuditLogUtil.log("update index: id - {0}, indexName - {1}, timeout - {2}, retry - {3}",
                    index.getId(),
                    index.getIndexName(),
                    index.getTimeout(),
                    index.getRetry());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @RequestMapping(value = "/all.json", method = RequestMethod.GET)
    public List<IndexBaseVO> getAllIndex() {
        List<Index> result = indexService.findAllSpecificdFiled();
        if(CollectionUtils.isEmpty(result)) {
            return new ArrayList<>();
        }
        return beanMapper.mapAsList(result, IndexBaseVO.class);

    }


    private Index getIndex(IndexVO params, String createUser) {
        Index index = new Index();
        index.setDescription(params.getDescription());
        index.setClusterName(params.getClusterId());
        index.setIndexName(params.getIndexName());
        index.setStat("inactive");
        index.setCreateUser(createUser);

        return index;
    }

    private IndexRouting insertDefaultRoutingForIndex(Index index) throws Exception {
        IndexRouting routing = routingService.getIndexRouting(index.getId(), IndexRouting.ROUTE_TYPE_INDEX);
        if (routing == null) {
            List<Cluster> clusters = clusterService.selectPhysicalClustersByIndexId(index.getId());
            List<IndexRoutingTargetGroup> groups = IndexRoutingTargetGroup.genDefault(index, clusters);
            groups.forEach(group -> routingService.addOrUpdateRoutingTargetGroup(index.getId(), group));
            routing = IndexRouting.genDefault(index, groups);
            routingService.addOrUpdateIndexRouting(index.getId(), routing);
        }
        return routing;
    }

}