<template>
    <div class="target-group-item">
        <div class="target-group-header">
            <div class="pull-left">
                <div>
                    <el-input size="mini" style="width: 100px;" v-model="name" v-if="editable"></el-input>
                    <span class="title" v-else>{{name}}</span>
                    <el-tag v-if="targetGroupItemInfo.clusterLevel === 1" type="warning">集群级别</el-tag>
                    <el-tag v-if="targetGroupItemInfo.clusterLevel === 2" type="warning">分片动态绑定</el-tag>
                </div>
            </div>
            <div class="pull-right" v-if="privilege">
                <el-button size="mini" type="success" @click="handleSave()" v-show="editable">保存</el-button>
                <el-button size="mini" @click="handleEdit()" v-show="!editable">编辑</el-button>
                <el-button size="mini" @click="handleCancel()" v-show="editable">取消</el-button>
                <el-button size="mini" type="danger" @click="handleDelete()">删除</el-button>
            </div>
        </div>
        <div class="target-group-content">
            <el-tree
            :data="myTree"
            show-checkbox
            node-key="name"
            ref="mytree"
            @check-change="checkChange"
            :default-expand-all='isDefaultExpand'
            :default-checked-keys="treeCheckedKeys"
            :render-content="renderContent"
            :props="defaultProps">
            </el-tree>
        </div>
    </div>
</template>

<script>
export default {
  props: ['privilege', 'targetGroupItemInfo', 'targetGroupTree', 'targetGroupTreeNoChildren', 'selectedShowTreeInfo'],
  data() {
    return {
      name: this.targetGroupItemInfo.name,
      isDefaultExpand: true,
      defaultProps: {
        children: 'children',
        label: 'name',
        disabled: 'disabled',
      },
      attrs: [{
        label: '普通级别',
        value: 0,
      }, {
        label: '集群级别',
        value: 1,
      }, {
        label: '分片动态绑定',
        value: 2,
      }],
      editable: false,
    };
  },
  methods: {
    checkChange(data, isChecked) {
      if (this.targetGroupItemInfo.clusterLevel !== 0) {
        if (isChecked) {
          const arr = [];
          arr.push(data.name);
          this.$refs.mytree.setCheckedKeys(arr);
        }
      }
    },
    handleSave() {
      this.targetGroupItemInfo.name = this.name;
      const targetGroupInfo = JSON.parse(JSON.stringify(this.targetGroupItemInfo));
      if (this.targetGroupItemInfo.clusterLevel === 0) {
        this.getTargetGroupNodes(targetGroupInfo.nodes);
        if (targetGroupInfo.nodes.length > 0) {
          const diff = targetGroupInfo.nodes.some((ele) => {
            if (ele.cluster !== targetGroupInfo.nodes[0].cluster) {
              return true;
            }
            return false;
          });
          if (diff) {
            this.$message.errorMessage('只能选择某个集群下的节点，请重新选择！');
          } else {
            this.saveInfo(targetGroupInfo);
          }
        }
      } else {
        this.getTargetGroupNodes(targetGroupInfo.clusters);
        this.saveInfo(targetGroupInfo);
      }
    },
    saveInfo(info) {
      this.$message.confirmMessage(`确定保存节点集${this.name}吗?`, () => {
        this.editable = false;
        this.$emit('save-operation', info);
      });
    },
    handleEdit() {
      this.editable = true;
      this.setTreeStatus(this.targetGroupTreeNoChildren, false);
      this.setTreeStatus(this.targetGroupTree, false);
    },
    handleCancel() {
      this.name = this.targetGroupItemInfo.name;
      if (this.targetGroupItemInfo.clusterLevel === 0) {
        this.$refs.mytree.setCheckedKeys(this.targetGroupItemInfo.nodes);
      } else {
        this.$refs.mytree.setCheckedKeys(this.targetGroupItemInfo.clusters);
      }
      this.editable = false;
      this.setTreeStatus(this.targetGroupTreeNoChildren, true);
      this.setTreeStatus(this.targetGroupTree, true);
    },
    setTreeStatus(arr, boolean) {
      arr.forEach((ele) => {
        this.$set(ele, 'disabled', boolean);
        if (ele.children) {
          ele.children.forEach((ele2) => {
            this.$set(ele2, 'disabled', boolean);
          });
        }
      });
    },
    getTargetGroupNodes(list) {
      list.splice(0, list.length);
      this.$refs.mytree.getCheckedNodes().forEach((element) => {
        if (!element.children) {
          list.push(element);
        }
      });
    },
    handleDelete() {
      this.$message.confirmMessage(`确定删除节点集${this.targetGroupItemInfo.name}吗?`, () => {
        this.$emit('delete-operation', this.targetGroupItemInfo.id);
      });
    },
    getNodesName(clusterName) {
      const arr = [];
      this.targetGroupTree.forEach((ele) => {
        if (ele.name === clusterName) {
          if (ele.children) {
            ele.children.forEach((ele2) => {
              arr.push(ele2.name);
            });
          }
        }
      });
      return arr;
    },
    getShardNodes(clusterName) {
      const arr = [];
      this.targetGroupTree.forEach((ele) => {
        if (ele.children) {
          ele.children.forEach((ele2) => {
            if (this.targetGroupItemInfo.nodes) {
              this.targetGroupItemInfo.nodes.forEach((ele3) => {
                if (ele2.cluster === clusterName && ele3 === ele2.address) {
                  arr.push(ele2.name);
                }
              });
            }
          });
        }
      });
      return arr;
    },
    renderContent(h, { data }) {
      if (this.targetGroupItemInfo.clusterLevel === 1) {
        return h(
          'span',
          [
            h('el-popover', { props: { placement: 'right', trigger: 'hover' } },
              [
                h('div', this.getNodesName(data.name).map(item => h('div', item))),
                h('span', { slot: 'reference', style: { 'font-size': '14px', 'margin-right': '5px' } }, this.selectedShowTreeInfo === 'name' ? data.name : data.address),
              ],
            ),
          ],
        );
      } else if (this.targetGroupItemInfo.clusterLevel === 0) {
        return h(
          'span',
          [
            h('span', { style: { 'font-size': '14px', 'margin-right': '5px' } }, this.selectedShowTreeInfo === 'name' ? data.name : data.address),
            h('span', { class: { 'el-tag': data.state === 1, 'el-tag--danger': data.state === 1 } }, data.state === 1 ? '离线' : ''),
          ],
        );
      }
      return h(
        'span',
        [
          h('el-popover', { props: { placement: 'right', trigger: 'hover', disabled: data.cluster !== this.targetGroupItemInfo.clusters[0] } },
            [
              h('div', this.getShardNodes(data.name).map(item => h('div', item))),
              h('span', { slot: 'reference', style: { 'font-size': '14px', 'margin-right': '5px' } }, this.selectedShowTreeInfo === 'name' ? data.name : data.address),
            ],
          ),
        ],
      );
    },
  },
  computed: {
    myTree() {
      if (this.targetGroupItemInfo.clusterLevel === 1 ||
      this.targetGroupItemInfo.clusterLevel === 2) {
        return this.targetGroupTreeNoChildren;
      }
      return this.targetGroupTree;
    },
    treeCheckedKeys() {
      if (this.targetGroupItemInfo.clusterLevel === 1 ||
      this.targetGroupItemInfo.clusterLevel === 2) {
        return this.targetGroupItemInfo.clusters;
      }
      return this.targetGroupItemInfo.nodes;
    },
  },
};
</script>

<style scoped>
.target-group-item {
    margin-bottom: 15px;
}
.target-group-item:hover{
    box-shadow: 0px 1px 20px #d1dbe5;
}
.target-group-item .target-group-header {
    padding: 10px;
    border: 1px solid gray;
    height: 25px;
    background-color: #373a3c;
}
.target-group-item .target-group-header .title{
    background-color: #13ce66;
    border-radius: 4px;
    padding: 5px 10px;
}
.target-group-item .target-group-content .el-tree {
    border: 1px solid gray;
}
</style>
