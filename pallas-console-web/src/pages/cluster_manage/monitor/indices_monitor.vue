<template>
    <div class="my-tab-content">
        <div class="content">
            <div class="data-table-filter">
                <div class="pull-left">
                    <el-form :inline="true" class="demo-form-inline">
                        <input type="text" v-show="false"/>
                        <el-form-item label="">
                            <el-input placeholder="请搜索索引" v-model="indiceForSearch" @keyup.enter.native="getIndicesList"></el-input>
                        </el-form-item>
                        <el-form-item class="filter-search">
                            <el-button type="primary" icon="search" @click="getIndicesList">查询</el-button>
                        </el-form-item>
                    </el-form>
                </div>
            </div>
        </div>
        <div class="content">
            <template>
                <el-table :data="indicesList" border style="width: 100%" v-loading="loading" element-loading-text="请稍等···">
                    <el-table-column label="Name" prop="name">
                        <template scope="scope">
                            <router-link tag="a" :to="{ path: 'indice_monitor_detail', query: {clusterId, indice: scope.row.name} }">{{scope.row.name}}</router-link>
                        </template>
                    </el-table-column>
                    <el-table-column label="Status" prop="status"></el-table-column>
                    <el-table-column label="Document Count" prop="document"></el-table-column>
                    <el-table-column label="Data" prop="data"></el-table-column>
                    <el-table-column label="Index Rate" prop="indexRate"></el-table-column>
                    <el-table-column label="Search Rate" prop="searchRate"></el-table-column>
                    <el-table-column label="Unassigned Shards" prop="unassignedShards"></el-table-column>
                </el-table>
            </template>
            <div class="my-pagination" v-if="total != 0">
                <el-pagination layout="prev, pager, next" :total="total" :page-size="pageSize" :current-page="currentPage" @current-change="changePage"></el-pagination>
            </div>
        </div>
    </div>
</template>
<script>
export default {
  data() {
    return {
      loading: false,
      indicesList: [],
      pageSize: 10,
      currentPage: 1,
      total: 2,
      indiceForSearch: '',
    };
  },
  methods: {
    changePage(currentPage) {
      this.currentPage = currentPage;
      this.getIndicesList();
    },
    getIndicesList() {
      const list = [{
        name: 'index1',
        status: 'Green',
        document: '592',
        data: '2.6MB',
        indexRate: '0/s',
        searchRate: '0/s',
        unassignedShards: '0',
      }, {
        name: 'index2',
        status: 'Green',
        document: '592',
        data: '2.6MB',
        indexRate: '0/s',
        searchRate: '0/s',
        unassignedShards: '0',
      }];
      this.indicesList = list;
    },
  },
  computed: {
    clusterId() {
      return this.$route.query.clusterId;
    },
  },
  created() {
    this.getIndicesList();
  },
  watch: {
  },
};
</script>
