<template>
    <div>
        <div class="token-index">
            <div v-if="Object.keys(tokenClusterInfo).length !== 0">
                <div>
                    <el-button size="small" type="success" @click="handleUpdate">更新授权</el-button>
                    <el-button size="small" type="danger" @click="handleReset">移除授权</el-button>
                </div>
                <div class="mrg-top-10">
                    <span class="title-span">集群权限:</span> 
                    <el-radio-group size="small" v-model="tokenClusterInfo.clusterPrivilege">
                        <el-radio-button label="">无</el-radio-button>
                        <el-radio-button label="ReadOnly">只读</el-radio-button>
                        <el-radio-button label="Write">修改</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div v-else style="height: 62px;"></div>
            <div class="mrg-top-10">
                <el-table :data="tokenClusterInfo.myIndexPrivilegeArr" border style="width: 100%" :height="clusterTreeHeight.height">
                    <el-table-column prop="name" label="索引"></el-table-column>
                    <el-table-column prop="indexPrivilege" label="权限">
                        <template scope="scope">
                            <el-radio-group v-model="scope.row.indexPrivilege" size="small">
                                <el-radio-button label="">无</el-radio-button>
                                <el-radio-button label="ReadOnly">只读</el-radio-button>
                                <el-radio-button label="Write">修改</el-radio-button>
                            </el-radio-group>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
        </div>
    </div>
</template>
<script>
export default {
  props: ['tokenClusterInfo', 'clusterTreeHeight'],
  data() {
    return {
    };
  },
  methods: {
    handleUpdate() {
      this.$message.confirmMessage('确定更新授权吗?', () => {
        this.$emit('update-token-cluster');
      });
    },
    handleReset() {
      if (this.tokenClusterInfo.clusterPrivilege !== '') {
        this.tokenClusterInfo.clusterPrivilege = '';
      }
      if (this.tokenClusterInfo.myIndexPrivilegeArr.length > 0) {
        this.tokenClusterInfo.myIndexPrivilegeArr.forEach((ele, index) => {
          if (ele.indexPrivilege !== '') {
            this.tokenClusterInfo.myIndexPrivilegeArr[index].indexPrivilege = '';
          }
        });
      }
    },
  },
};
</script>
<style>
.token-index {
    margin-top: 3px;
}
.token-index .el-radio-button--small .el-radio-button__inner {
    padding: 3px 5px;
}
.token-index .el-table td, .el-table th {
    height: 36px;
}
.token-index .el-table th>.cell {
    line-height: 36px;
}
.token-index .el-table .cell, .el-table th>div {
    padding-top: 0;
    padding-bottom: 0;
}
</style>
