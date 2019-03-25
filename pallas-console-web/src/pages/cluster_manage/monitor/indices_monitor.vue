<template>
    <div class="my-tab-content">
        <div class="content">
            <div class="data-table-filter">
                <div class="pull-left">
                    <el-form :inline="true" class="demo-form-inline">
                        <input type="text" v-show="false"/>
                        <el-form-item label="">
                            <el-input placeholder="请搜索索引" v-model="indiceForSearch" @keyup.enter.native=""></el-input>
                        </el-form-item>
                        <el-form-item class="filter-search">
                            <el-button type="primary" icon="search" @click="">查询</el-button>
                        </el-form-item>
                    </el-form>
                </div>
            </div>
        </div>
        <div class="content">
            <template>
                <el-table :data="indices" border style="width: 100%" v-loading="loading" element-loading-text="请稍等···">
                    <el-table-column label="Name" prop="indexName">
                        <template scope="scope">
                            <router-link tag="a" :to="{ path: 'indice_monitor_detail', query: {clusterId, indice: scope.row.indexName} }">{{scope.row.indexName}}</router-link>
                        </template>
                    </el-table-column>
                    <el-table-column label="Status" prop="status" width="70px"></el-table-column>
                    <el-table-column label="Document Count" prop="documentCount"></el-table-column>
                    <el-table-column label="Document Store Primary" prop="document_store_byte_primary"></el-table-column>
                    <el-table-column label="Document Store Total" prop="document_store_byte_total"></el-table-column>
                    <el-table-column label="Health" prop="health" width="70px"></el-table-column>
                    <el-table-column label="Total Shard Count" prop="totalShardCount"></el-table-column>
                </el-table>
            </template>
        </div>
    </div>
</template>
<script>
export default {
  props: ['indices'],
  data() {
    return {
      loading: false,
      indiceForSearch: '',
    };
  },
  methods: {
  },
  computed: {
    timeInterval() {
      return this.$store.state.monitorTimeInterval;
    },
    clusterId() {
      return this.$route.query.clusterId;
    },
  },
  created() {
  },
  watch: {
  },
};
</script>
