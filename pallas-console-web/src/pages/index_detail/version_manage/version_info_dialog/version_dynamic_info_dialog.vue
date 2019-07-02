  <template>
    <div class="version-info-dialog">
        <el-dialog size="large" v-model="isVersionInfoVisible" :before-close="closeDialog" v-loading="loading" element-loading-text="请稍等···">
            <span slot="title">
              <span>{{versionInfoTitle}}</span>
            </span>
            <el-form :model="versionInfo" :rules="rules" ref="versionInfo" label-position="left">
                <el-tabs value="first">
                    <el-tab-pane label="索引配置" name="first">
                        <div class="label-title">
                                    <span class="span-title"><i class="fa fa-th-large"></i>分片路由属性</span>
                          <span v-if="isLogical" style="color: #C8C8C8;">（所属集群：{{clusterArray.join()}}）</span>
                        </div>
                        <div class="label-content">
                            <el-row :gutter="20">
                                <el-col :span="12">
                                    <el-form-item label="复制数量" prop="replicationNum" label-width="120px">
                                        <el-input v-model.number="versionInfo.replicationNum"></el-input>
                                    </el-form-item>
                                </el-col>
                                <el-col :span="12">
                                    <el-form-item label="ShardPerNode" prop="totalShardsPerNode" label-width="120px">
                                        <el-input v-model.number="versionInfo.totalShardsPerNode"></el-input>
                                    </el-form-item>
                                </el-col>
                            </el-row>
                        </div>
                        <div class="label-content">
                            <el-row :gutter="20" v-if="isLogical">
                                <el-col :span="24">
                                    <el-form-item label="所属节点" prop="nodes" label-width="120px">
                                        <el-select multiple filterable value-key="name" v-model="versionInfo.nodes" placeholder="请选择机器"  style="width: 100%">
                                            <el-option-group v-for="group in clusterGroups" :key="group.clusterId" :label="group.clusterId">
                                                <el-option v-for="item in group.nodes" :key="item.name" :label="item.name" :value="item">
                                                    <span style="float: left">{{ item.name }}</span>
                                                    <el-tooltip placement="right">
                                                        <div slot="content">
                                                            <div v-if="item.indicis.length > 0" style="width: 600px;">
                                                                <el-col v-for="item1 in item.indicis" :key="item1" :span="8">{{item1}}</el-col>
                                                            </div>
                                                            <div v-else>暂无索引</div>
                                                        </div>
                                                        <el-tag style="float: left;margin-left: 10px;">{{ item.indicis.length }}</el-tag>
                                                    </el-tooltip>
                                                </el-option>
                                            </el-option-group>
                                        </el-select>
                                    </el-form-item>
                                </el-col>
                            </el-row>
                            <el-row :gutter="20" v-else>
                                <el-col :span="24">
                                    <el-form-item label="所属节点" prop="nodes" label-width="120px">
                                        <el-select multiple filterable v-model="versionInfo.nodes" placeholder="请选择机器"  style="width: 100%">
                                            <el-option v-for="item in clusterNodes" :key="item.name" :label="item.name" :value="item.name">
                                                <span style="float: left">{{ item.name }}</span>
                                                <el-tooltip placement="right">
                                                    <div slot="content">
                                                        <div v-if="item.indicis.length > 0" style="width: 600px;">
                                                            <el-col v-for="item1 in item.indicis" :key="item1" :span="8">{{item1}}</el-col>
                                                        </div>
                                                        <div v-else>暂无索引</div>
                                                    </div>
                                                    <el-tag style="float: left;margin-left: 10px;">{{ item.indicis.length }}</el-tag>
                                                </el-tooltip>
                                            </el-option>
                                        </el-select>
                                    </el-form-item>
                                </el-col>
                            </el-row>
                        </div>
                        <div class="label-title"><span class="span-title"><i class="fa fa-th-large"></i>慢日志落盘配置（单位ms，-1不限定）</span></div>
                        <div class="label-content">
                            <el-row :gutter="20">
                                <el-col :span="8">
                                    <el-form-item label="Index Slow" prop="indexSlowThreshold" label-width="120px">
                                        <el-input v-model.number="versionInfo.indexSlowThreshold" ></el-input>
                                    </el-form-item>
                                </el-col>
                                <el-col :span="8">
                                    <el-form-item label="Fetch Slow" prop="fetchSlowThreshold" label-width="120px">
                                        <el-input v-model.number="versionInfo.fetchSlowThreshold" ></el-input>
                                    </el-form-item>
                                </el-col>
                                <el-col :span="8">
                                    <el-form-item label="Query Slow" prop="querySlowThreshold" label-width="120px">
                                        <el-input v-model.number="versionInfo.querySlowThreshold" ></el-input>
                                    </el-form-item>
                                </el-col>
                            </el-row>
                        </div>
                        <div class="label-title"><span class="span-title"><i class="fa fa-th-large"></i>索引其他配置</span></div>
                        <div class="label-content">
                            <el-row :gutter="20">
                                <el-col :span="12">
                                    <el-form-item label="max_result_window" prop="maxResultWindow" label-width="180px">
                                        <el-input placeholder="10000" v-model.number="versionInfo.maxResultWindow" ></el-input>
                                    </el-form-item>
                                </el-col>
                                <el-col :span="12">
                                    <el-form-item label="flush_threshold_size" prop="flushThresholdSize" label-width="180px">
                                        <el-input placeholder="512mb" v-model="versionInfo.flushThresholdSize" ></el-input>
                                    </el-form-item>
                                </el-col>
                            </el-row>
                        </div>
                        <div class="label-content">
                            <el-row :gutter="20">
                                <el-col :span="12">
                                    <el-form-item label="sync_interval" prop="syncInterval" label-width="180px">
<!--                                        该字段无法动态更新，在这里纯展示，不做更新操作-->
                                        <el-input v-model="versionInfo.syncInterval" :disabled="true" ></el-input>
                                    </el-form-item>
                                </el-col>
                                <el-col :span="12">
                                    <el-form-item label="translog_durability" prop="translogDurability" label-width="180px">
                                        <el-input placeholder="async" v-model="versionInfo.translogDurability" ></el-input>
                                    </el-form-item>
                                </el-col>
                            </el-row>
                        </div>
                        <div class="label-content">
                            <el-row :gutter="20">
                                <el-col :span="8">
                                    <el-form-item label="refresh（秒）" prop="refreshInterval" label-width="120px">
                                        <el-input v-model.number="versionInfo.refreshInterval" ></el-input>
                                    </el-form-item>
                                </el-col>
                            </el-row>
                        </div>
                    </el-tab-pane>

                </el-tabs>


            </el-form>
            <div slot="footer" class="dialog-footer">                
                <el-button @click="closeDialog()">取 消</el-button>
                <el-button type="confirm" @click="submitVersionInfo()">确 定</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>

export default {
  props: ['versionOperation', 'versionInfo', 'versionInfoTitle', 'clusters', 'isLogical'],
  data() {
    return {
      loading: false,
      isVersionInfoVisible: true,
      rules: {
        replicationNum: [{ required: true, message: '复制数量不能为空' }, { type: 'number', message: '复制数量必须为数字值' }],
        indexSlowThreshold: [{ required: true, message: 'Index Slow Log不能为空' }, { type: 'number', message: 'Index Slow Log必须为数字值' }],
        fetchSlowThreshold: [{ required: true, message: 'Fetch Slow Log不能为空' }, { type: 'number', message: 'Fetch Slow Log必须为数字值' }],
        querySlowThreshold: [{ required: true, message: 'Query Slow Log不能为空' }, { type: 'number', message: 'Query Slow Log必须为数字值' }],
        refreshInterval: [{ required: true, message: 'refresh_interval不能为空' }, { type: 'number', message: 'refresh_interval必须为数字值' }],
        maxResultWindow: [{ required: true, message: 'max_result_window不能为空' }, { type: 'number', message: 'max_result_window必须为数字值' }],
        totalShardsPerNode: [{ required: true, message: 'total_shards_per_node不能为空' }, { type: 'number', message: 'total_shards_per_node必须为数字值' }],
        flushThresholdSize: [{ required: true, message: 'flush_threshold_size不能为空' }],
        translogDurability: [{ required: true, message: 'translog_durability不能为空' }],
      },
    };
  },
  methods: {
    getLogicClusterAllocationNodes() {
      const result = [];
      this.clusterArray.forEach((ele) => {
        const obj = {
          cluster: ele,
          nodes: this.versionInfo.nodes.filter(e => ele === e.parent).map(v => v.name),
        };
        result.push(obj);
      });
      let str = '';
      result.forEach((element) => {
        str += `${element.cluster}:${element.nodes.join()};`;
      });
      return str;
    },
    submitVersionInfo() {
      this.$refs.versionInfo.validate((valid) => {
        if (valid) {
          if (this.isLogical) {
            this.$set(this.versionInfo, 'allocationNodes', this.getLogicClusterAllocationNodes());
          } else {
            this.$set(this.versionInfo, 'allocationNodes', this.versionInfo.nodes.join(','));
          }
          this.loading = true;
          this.$http.post('/index/version/dynamic_update.json', this.versionInfo).then(() => {
            this.$message.successMessage('编辑版本成功', () => {
              this.$emit('template-operate-success');
            });
          })
          .finally(() => {
            this.loading = false;
          });
        }
      });
    },
    closeDialog() {
      this.$emit('close-dialog');
      this.$refs.versionInfo.resetFields();
    },
  },
  computed: {
    clusterArray() {
      const arr = this.clusters.map(e => e.clusterId);
      return arr;
    },
    clusterNodes() {
      let arr = [];
      this.clusters.forEach((ele) => {
        if (ele.id === this.versionInfo.clusterId) {
          arr = ele.nodes;
        }
      });
      return arr;
    },
    clusterGroups() {
      const result = this.clusters.map((obj) => {
        const rObj = { ...obj };
        rObj.nodes = obj.nodes.map((obj2) => {
          const rObj2 = { ...obj2 };
          rObj2.parent = obj.clusterId;
          return rObj2;
        });
        return rObj;
      });
      return result;
    },
  },
};

</script>

<style>
.label-title {
    height: 30px;
    line-height: 30px;
    margin-bottom: 5px;
    padding-left: 5px;
    background-color: #333;
}
.label-title .span-title{
    color: #eee;
    font-size: 15px;
    margin-right: 5px;
}
.label-content {
  margin: 10px 15px 20px;
}
.label-content span {
  color: #eee;
}
.version-info-dialog .el-dialog__body {
  padding: 10px 5px 10px 0px;
}
.version-info-dialog .el-dialog__footer {
  padding: 10px 20px 0 0;
}

.version-info-dialog form{
  margin-bottom:0px;
}

.version-info-dialog .el-form-item {
  margin-bottom: 0px;
}

.red {
  color: red;
}

.version-info-dialog .el-button--text {
    color: #eee;
}

.version-info-dialog .el-button--text:focus, .el-button--text:hover {
    color: gray;
}

.version-info-dialog .el-checkbox {
    color: #eee;
}
</style>
