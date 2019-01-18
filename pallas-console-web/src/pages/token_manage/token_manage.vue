<template>
    <div class="page-content">
        <div class="my-breadcrumb">
            <el-breadcrumb separator="/" class="my-breadcrumb-content">
                <el-breadcrumb-item><i class="fa fa-home"></i>Token管理</el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <el-row :gutter="20" v-loading="loading" element-loading-text="请稍等···">
            <el-col :span="6">
                <div v-if="isPrivilege">
                    <el-button type="primary" icon="plus" @click="handleAdd">新建Token</el-button>
                </div>
                <div class="mrg-top-10">
                    <el-input placeholder="输入关键字进行过滤" icon="search" v-model="tokenForSearch" :on-icon-click="handleSearch"></el-input>
                </div>
                <div class="mrg-top-10 token-table">
                    <el-table :data="tokenList" :show-header="false" highlight-current-row border @row-click="handleTokenClick" :height="tokenHeight.height">
                        <el-table-column label="ID" prop="id" width="50px"></el-table-column>
                        <el-table-column prop="title" label="名称" show-overflow-tooltip>
                            <template scope="scope"> 
                                <div style="font-size: 16px;">{{scope.row.title}}
                                    <el-tag :type="scope.row.enabled ? 'success' : 'danger'">{{statusMap[scope.row.enabled]}}</el-tag>
                                </div>
                                <div style="color: gray;font-size: 12px;">{{scope.row.clientToken}}</div>
                            </template>
                        </el-table-column>
                        <el-table-column label="编辑" width="30px" align="center">
                            <template scope="scope"> 
                                <el-button type="text" @click.stop="handleEdit(scope.row)"><i class="fa fa-pencil-square-o"></i></el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
            </el-col>
            <el-col :span="18">
                <div class="token-warning" :style="tokenErrorHeight" v-if="!isSelectToken"><i class="el-icon-warning"></i>请选择Token</div>
                <div v-for="token in tokenList" :key="token.clientToken" class="token-body">
                    <token-detail v-if="tokenSelectInfo.clientToken === token.clientToken" :token-height="tokenHeight" :token-info="tokenSelectInfo"></token-detail>
                </div>
            </el-col>
        </el-row>
        <div v-if="isTokenInfoVisible">
            <token-info-dialog :token-info="tokenInfo" :token-info-operate="tokenInfoOperate" :token-info-title="tokenInfoTitle" @token-info-success="tokenInfoSuccess" @close-dialog="closeTokenInfoDialog"></token-info-dialog>
        </div>
    </div>
</template>

<script>
import TokenDetail from './token_detail';
import TokenInfoDialog from './token_info_dialog';

export default {
  data() {
    return {
      loading: false,
      isPrivilege: false,
      isTokenInfoVisible: false,
      isSelectToken: false,
      tokenInfo: {},
      tokenInfoTitle: '',
      tokenInfoOperate: '',
      tokenForSearch: '',
      tokenSelectInfo: {},
      tokenList: [],
      statusMap: {
        true: '启用',
        false: '禁用',
      },
      initTokenList: [],
      tokenHeight: {
        height: document.body.clientHeight - 225,
      },
    };
  },
  computed: {
    tokenErrorHeight() {
      return { height: this.tokenHeight.height + 92, 'line-height': `${this.tokenHeight.height + 92}px` };
    },
  },
  mounted() {
    this.tokenHeight = { height: document.body.clientHeight - 225 };
    const that = this;
    window.onresize = function temp() {
      that.tokenHeight = { height: document.body.clientHeight - 225 };
    };
  },
  methods: {
    handleSearch() {
      let filtered = this.initTokenList;
      filtered = filtered.filter((e) => {
        if (typeof e.clientToken === 'string') {
          const isRight =
          e.clientToken.toLowerCase().indexOf(this.tokenForSearch.toLowerCase()) > -1 ||
          e.title.toLowerCase().indexOf(this.tokenForSearch.toLowerCase()) > -1 ||
          e.id.toString().indexOf(this.tokenForSearch.toLowerCase()) > -1;
          return isRight;
        }
        return e.clientToken === this.tokenForSearch;
      });
      this.tokenList = filtered;
    },
    handleAdd() {
      const tokenAddInfo = {
        title: '',
        clientToken: '',
        enabled: true,
      };
      this.tokenInfo = JSON.parse(JSON.stringify(tokenAddInfo));
      this.tokenInfoTitle = '新建Token';
      this.tokenInfoOperate = 'add';
      this.isTokenInfoVisible = true;
    },
    closeTokenInfoDialog() {
      this.isTokenInfoVisible = false;
    },
    tokenInfoSuccess(resp) {
      if (resp.operation === 'edit') {
        this.tokenSelectInfo.clientToken = resp.clientToken;
        this.tokenSelectInfo.title = resp.title;
      }
      this.isTokenInfoVisible = false;
      this.init();
    },
    handleEdit(row) {
      const tokenEditInfo = {
        id: row.id,
        title: row.title,
        clientToken: row.clientToken,
        enabled: row.enabled,
      };
      this.tokenInfo = JSON.parse(JSON.stringify(tokenEditInfo));
      this.tokenInfoTitle = '编辑Token';
      this.tokenInfoOperate = 'edit';
      this.isTokenInfoVisible = true;
    },
    handleTokenClick(row) {
      this.isSelectToken = true;
      this.tokenSelectInfo = JSON.parse(JSON.stringify(row));
    },
    getDataList() {
      return this.$http.get('/token/list.json').then((data) => {
        this.tokenList = data;
        this.initTokenList = data;
        this.isPrivilege = true;
      });
    },
    init() {
      this.loading = true;
      Promise.all([this.getDataList()]).then()
      .finally(() => {
        this.loading = false;
      });
    },
  },
  created() {
    this.init();
  },
  components: {
    'token-detail': TokenDetail,
    'token-info-dialog': TokenInfoDialog,
  },
};

</script>
<style>
.token-table .el-table--enable-row-transition .el-table__body td {
   text-align: left;
}
.token-warning {
  text-align: center;
  color: red;
  font-size: larger;
  font-weight: bolder;
  vertical-align: middle;
}
.token-warning i {
  padding-right: 10px;
}
.token-body {
}
</style>
