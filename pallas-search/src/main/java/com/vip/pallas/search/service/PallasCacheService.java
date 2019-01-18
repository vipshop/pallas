package com.vip.pallas.search.service;

import java.util.List;
import java.util.concurrent.ExecutionException;

import com.vip.pallas.search.model.*;

public interface PallasCacheService {

    /**
     * 初始化cache
     */
    void initCache() throws ExecutionException;

    /**
     * 刷新路由规则
     */
    void refreshRouting();

    /**
     * 让cache失效
     */
    void invalidateCache();

    /**
     * 根据indexName查询所有相关可用节点IP
     * @param indexName
     * @return
     */
    List<String> getAvailableNodesByIndex(String indexName) throws ExecutionException;

    /**
     * 根据clusterName查询所有相关可用节点IP
     * @param clusterName
     * @return
     */
    List<String> getAvailableNodesByCluster(String clusterName) throws ExecutionException;

    /**
     * 根据indexName和逻辑Cluster查询所有相关可用路由规则
     * @param indexName
     * @param clusterId
     * @return
     * @throws ExecutionException
     */
    List<IndexRouting> getIndexLevelRoutingByIndexNameAndCluster(String indexName, String clusterId) throws ExecutionException;

    /**
     * 根据物理Cluster查询所有相关可用路由规则
     * @param clusterId
     * @return
     * @throws ExecutionException
     */
    List<IndexRouting> getClusterLevelRoutingByIndexNameAndCluster(String clusterId) throws ExecutionException;

    /**
     * 根据indexName和逻辑Cluster查询所有相关可用路由目标
     * @param indexId
     * @return
     * @throws ExecutionException
     */
    List<IndexRoutingTargetGroup> getTargetGroupByIndexId(Long indexId) throws ExecutionException;

    /**
     * 根据targetGroupId查询TargetGroup
     * @param targetGroupId
     * @return
     * @throws ExecutionException
     */
    IndexRoutingTargetGroup getTargetGroupById(Long targetGroupId) throws ExecutionException;

    /**
     * 根据提供的Token信息返回这个Token的授权对象
     * @param clientToken
     * @return
     * @throws ExecutionException
     */
    SearchAuthorization getSearchAuthorization(String clientToken) throws ExecutionException;

    /**
     * 根据indexName查询所有相关集群信息
     * @param indexName
     * @return
     * @throws ExecutionException
     */
//    Cluster getClusterByIndexName(String indexName) throws ExecutionException;

    /**
     * 根据indexName查询集群HTTP端口
     * @param indexName
     * @return
     * @throws ExecutionException
     */
    String getClusterPortByIndexAndCluster(String indexName, String clusterName);

    /**
     * 根据clusterName查询所有相关可用节点IP
     * @param clusterName
     * @return
     */
    List<String> getAllNodeListByClusterName(String clusterName) throws ExecutionException;

    /**
     * 根据clusterName查询所有动态绑定节点IP
     * @param clusterName
     * @return
     */
    List<String> getShardNodeListByIndexAndCluster(String indexName, String clusterName) throws ExecutionException;

    /**
     * 根据别名获取原始索引名字列表
     * @param indexName
     * @param clusterName
     * @return
     * @throws ExecutionException
     */
    List<String> getSourceIndexByIndexAndCluster(String indexName, String clusterName) throws ExecutionException;
    
    TemplateWithTimeoutRetry getConfigByTemplateIdAndCluster(String templateId, String clusterName, String indexName) throws ExecutionException;

    /**
     * 根据索引及集群名获取索引信息，包括索引配置信息
     * @param indexName
     * @return
     * @throws ExecutionException
     */
    Index getIndexByIndexAndCluster(String indexName, String clusterName);

    /**
     * 尝试解析出请求到达的ES domain 如果用户没提供，而数据库只有一条索引记录，也返回该记录的ES集群
     * @param indexName
     * @param providedClusterId 用户提供的集群，通过pallas aop传入
     * @return
     * @throws ExecutionException
     */
    String tryToExtractClusterId(String indexName, String providedClusterId) throws ExecutionException;

    /**
     * 根据clusterName 来查找ES集群的端口
     * @param clusterName
     * @return
     * @throws ExecutionException
     */
    String getClusterPortByClusterName(String clusterName);

	/**
	 * 根据集群名、索引名、模板名获取流量记录信息
	 * @param clusterName
	 * @param indexName
	 * @param templateName
	 * @return
	 * @throws ExecutionException
	 */
	List<FlowRecord> getFlowRecord(String clusterName, String indexName, String templateName) throws ExecutionException;

	/**
	 * 根据ID查询流量记录信息
	 * @param recordId
	 * @return
	 * @throws ExecutionException
	 */
	FlowRecord getFlowRecordById(Long recordId) throws ExecutionException;
}
