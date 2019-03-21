<template>
    <div class="token-detail" v-loading="loading" element-loading-text="请稍等···">
        <el-row :gutter="20" class="mrg-top-15">
            <el-col :span="10">
                <div >
                  <div class="token-title">
                        当前选择Token : <span class="token-name-span">{{tokenInfo.title}}</span></br>
                  </div>
                </div>
                <div class="token-cluster">
                    <div class="mrg-top-15">
                        <span class="title-span">集群列表<small style="color:gray">(一个token只能绑定一个集群)</small></span>
                        <el-tree class="token-cluster-tree" :style="clusterTokenHeight" :data="clusterList" :render-content="renderCluster" highlight-current node-key="id" @node-click="handleNodeClick"></el-tree>
                    </div>
                </div>
                
                <div class="token-pool">
                    <div class="mrg-top-15">
                        <span class="title-span">代理集群节点集<small style="color:gray">((默认为整个集群))</small></span>
                        <el-tree class="token-cluster-tree" :style="clusterPoolHeight" :data="psClusterList" :render-content="renderPsCluster" ref="psPoolTree" show-checkbox
                          highlight-current default-expand-all check-strictly @check-change="handlePsNodeClick" :default-checked-keys="tokenClusterInfo.checkedPools" node-key="id"></el-tree>
                    </div>
                </div>
            </el-col>
            <el-col :span="14">
                <token-index :token-cluster-info="tokenClusterInfo" :cluster-tree-height="clusterIndexHeight" @update-token-cluster="updateTokenCluster"></token-index>
            </el-col>
        </el-row>
    </div>
</template>
<script>
import TokenIndex from './token_index';

export default {
  props: ['tokenInfo', 'tokenHeight'],
  data() {
    return {
      loading: false,
      clusterList: [],
      psClusterList: [],
      clusterList2update: [],
      tokenClusterInfo: {},
    };
  },
  computed: {
    clusterPoolHeight() {
      return { height: (this.tokenHeight.height * (3 / 5)) - 48 };
    },
    clusterTokenHeight() {
      return { height: this.tokenHeight.height * (2 / 5) };
    },
    clusterIndexHeight() {
      return { height: this.tokenHeight.height };
    },
  },
  methods: {
    updateTokenCluster() {
      const clusterParams = {
        id: this.tokenClusterInfo.id,
        name: this.tokenClusterInfo.name,
      };
      if (this.tokenClusterInfo.clusterPrivilege === '') {
        this.$set(clusterParams, 'privileges', {});
      } else {
        this.$set(clusterParams, 'privileges', { ClusterAll: [this.tokenClusterInfo.clusterPrivilege] });
      }
      if (this.tokenClusterInfo.myIndexPrivilegeArr.length === 0) {
        this.$set(clusterParams, 'indexPrivileges', []);
      } else {
        this.tokenClusterInfo.myIndexPrivilegeArr.forEach((ele) => {
          if (ele.indexPrivilege === '') {
            this.$set(ele, 'privileges', {});
          } else {
            this.$set(ele, 'privileges', { IndexAll: [ele.indexPrivilege] });
          }
        });
        this.$set(clusterParams, 'indexPrivileges', this.tokenClusterInfo.myIndexPrivilegeArr);
      }
      if (this.tokenClusterInfo.pools.length === 0) {
        this.$set(clusterParams, 'pools', []);
      } else {
        this.tokenClusterInfo.pools.forEach((ele) => {
          Object.assign(ele, { aliasName: '' });
        });
        this.$set(clusterParams, 'pools', this.tokenClusterInfo.pools);
      }
      this.clusterList.some((ele, index) => {
        if (ele.id === clusterParams.id) {
          this.clusterList[index] = clusterParams;
        }
        return false;
      });
      this.clusterList2update[0] = clusterParams;
      this.updateTokenRequest();
    },
    updateTokenRequest() {
      const params = {
        id: this.tokenInfo.id,
        authorizationItems: this.clusterList2update,
      };
      this.loading = true;
      this.$http.post('/token/token_privilege/update.json', params).then(() => {
        this.getTokenDetail();
      })
      .finally(() => {
        this.loading = false;
      });
    },
    handleNodeClick(data) {
      const tokenClusterData = {
        id: data.id,
        name: data.name,
      };
      if (Object.keys(data.privileges).length === 0) {
        this.$set(tokenClusterData, 'clusterPrivilege', '');
      } else {
        this.$set(tokenClusterData, 'clusterPrivilege', data.privileges.ClusterAll[0]);
      }
      if (data.indexPrivileges.length > 0) {
        data.indexPrivileges.forEach((ele) => {
          if (Object.keys(ele.privileges).length === 0) {
            this.$set(ele, 'indexPrivilege', '');
          } else {
            this.$set(ele, 'indexPrivilege', ele.privileges.IndexAll[0]);
          }
        });
      }
      if (Object.keys(data.pools).length === 0) {
        this.$set(tokenClusterData, 'pools', []);
      } else {
        this.$set(tokenClusterData, 'pools', data.pools);
      }
      this.$set(tokenClusterData, 'serverPools', data.serverPools);
      this.$set(tokenClusterData, 'myIndexPrivilegeArr', data.indexPrivileges);
      this.psClusterList = this.getPsServerPoolJsonTree(data.serverPools);
      const checkedPools = [];
      tokenClusterData.pools.forEach((ele) => {
        checkedPools.push(this.genPsNodeKey(ele));
      });
      this.$set(tokenClusterData, 'checkedPools', checkedPools);
      this.tokenClusterInfo = JSON.parse(JSON.stringify(tokenClusterData));
    },
    getTokenDetail() {
      if (this.tokenInfo.id) {
        this.loading = true;
        this.$http.get(`/token/token_privileges.json?id=${this.tokenInfo.id}`).then((data) => {
          this.clusterList = data;
          this.sortTokenCluster();
        })
        .finally(() => {
          this.loading = false;
        });
      }
    },
    sortTokenCluster() {
      this.clusterList.sort((a, b) => {
        const subA = this.isIndexPrivilege(a) ? 1 : 0;
        const subB = this.isIndexPrivilege(b) ? 1 : 0;
        if (Object.keys(a.privileges).length + subA > Object.keys(b.privileges).length + subB) {
          return -1;
        }
        if (Object.keys(a.privileges).length + subA < Object.keys(b.privileges).length + subB) {
          return 1;
        }
        return 0;
      });
    },
    isIndexPrivilege(item) {
      let flag = false;
      flag = item.indexPrivileges.some((ele) => {
        if (Object.keys(ele.privileges).length !== 0) {
          return true;
        }
        return false;
      });
      return flag;
    },
    renderCluster(h, { data }) {
      let statusMap = '';
      if (Object.keys(data.privileges).length !== 0 || this.isIndexPrivilege(data)) {
        statusMap = '已授权';
      }
      const renderHtml = h(
        'span',
        [
          h('span', { style: { float: 'left', 'margin-left': '10px', 'font-size': '14px' } }, data.name),
          h('span', { style: { float: 'right', color: '#32cd32', 'margin-right': '10px', 'font-size': '12px' } }, statusMap),
        ],
      );
      return renderHtml;
    },
    renderPsCluster(h, { data }) {
      let renderHtml = '';
      if (data.aliasName) {
        renderHtml = h(
          'span',
          [
            h('span', { style: { 'margin-left': '10px', 'font-size': '14px' } }, data.name),
            h('span', { style: { 'margin-left': '5px', color: 'gray', 'font-size': '14px' } }, `( ${data.aliasName} )`),
          ],
        );
      } else {
        renderHtml = h(
          'span',
          [
            h('span', { style: { 'margin-left': '10px', 'font-size': '14px' } }, data.name),
          ],
        );
      }
      return renderHtml;
    },
    handlePsNodeClick() {
      if (Object.keys(this.tokenClusterInfo).length === 0) {
        this.$set(this.tokenClusterInfo, 'pools', []);
      } else {
        this.$set(this.tokenClusterInfo, 'pools', this.$refs.psPoolTree.getCheckedNodes());
      }
    },
    getPsServerPoolJsonTree(data) {
      const groups = {};
      data.forEach((ele) => {
        const group = ele.psClusterName;
        const newEle = Object.assign({}, ele, { id: this.genPsNodeKey(ele) });
        groups[group] = groups[group] || [];
        groups[group].push(newEle);
      });
      const tree = [];
      Object.keys(groups).forEach((key) => {
        const node = {
          name: key,
          children: groups[key],
          disabled: true,
        };
        tree.push(node);
      });
      return tree;
    },
    genPsNodeKey(ele) {
      return `${ele.name}:${ele.psClusterName}`;
    },
  },
  created() {
    this.getTokenDetail();
  },
  components: {
    'token-index': TokenIndex,
  },
};
</script>
<style>
.token-name-span{
  color: #32cd32;
  font-weight: bold;
  margin-right: 10px;
}
.token-cluster {
    margin-top: 5px;
    overflow: auto;
}
.token-pool {
    margin-top: 5px;
}
.token-index {
    margin-top: 5px;
}
.title-span {
    font-size: 14px;
    margin-right: 10px;
}
.token-cluster-tree {
  margin-top: 10px;
  position: relative;
  overflow: auto;
}
</style>
