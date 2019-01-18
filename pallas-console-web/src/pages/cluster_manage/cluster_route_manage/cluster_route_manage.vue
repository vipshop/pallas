<template>
    <div class="page-content">
        <div class="my-breadcrumb">
            <el-breadcrumb separator="/" class="my-breadcrumb-content">
                <el-breadcrumb-item :to="{ name:'cluster_manage' }"><i class="fa fa-home"></i>ES集群管理</el-breadcrumb-item>
                <el-breadcrumb-item>ES集群路由配置</el-breadcrumb-item>
                <el-breadcrumb-item>{{clusterId}}</el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div class="data-table-filter" v-if="privilege">
            <div class="pull-left">
                <el-button type="primary" icon="plus" @click="handleAdd">创建规则</el-button>
            </div>
        </div>
        <el-table :data="rulesList" stripe style="width: 100%" v-loading="loading" element-loading-text="请稍等···">
            <el-table-column label="规则名称" prop="name" header-align="center"></el-table-column>
            <el-table-column label="规则关系" prop="conditionRelation" header-align="center" width="80px"></el-table-column>
            <el-table-column label="判断表达式" header-align="center">
                <el-table-column label="判断参数名" min-width="150" header-align="center">
                    <template scope="scope"> 
                        <div class="condition-table" v-for="item in scope.row.conditions" :key="item.paramName">{{item.paramName}}</div>
                    </template>
                </el-table-column>
                <el-table-column label="判断运算" min-width="70" header-align="center">
                    <template scope="scope"> 
                        <div class="condition-table" v-for="item in scope.row.conditions" :key="item.exprOp">{{item.exprOp}}</div>
                    </template>
                </el-table-column>
                <el-table-column label="判断值" min-width="140" header-align="center">
                    <template scope="scope">
                        <div class="condition-table" v-for="item in scope.row.conditions" :key="item.paramValue">{{item.paramValue}}</div>
                    </template>
                </el-table-column>
            </el-table-column>
            <el-table-column label="节点集" min-width="150" header-align="center">
                <template scope="scope">
                    <div v-for="item in scope.row.targetGroups" :key="item.id" class="route-tag">
                        <el-tag class="target-group-item" type="warning">
                            <span>{{routingTargetGroupsList[item.id].name}}</span>
                            <span class="route-weight" v-if="scope.row.targetGroups.length > 1">{{item.weight}}</span>
                        </el-tag>
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="是否启用" header-align="center" width="80px">
                <template scope="scope">
                    <el-tag :type="scope.row.enable ? 'success' : 'danger'" close-transition>{{scope.row.enable | translateEnable}}</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="路由管理" width="80px" header-align="center" v-if="privilege">
                <template scope="scope">
                    <el-tooltip content="编辑" placement="top">
                        <el-button type="text" @click="handleEdit(scope.row)"><i class="fa fa-edit"></i></el-button>
                    </el-tooltip>
                    <el-tooltip content="删除" placement="top">
                        <el-button type="text" @click="handleDelete(scope.row, scope.$index)"><i class="fa fa-trash"></i></el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
        </el-table>
        <target-group :privilege="privilege" :cluster-id="clusterId" :rules-list="rulesList" :routing-target-groups-list="routingTargetGroupsList" @get-routing-list="getRoutingList"></target-group>
        <div v-if="isConditionInfoVisible">
            <condition-info-dialog :routing-target-groups="routingTargetGroups" :conditions="JSON.parse(JSON.stringify(conditions))" :condition-info="conditionInfo" :condition-info-title="conditionInfoTitle" :condition-operation="conditionOperation" @add-condition="addCondition" @edit-condition="editCondition" @close-dialog="closeConditionDialog"></condition-info-dialog>
        </div>
    </div>
</template>

<script>
import TargetGroup from './cluster_target_group/cluster_target_group';
import ConditionInfoDialog from './cluster_condition_info_dialog/cluster_condition_info_dialog';

export default {
  data() {
    return {
      loading: false,
      privilege: false,
      clusterId: this.$route.query.clusterId,
      isConditionInfoVisible: false,
      conditionInfo: {},
      conditionInfoTitle: '',
      conditionOperation: '',
      selectedCluster: '',
      indexNameForSearch: '',
      conditions: [],
      rulesList: [],
      routingTargetGroupsList: {},
      initTargetGroups: [],
      routingTargetGroups: [],
    };
  },
  methods: {
    sortTargetGroups(arr) {
      arr.sort((a, b) => {
        if (a.weight < b.weight) {
          return 1;
        }
        if (a.weight > b.weight) {
          return -1;
        }
        return 0;
      });
    },
    handleAdd() {
      this.isConditionInfoVisible = true;
      this.conditionInfoTitle = '创建规则';
      this.conditionOperation = 'add';
      this.routingTargetGroups = JSON.parse(JSON.stringify(this.initTargetGroups));
      const conditionAddInfo = {
        name: '',
        targetGroups: [],
        targetGroupsId: [],
        enable: false,
        conditions: [],
        conditionRelation: 'AND',
        priority: '',
      };
      this.conditionInfo = JSON.parse(JSON.stringify(conditionAddInfo));
    },
    addCondition(item) {
      const addRulesList = JSON.parse(JSON.stringify(this.rulesList));
      if (item.priority === '') {
        addRulesList.push(item);
      } else {
        addRulesList.some((element, index) => {
          if (element.name === item.priority) {
            addRulesList.splice(index, 0, item);
            return true;
          }
          return false;
        });
      }
      this.updateConditionRequest(addRulesList);
    },
    closeConditionDialog() {
      this.isConditionInfoVisible = false;
    },
    handleEdit(row) {
      this.isConditionInfoVisible = true;
      this.conditionInfoTitle = '编辑规则';
      this.conditionOperation = 'edit';
      this.routingTargetGroups = JSON.parse(JSON.stringify(this.initTargetGroups));
      const targetGroupsId = [];
      row.targetGroups.forEach((ele1) => {
        targetGroupsId.push(ele1.id);
        this.routingTargetGroups.forEach((ele2) => {
          if (ele1.id === ele2.id) {
            this.$set(ele2, 'weight', ele1.weight);
            this.$set(ele2, 'position', 'right');
          }
        });
      });
      const conditionEditInfo = {
        name: row.name,
        targetGroups: row.targetGroups,
        targetGroupsId,
        enable: row.enable,
        conditionRelation: row.conditionRelation,
        priority: '',
        conditions: row.conditions,
      };
      this.conditionInfo = JSON.parse(JSON.stringify(conditionEditInfo));
    },
    editCondition(item) {
      const editRulesList = JSON.parse(JSON.stringify(this.rulesList));
      let editConditionIndex;
      editRulesList.some((element, index) => {
        if (element.name === item.name) {
          editConditionIndex = index;
          return true;
        }
        return false;
      });
      if (item.priority === '') {
        editRulesList.splice(editConditionIndex, 1, item);
      } else {
        editRulesList.splice(editConditionIndex, 1);
        editRulesList.some((element, index) => {
          if (element.name === item.priority) {
            editRulesList.splice(index, 0, item);
            return true;
          }
          return false;
        });
      }
      this.updateConditionRequest(editRulesList);
    },
    updateConditionRequest(rulesList) {
      const params = {
        clusterId: this.clusterId,
        rules: rulesList,
      };
      this.loading = true;
      this.$http.post('/cluster/routing/rule/update.json', params).then(() => {
        this.$message.successMessage('操作成功', () => {
          this.isConditionInfoVisible = false;
          this.getRoutingList();
        });
      })
      .finally(() => {
        this.loading = false;
      });
    },
    handleDelete(row, index) {
      this.$message.confirmMessage(`确定删除规则${row.name}吗?`, () => {
        const deleteRulesList = JSON.parse(JSON.stringify(this.rulesList));
        deleteRulesList.splice(index, 1);
        this.updateConditionRequest(deleteRulesList);
      });
    },
    getRoutingList() {
      const params = {
        clusterId: this.clusterId,
      };
      this.loading = true;
      this.$http.get('/cluster/routing/rule/list.json', params).then((data) => {
        this.rulesList = data.data.rules;
        this.rulesList.forEach((ele) => {
          this.sortTargetGroups(ele.targetGroups);
        });
        const conditionList = [];
        data.data.rules.forEach((element) => {
          conditionList.push(element.name);
        });
        this.conditions = conditionList;
        this.routingTargetGroupsList = data.data.routingTargetGroups;
        this.initTargetGroups = Object.values(data.data.routingTargetGroups).map((obj) => {
          const rObj = {};
          rObj.id = obj.id;
          rObj.name = obj.name;
          rObj.weight = 1;
          rObj.position = 'left';
          return rObj;
        });
        this.privilege = data.privilege;
      })
      .finally(() => {
        this.loading = false;
      });
    },
  },
  created() {
    this.getRoutingList();
  },
  filters: {
    translateEnable(data) {
      const IS_ENABLED = { true: '是', false: '否' };
      return IS_ENABLED[data];
    },
  },
  components: {
    'target-group': TargetGroup,
    'condition-info-dialog': ConditionInfoDialog,
  },
};
</script>

<style>
.target-group-item {
  margin-right: 5px;
}
.condition-table+.condition-table {
  border-top: 1px solid gray;
}
.condition-table span {
  white-space: nowrap;
}
.route-tag {
  margin-bottom: 5px;
}
.route-weight {
  color: #7fffd4;
  font-size: 12px;
  margin-left: 3px;
}
</style>
