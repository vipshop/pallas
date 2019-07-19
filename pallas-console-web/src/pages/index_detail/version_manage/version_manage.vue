<template>
    <div class="my-tab-content">
        <div class="content" v-show="isAllPrivilege">
            <el-button type="primary" icon="plus" @click="handleAdd">新增版本</el-button>
        </div>
        <div class="content">
            <template>
                <el-table :data="versionList" border style="width: 100%" v-loading="loading" element-loading-text="请稍等···">
                    <el-table-column label="版本id" prop="id" width="150px"></el-table-column>
                    <el-table-column label="所属集群" prop="realClusterIds">
                        <template slot-scope="scope">
                            <div class="condition-table" v-for="item in $array.strToArray(scope.row.realClusterIds)" :key="item">
                                <router-link tag="a" :to="{ name:'cluster_detail',query:{clusterId: getClusterName(item)} }">{{getClusterName(item)}}</router-link>
                                <el-tooltip effect="dark" content="查看配置信息" placement="top">
                                    <el-button type="text" @click="viewConfigInfo(scope.row, item)"><i class="fa fa-cog"></i></el-button>
                                </el-tooltip>
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column label="数据量" prop="count" width="150px">
                        <template slot-scope="scope">
                            <div class="condition-table" v-for="(item, index) in scope.row.count" :key="item">{{item}}
                                <el-tooltip effect="dark" content="快速查看数据" placement="top">
                                    <el-button type="text" @click="retrieve(scope.row, $array.strToArray(scope.row.realClusterIds)[index])"><i class="fa fa-search-plus"></i></el-button>
                                </el-tooltip>
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column label="创建时间" prop="createTime" width="200px">
                        <template slot-scope="scope">{{scope.row.createTime | formatDate}}</template>
                    </el-table-column>
                    <el-table-column label="是否启用" prop="isUsed" width="150px">
                        <template slot-scope="scope"> 
                            <el-tag :type="scope.row.isUsed ? 'success' : 'danger'" close-transition>{{scope.row.isUsed || false | translateIsUsed}}</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="80" v-if="isAllPrivilege">
                        <template slot-scope="scope">
                            <el-dropdown trigger="click">
                              <span class="el-dropdown-link">
                                操作<i class="el-icon-caret-bottom el-icon--right"></i>
                              </span>
                              <el-dropdown-menu class="dropdown-operation" slot="dropdown">
                                <el-dropdown-item v-show="!scope.row.isSync"><a @click="createIndex(scope.row)"><span><i class="fa fa-play-circle"></i>创建索引</span></a></el-dropdown-item>
                                <el-dropdown-item v-show="!scope.row.isUsed"><a @click="enableVersion(scope.row)"><span><i class="fa fa-hand-o-right"></i>启用版本</span></a></el-dropdown-item>
                                <el-dropdown-item><a @click="triggerDialog(scope.row, 'view')"><span><i class="fa fa-eye"></i>配置查看</span></a></el-dropdown-item>
                                  <el-dropdown-item v-show="scope.row.isSync"><a @click="triggerDialog(scope.row, 'dynamic_edit')"><span><i class="fa fa-pencil-square-o"></i>动态配置更改</span></a></el-dropdown-item>
                                <el-dropdown-item v-show="!scope.row.isSync"><a @click="triggerDialog(scope.row, 'edit')"><span><i class="fa fa-pencil-square-o"></i>配置更改</span></a></el-dropdown-item>
                                <el-dropdown-item><a @click="copyVersion(scope.row, 'copy')"><span><i class="fa fa-clone"></i>版本复制</span></a></el-dropdown-item>
                                <el-dropdown-item><a @click="preheading(scope.row)"><span><i class="fa fa-sun-o"></i>索引预热</span></a></el-dropdown-item>
                                <el-dropdown-item v-show="scope.row.isSync"><a @click="stopSync(scope.row)"><span><i class="fa fa-stop-circle-o"></i>删除索引</span></a></el-dropdown-item>
                                <el-dropdown-item v-show="!scope.row.isSync"><a @click="handleDelete(scope.row)"><span><i class="fa fa-trash"></i>删除版本</span></a></el-dropdown-item>
                              </el-dropdown-menu>
                            </el-dropdown>
                        </template>
                    </el-table-column>
                </el-table>
            </template>
            <div class="my-pagination" v-if="versionData.total != 0">
                <el-pagination layout="prev, pager, next" :total="versionData.total" :page-size="pageSize" :current-page="currentPage" @current-change="changePage"></el-pagination>
            </div>
        </div>
        <div v-if="isVersionInfoVisible">
            <version-info-dialog :version-list="versionList" :version-operation="versionOperation" :version-info-title="versionInfoTitle" :version-info="versionInfo" :is-logical="isLogical" :clusters="clusters" :is-meta-data-null="isMetaDataNull" @close-dialog="closeDialog" @template-operate-success="templateOperateSuccess"></version-info-dialog>
        </div>
        <div v-if="isVersionDynamicInfoVisible">
            <version-dynamic-info-dialog :version-operation="versionOperation" :version-info-title="versionInfoTitle" :version-info="versionInfo" :is-logical="isLogical" :clusters="clusters"  @close-dialog="closeDynamicDialog" @template-operate-success="dynamicUpdateOperateSuccess"></version-dynamic-info-dialog>
        </div>
        <div v-if="isViewConfigVisible">
            <json-content-dialog :content="configInfo" :title="configTitle" @close-dialog="closeViewConfigDialog"></json-content-dialog>
        </div>
        <div v-if="isPreheadingVisible">
            <preheading-dialog :preheading-info="preheadingInfo" @close-dialog="closePreheadingDialog"></preheading-dialog>
        </div>
    </div>
</template>

<script>
import VersionInfoDialog from './version_info_dialog/version_info_dialog';
import VersionDynamicInfoDialog from './version_info_dialog/version_dynamic_info_dialog';
import PreheadingDialog from './preheading_dialog';

export default {
  components: {
    'version-info-dialog': VersionInfoDialog,
    'version-dynamic-info-dialog': VersionDynamicInfoDialog,
    'preheading-dialog': PreheadingDialog,
  },
  data() {
    return {
      loading: false,
      indexId: this.$route.query.indexId,
      indexName: this.$route.query.indexName,
      isMetaDataNull: false,
      isAllPrivilege: false,
      isVersionInfoVisible: false,
      isVersionDynamicInfoVisible: false,
      versionInfoTitle: '',
      versionOperation: '',
      isStartSyncVisible: false,
      isViewConfigVisible: false,
      isStartFullImportVisible: false,
      configInfo: '',
      configTitle: '',
      pageSize: 10,
      currentPage: 1,
      versionData: {},
      versionList: [],
      versionInfo: {},
      versionGetInfo: {},
      versionAddInfo: {
        indexId: '',
        shardNum: 1,
        replicationNum: 1,
        vdpQueue: '',
        routingKey: 'id',
        idField: 'id',
        updateTimeField: 'update_time',
        filterFields: false,
        vdp: 1,
        checkSum: false,
        schema: [],
        clusterId: '',
        nodes: [],
        indexSlowThreshold: 200,
        fetchSlowThreshold: 200,
        querySlowThreshold: 200,
        refreshInterval: 60,
        maxResultWindow: 10000,
        totalShardsPerNode: -1,
        flushThresholdSize: '512mb',
        syncInterval: '5s',
        translogDurability: 'async',
        sourceDisabled: false,
        sourceIncludes: '',
        sourceExcludes: '',
        sourceIncludesArr: [],
        sourceExcludesArr: [],
      },
      clusters: [],
      isLogical: false,
      isPreheadingVisible: false,
      preheadingInfo: {},
    };
  },
  methods: {
    preheading(row) {
      this.preheadingInfo = {
        versionId: row.id,
        isUsed: row.isUsed,
      };
      this.isPreheadingVisible = true;
    },
    closePreheadingDialog() {
      this.isPreheadingVisible = false;
    },
    getClusterName(id) {
      let clusterName = '';
      this.clusters.some((ele) => {
        if (ele.id === Number(id)) {
          clusterName = ele.clusterId;
          return true;
        }
        return false;
      });
      return clusterName;
    },
    viewConfigInfo(row, clusterId) {
      const params = {
        indexId: this.indexId,
        indexName: this.indexName,
        versionId: row.id,
        cid: clusterId,
      };
      this.loading = true;
      this.$http.get('/index/version/info.json', params).then((data) => {
        this.configInfo = data;
        this.configTitle = `${this.indexName}_${row.id}配置信息（集群：${this.getClusterName(clusterId)}）`;
        this.isViewConfigVisible = true;
      })
      .finally(() => {
        this.loading = false;
      });
    },
    retrieve(row, clusterId) {
      const params = {
        indexId: this.indexId,
        indexName: this.indexName,
        versionId: row.id,
        cid: clusterId,
      };
      this.loading = true;
      this.$http.get('/index/version/retrieve.json', params).then((data) => {
        this.configInfo = data;
        this.configTitle = `${this.indexName}_${row.id}索引数据（集群：${this.getClusterName(clusterId)}）`;
        this.isViewConfigVisible = true;
      })
      .finally(() => {
        this.loading = false;
      });
    },
    closeViewConfigDialog() {
      this.isViewConfigVisible = false;
    },
    createIndex(row) {
      this.loading = true;
      this.$http.post('/index/version/create_index.json', { indexId: this.indexId, versionId: row.id }).then(() => {
        this.$message.successMessage('生成索引成功', () => {
          this.getVersionList();
        });
      })
      .finally(() => {
        this.loading = false;
      });
    },
    enableVersion(row) {
      this.$message.confirmMessage(`确定启用版本${row.id}吗?`, () => {
        this.loading = true;
        this.$http.post('/index/version/enable.json', { indexId: this.indexId, versionId: row.id }).then(() => {
          this.$message.successMessage('启用版本成功', () => {
            this.getVersionList();
          });
        })
        .finally(() => {
          this.loading = false;
        });
      });
    },
    handleAdd() {
      this.isVersionInfoVisible = true;
      this.versionInfoTitle = '新增版本';
      this.versionOperation = 'add';
      this.versionAddInfo.indexId = this.indexId;
      this.versionAddInfo.clusterId = this.clusters[0].id;
      this.versionInfo = JSON.parse(JSON.stringify(this.versionAddInfo));
    },
    triggerDialog(row, operation) {
      this.loading = true;
      this.$http.get(`/index/version/id.json?versionId=${row.id}`).then((data) => {
        this.versionGetInfo = data;
        Object.keys(this.versionGetInfo.schema).forEach((element, index) => {
          this.versionGetInfo.schema[index].children =
          this.versionGetInfo.schema[index].children || [];
          this.versionGetInfo.schema[index].multiField =
          this.versionGetInfo.schema[index].multiField || [];
          this.versionGetInfo.schema[index].copyTo =
          this.versionGetInfo.schema[index].copyTo || [];
          this.versionGetInfo.versionId = row.id;
        });
        this.$set(this.versionGetInfo, 'versionId', this.versionGetInfo.id);
        this.$set(this.versionGetInfo, 'clusterId', this.clusters[0].id);
        if (this.isLogical) {
          this.$set(this.versionGetInfo, 'nodes', this.getLogicClusterNodesArray(this.versionGetInfo.allocationNodes));
        } else {
          this.$set(this.versionGetInfo, 'nodes', this.getNodesArray(this.versionGetInfo.allocationNodes));
        }
        this.versionGetInfo.sourceIncludesArr = [];
        this.versionGetInfo.sourceExcludesArr = [];
        if (this.versionGetInfo.sourceIncludes && this.versionGetInfo.sourceIncludes.length > 0) {
          this.$set(this.versionGetInfo, 'sourceIncludesArr', this.getNodesArray(this.versionGetInfo.sourceIncludes));
        }
        if (this.versionGetInfo.sourceExcludes && this.versionGetInfo.sourceExcludes.length > 0) {
          this.$set(this.versionGetInfo, 'sourceExcludesArr', this.getNodesArray(this.versionGetInfo.sourceExcludes));
        }
        this.versionInfo = JSON.parse(JSON.stringify(this.versionGetInfo));
        if (operation === 'dynamic_edit') {
          this.versionInfoTitle = '编辑版本动态配置';
          this.isVersionDynamicInfoVisible = true;
          this.versionOperation = operation;
          const clusterName = this.getClusterName(
              this.$array.strToArray(row.realClusterIds)[0]);
          this.$http.get(`/cluster/id.json?clusterId=${clusterName}`).then((clusterData) => {
            if (clusterData) {
              const hostParam = `http://${this.$array.strToArray(clusterData.httpAddress)[0]}`;
              const indexParam = `${this.indexName}_${row.id}`;
              this.$http.postCerebro('/cerebro/commons/get_index_settings', { host: hostParam, index: indexParam }).then((settings) => {
                const settingData = settings;
                const settingInfo = settingData[indexParam].settings.index;
                this.versionInfo.replicationNum = +settingInfo.number_of_replicas;
                this.versionInfo.indexSlowThreshold = +settingInfo.indexing.slowlog.threshold.index.info.replace('ms', '');
                this.versionInfo.fetchSlowThreshold = +settingInfo.search.slowlog.threshold.fetch.info.replace('ms', '');
                this.versionInfo.querySlowThreshold = +settingInfo.search.slowlog.threshold.query.info.replace('ms', '');
                this.versionInfo.refreshInterval = +settingInfo.refresh_interval.replace('s', '');
                this.versionInfo.maxResultWindow = +settingInfo.max_result_window;
                this.versionInfo.totalShardsPerNode = +settingInfo
                  .routing.allocation.total_shards_per_node;
                this.versionInfo.flushThresholdSize = settingInfo.translog.flush_threshold_size;
                this.versionInfo.syncInterval = settingInfo.translog.sync_interval;
                this.versionInfo.translogDurability = settingInfo.translog.durability;
              });
            }
          });
          return;
        }
        if (operation === 'edit') {
          this.versionInfoTitle = '编辑版本';
        } else if (operation === 'copy') {
          this.versionInfoTitle = `复制版本(From ${row.id})`;
        } else {
          this.versionInfoTitle = '查看版本';
        }
        this.versionOperation = operation;
        this.isVersionInfoVisible = true;
      })
      .finally(() => {
        this.loading = false;
      });
    },
    copyVersion(row, operation) {
      this.loading = true;
      this.$http.post('/index/version/copy.json', {
        versionId: row.id,
        indexId: this.indexId,
      }).then((data) => {
        this.versionGetInfo = data;
        Object.keys(this.versionGetInfo.schema).forEach((element, index) => {
          this.versionGetInfo.schema[index].children =
          this.versionGetInfo.schema[index].children || [];
          this.versionGetInfo.schema[index].multiField =
          this.versionGetInfo.schema[index].multiField || [];
          this.versionGetInfo.schema[index].copyTo =
          this.versionGetInfo.schema[index].copyTo || [];
          this.versionGetInfo.versionId = row.id;
        });
        this.$set(this.versionGetInfo, 'versionId', this.versionGetInfo.id);
        this.$set(this.versionGetInfo, 'clusterId', this.clusters[0].id);
        if (this.isLogical) {
          this.$set(this.versionGetInfo, 'nodes', this.getLogicClusterNodesArray(this.versionGetInfo.allocationNodes));
        } else {
          this.$set(this.versionGetInfo, 'nodes', this.getNodesArray(this.versionGetInfo.allocationNodes));
        }
        this.versionGetInfo.sourceIncludesArr = [];
        this.versionGetInfo.sourceExcludesArr = [];
        if (this.versionGetInfo.sourceIncludes && this.versionGetInfo.sourceIncludes.length > 0) {
          this.$set(this.versionGetInfo, 'sourceIncludesArr', this.getNodesArray(this.versionGetInfo.sourceIncludes));
        }
        if (this.versionGetInfo.sourceExcludes && this.versionGetInfo.sourceExcludes.length > 0) {
          this.$set(this.versionGetInfo, 'sourceExcludesArr', this.getNodesArray(this.versionGetInfo.sourceExcludes));
        }
        this.versionInfo = JSON.parse(JSON.stringify(this.versionGetInfo));
        this.versionInfoTitle = `复制版本(From ${row.id})`;
        this.versionOperation = operation;
        this.isVersionInfoVisible = true;
      })
      .finally(() => {
        this.loading = false;
      });
    },
    getNodesArray(nodesStr) {
      let arr = [];
      if (nodesStr !== '') {
        if (nodesStr.indexOf(',') > 0) {
          arr = nodesStr.split(',');
        } else {
          arr = [nodesStr];
        }
      }
      return arr;
    },
    getLogicClusterNodesArray(nodesStr) {
      const resultArray = [];
      if (nodesStr.indexOf(';') > -1) {
        const clusterArr = nodesStr.split(';');
        if (clusterArr) {
          clusterArr.forEach((ele) => {
            if (ele) {
              const cluster = ele.split(':')[0];
              const nodeStr = ele.split(':')[1];
              if (nodeStr) {
                nodeStr.split(',').forEach((ele2) => {
                  const params = {
                    parent: cluster,
                    name: ele2,
                  };
                  resultArray.push(params);
                });
              }
            }
          });
        }
      }
      return resultArray;
    },
    handleDelete(row) {
      this.$message.confirmMessage(`确定删除版本${row.id}吗?`, () => {
        this.loading = true;
        this.$http.post('/index/version/delete/id.json', { indexId: this.indexId, versionId: row.id }).then(() => {
          this.$message.successMessage('删除成功', () => {
            this.getVersionList();
          });
        })
        .finally(() => {
          this.loading = false;
        });
      });
    },
    closeDialog() {
      this.isVersionInfoVisible = false;
    },
    closeDynamicDialog() {
      this.isVersionDynamicInfoVisible = false;
    },
    templateOperateSuccess() {
      this.isVersionInfoVisible = false;
      this.getVersionList();
    },
    dynamicUpdateOperateSuccess() {
      this.isVersionDynamicInfoVisible = false;
      this.getVersionList();
    },
    getSchemaMetaData() {
      this.loading = true;
      this.$http.post('/index/version/metadata.json', { indexId: this.indexId }).then((data) => {
        this.clusters = data.clusters;
        this.isLogical = data.isLogical;
        if (data.list === null || data.list.length === 0) {
          this.versionAddInfo.schema = [];
          this.isMetaDataNull = true;
        } else {
          this.isMetaDataNull = false;
          this.versionAddInfo.schema = data.list.map((obj) => {
            const rObj = {};
            rObj.dbFieldType = obj.dbFieldType;
            rObj.fieldName = obj.dbFieldName;
            switch (rObj.dbFieldType) {
              case 'TINYINT':
              case 'SMALLINT':
                rObj.fieldType = 'keyword_as_number';
                break;
              case 'INTEGER':
                rObj.fieldType = 'integer';
                break;
              case 'BIGINT':
                rObj.fieldType = 'long';
                break;
              case 'DATE':
              case 'TIMESTAMP':
                rObj.fieldType = 'date';
                break;
              case 'DOUBLE':
              case 'DECIMAL':
                rObj.fieldType = 'double';
                break;
              default:
                rObj.fieldType = 'keyword';
                break;
            }
            rObj.multi = false;
            switch (rObj.fieldName) {
              case 'id':
              case 'update_time':
                rObj.search = true;
                rObj.docValue = true;
                break;
              default:
                rObj.search = false;
                rObj.docValue = false;
                break;
            }
            rObj.children = [];
            rObj.multiField = [];
            rObj.copyTo = [];
            return rObj;
          });
        }
      })
      .finally(() => {
        this.loading = false;
      });
    },
    getVersionList() {
      const params = {
        currentPage: this.currentPage,
        pageSize: this.pageSize,
        indexId: this.indexId,
      };
      this.loading = true;
      this.$http.get('/index/version/page.json', params).then((data) => {
        this.versionData = data;
        this.isAllPrivilege = data.allPrivilege;
        this.versionList = data.list.map((obj) => {
          const rObj = { ...obj };
          return rObj;
        });
        const versionIdList = [];
        if (this.versionList.length > 0) {
          this.versionList.forEach((element) => {
            versionIdList.push(element.id);
          });
          this.$http.post('/index/version/count.json', { indexName: this.indexName, indexId: this.indexId, versionIds: versionIdList }).then((countList) => {
            this.versionList.forEach((ele) => {
              const result = countList.filter(e => e.vid === ele.id);
              const countResult = [];
              if (result[0].data.length > 0) {
                this.$array.strToArray(ele.realClusterIds).forEach((ele2) => {
                  const count = result[0].data.filter(v => v.cid === Number(ele2))
                  .map(m => m.count)[0];
                  countResult.push(count);
                });
              }
              this.$set(ele, 'count', countResult);
            });
          });
        }
      })
      .finally(() => {
        this.loading = false;
      });
    },
    changePage(currentPage) {
      this.currentPage = currentPage;
      this.getVersionList();
    },
    stopSync(row) {
      const isUsedStopMsg = this.$createElement('span', null, [this.$createElement('p', null, `确定删除该版本${row.id}的索引吗?`), this.$createElement('p', null, '注意此操作会同时停用该版本!')]);
      const msg = row.isUsed ? isUsedStopMsg : `确定删除该版本${row.id}的索引吗?`;
      this.$message.confirmMessage(msg, () => {
        this.loading = true;
        this.$http.post('/index/version/disable.json', { indexId: this.indexId, versionId: row.id }).then(() => {
          this.$message.successMessage('删除索引成功', () => {
            this.getVersionList();
          });
        })
        .finally(() => {
          this.loading = false;
        });
      });
    },
  },
  filters: {
    translateIsUsed(data) {
      const IS_ENABLED = { true: '是', false: '否' };
      return IS_ENABLED[data];
    },
  },
  created() {
    this.getVersionList();
    this.getSchemaMetaData();
  },
};

</script>
