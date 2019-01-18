<template>
    <div class="target-group" v-loading="loading" element-loading-text="请稍等···">
        <div class="data-table-filter">
            <el-button type="primary" icon="plus" @click="handleAdd" v-if="privilege">添加节点集</el-button>
            <el-select v-if="JSON.stringify(routingTargetGroupsList) != '{}'" v-model="selectedShowTreeInfo" @change="changeShowTreeInfo">
                <el-option label="展示节点名称" value="name"></el-option>
                <el-option label="展示节点地址" value="address"></el-option>
            </el-select>
        </div>
        <div class="target-group-null" v-if="JSON.stringify(routingTargetGroupsList) === '{}'">
            <span>请添加节点集</span>
        </div>
        <div v-else>
            <el-row :gutter="20">
                <el-col :span="12" v-for="(item, index) in routingTargetGroupsList" :key="item.id">
                    <target-group-item :privilege="privilege" :target-group-tree-no-children="JSON.parse(JSON.stringify(targetGroupTreeNoChildren))" :target-group-tree="JSON.parse(JSON.stringify(targetGroupTree))"
                    :targetGroupItemInfo="item" :selected-show-tree-info="selectedShowTreeInfo" @save-operation="saveOperation" @delete-operation="deleteOperation"></target-group-item>
                </el-col>
            </el-row>
        </div>
        <div v-if="istargetGroupInfoVisible">
            <target-group-info-dialog :target-group-info="targetGroupInfo" :target-group-info-title="targetGroupInfoTitle" @operation-success="operationSuccess" @close-dialog="closeTargetGroupDialog"></target-group-info-dialog>
        </div>
    </div>
</template>

<script>
import TargetGroupItem from './cluster_target_group_item';
import TargetGroupInfoDialog from './cluster_target_group_info_dialog';

export default {
  props: ['privilege', 'rulesList', 'clusterId', 'routingTargetGroupsList'],
  data() {
    return {
      loading: false,
      istargetGroupInfoVisible: false,
      targetGroupInfo: {},
      targetGroupInfoTitle: '',
      targetGroupTree: [],
      targetGroupTreeNoChildren: [],
      selectedShowTreeInfo: 'name',
    };
  },
  methods: {
    changeShowTreeInfo(val) {
      this.selectedShowTreeInfo = val;
    },
    handleAdd() {
      this.istargetGroupInfoVisible = true;
      this.targetGroupInfoTitle = '创建节点集';
      const targetGroupAddInfo = {
        clusterId: this.clusterId,
        name: '',
        clusterLevel: 0,
        nodes: [],
        clusters: [],
      };
      this.targetGroupInfo = JSON.parse(JSON.stringify(targetGroupAddInfo));
    },
    saveOperation(info) {
      this.$set(info, 'clusterId', this.clusterId);
      this.loading = true;
      this.$http.post('/cluster/routing/target_group/update.json', info).then(() => {
        this.$emit('get-routing-list');
      })
      .finally(() => {
        this.loading = false;
      });
    },
    deleteOperation(id) {
      let flag = false;
      this.rulesList.forEach((ele) => {
        ele.targetGroups.some((ele2) => {
          if (ele2.id === id) {
            flag = true;
          }
          return flag;
        });
      });
      if (!flag) {
        this.loading = true;
        this.$http.post('/index/routing/target_group/delete.json', { groupId: id }).then(() => {
          this.$emit('get-routing-list');
        })
        .finally(() => {
          this.loading = false;
        });
      } else {
        this.$message.errorMessage('此节点集正在使用中，不能删除！');
      }
    },
    operationSuccess() {
      this.istargetGroupInfoVisible = false;
      this.$emit('get-routing-list');
    },
    closeTargetGroupDialog() {
      this.istargetGroupInfoVisible = false;
    },
    getTargetGroupTree() {
      const params = {
        clusterId: this.clusterId,
      };
      this.loading = true;
      this.$http.get('/cluster/routing/target_group/list.json', params).then((data) => {
        this.targetGroupTree = data.tree.map((obj) => {
          const rObj = {};
          rObj.cluster = obj.cluster;
          rObj.address = obj.address;
          rObj.name = obj.name;
          rObj.disabled = true;
          if (obj.children.length > 0) {
            rObj.children = this.setTargetGroupChildrenDisabled(obj.children);
          }
          return rObj;
        });
        this.targetGroupTreeNoChildren = data.tree.map((obj) => {
          const rObj = {};
          rObj.cluster = obj.cluster;
          rObj.address = obj.address;
          rObj.name = obj.name;
          rObj.disabled = true;
          return rObj;
        });
      })
      .finally(() => {
        this.loading = false;
      });
    },
    setTargetGroupChildrenDisabled(arr) {
      const disabledTree = arr.map((obj) => {
        const rObj = {};
        rObj.cluster = obj.cluster;
        rObj.address = obj.address;
        rObj.name = obj.name;
        rObj.state = obj.state;
        rObj.weight = obj.weight;
        rObj.disabled = true;
        return rObj;
      });
      return disabledTree;
    },
  },
  components: {
    'target-group-info-dialog': TargetGroupInfoDialog,
    'target-group-item': TargetGroupItem,
  },
  created() {
    this.getTargetGroupTree();
  },
};
</script>

<style scoped>
.target-group {
    margin-top: 10px;
}
.target-group-null {
    text-align: center;
    border: 1px solid gray;
    padding: 10px;
    font-size: 14px;
    color: #5e7382;
    background-color: #373a3c;
}
</style>
