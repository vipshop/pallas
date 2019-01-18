<template>
    <div class="my-tab-content">
        <div class="content" v-show="isAllPrivilege">
            <el-button type="primary" icon="plus" @click="handleAdd">新增版本</el-button>
        </div>
        <div class="content">
            <template>
                <el-table :data="versionList" border style="width: 100%" v-loading="loading" element-loading-text="请稍等···">
                    <el-table-column label="版本id" prop="id" width="100px"></el-table-column>
                    <el-table-column label="所属集群" prop="clusterId" min-width="110">
                        <template scope="scope">
                            <router-link tag="a" :to="{ name:'cluster_detail',query:{clusterId: getClusterName(scope.row.clusterId)} }">{{getClusterName(scope.row.clusterId)}}</router-link>
                        </template>
                    </el-table-column>
                    <el-table-column label="数据量" prop="count" width="110px"></el-table-column>
                    <el-table-column label="配置信息" width="120px">
                        <template scope="scope">
                            <el-button size="small" @click="viewConfigInfo(scope.row)" :disabled="!isAllPrivilege">查看配置信息</el-button>
                        </template>
                    </el-table-column>
                    <el-table-column label="创建时间" prop="createTime" min-width="110">
                        <template scope="scope">{{scope.row.createTime | formatDate}}</template>
                    </el-table-column>
                    <el-table-column label="是否启用" prop="isUsed" min-width="50">
                        <template scope="scope"> 
                            <el-tag :type="scope.row.isUsed ? 'success' : 'danger'" close-transition>{{scope.row.isUsed || false | translateIsUsed}}</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="80" v-if="isAllPrivilege">
                        <template scope="scope">
                            <el-dropdown trigger="click">
                              <span class="el-dropdown-link">
                                操作<i class="el-icon-caret-bottom el-icon--right"></i>
                              </span>
                              <el-dropdown-menu class="dropdown-operation" slot="dropdown">
                                <el-dropdown-item v-show="!scope.row.isSync"><a @click="createIndex(scope.row)"><span><i class="fa fa-play-circle"></i>创建索引</span></a></el-dropdown-item>
                                <el-dropdown-item v-show="!scope.row.isUsed"><a @click="enableVersion(scope.row)"><span><i class="fa fa-hand-o-right"></i>启用版本</span></a></el-dropdown-item>
                                <el-dropdown-item><a @click="triggerDialog(scope.row, 'view')"><span><i class="fa fa-eye"></i>配置查看</span></a></el-dropdown-item>
                                <el-dropdown-item v-show="!scope.row.isSync"><a @click="triggerDialog(scope.row, 'edit')"><span><i class="fa fa-pencil-square-o"></i>配置更改</span></a></el-dropdown-item>
                                <el-dropdown-item><a @click="copyVersion(scope.row, 'copy')"><span><i class="fa fa-clone"></i>版本复制</span></a></el-dropdown-item>
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
            <version-info-dialog :version-operation="versionOperation" :version-info-title="versionInfoTitle" :version-info="versionInfo" :clusters="clusters" :is-meta-data-null="isMetaDataNull" @close-dialog="closeDialog" @template-operate-success="templateOperateSuccess"></version-info-dialog>
        </div>
        <div v-if="isViewConfigVisible">
            <json-content-dialog :content="configInfo" :title="configTitle" @close-dialog="closeViewConfigDialog"></json-content-dialog>
        </div>
    </div>
</template>

<script>
import VersionInfoDialog from './version_info_dialog/version_info_dialog';

export default {
  components: {
    'version-info-dialog': VersionInfoDialog,
  },
  data() {
    return {
      loading: false,
      indexId: this.$route.query.indexId,
      indexName: this.$route.query.indexName,
      isMetaDataNull: false,
      isAllPrivilege: false,
      isVersionInfoVisible: false,
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
        indexSlowThreshold: -1,
        fetchSlowThreshold: -1,
        querySlowThreshold: -1,
        refreshInterval: 60,
      },
      clusters: [],
    };
  },
  methods: {
    getClusterName(id) {
      let clusterName = '';
      this.clusters.some((ele) => {
        if (ele.id === id) {
          clusterName = ele.clusterId;
          return true;
        }
        return false;
      });
      return clusterName;
    },
    viewConfigInfo(row) {
      const params = {
        indexId: this.indexId,
        indexName: this.indexName,
        versionId: row.id,
      };
      this.loading = true;
      this.$http.get('/index/version/info.json', params).then((data) => {
        this.configInfo = data;
        this.configTitle = `${this.indexName}_${row.id}配置信息`;
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
          this.versionGetInfo.versionId = row.id;
        });
        this.$set(this.versionGetInfo, 'versionId', this.versionGetInfo.id);
        this.$set(this.versionGetInfo, 'nodes', this.getNodesArray(this.versionGetInfo.allocationNodes));
        this.versionInfo = JSON.parse(JSON.stringify(this.versionGetInfo));
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
          this.versionGetInfo.versionId = row.id;
        });
        this.$set(this.versionGetInfo, 'versionId', this.versionGetInfo.id);
        this.$set(this.versionGetInfo, 'nodes', this.getNodesArray(this.versionGetInfo.allocationNodes));
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
    templateOperateSuccess() {
      this.isVersionInfoVisible = false;
      this.getVersionList();
    },
    getSchemaMetaData() {
      this.loading = true;
      this.$http.post('/index/version/metadata.json', { indexId: this.indexId }).then((data) => {
        this.clusters = data.clusters;
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
                rObj.fieldType = 'short';
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
          const rObj = {};
          rObj.createTime = obj.createTime;
          rObj.id = obj.id;
          rObj.idField = obj.idField;
          rObj.clusterId = obj.clusterId;
          rObj.indexId = obj.indexId;
          rObj.isSync = obj.isSync;
          rObj.isUsed = obj.isUsed;
          rObj.numOfReplication = obj.numOfReplication;
          rObj.numOfShards = obj.numOfShards;
          rObj.routingKey = obj.routingKey;
          rObj.syncStat = obj.syncStat;
          rObj.updateTime = obj.updateTime;
          rObj.updateTimeField = obj.updateTimeField;
          rObj.vdpQueue = obj.vdpQueue;
          rObj.versionName = obj.versionName;
          rObj.indexSlowThreshold = obj.indexSlowThreshold;
          rObj.fetchSlowThreshold = obj.fetchSlowThreshold;
          rObj.querySlowThreshold = obj.querySlowThreshold;
          return rObj;
        });
        const versionIdList = [];
        if (this.versionList.length > 0) {
          this.versionList.forEach((element) => {
            versionIdList.push(element.id);
          });
          this.$http.post('/index/version/count.json', { indexName: this.indexName, versionIds: versionIdList }).then((countData) => {
            this.versionList.forEach((ele) => {
              this.$set(ele, 'count', countData[ele.id]);
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
