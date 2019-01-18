<template>
    <div class="data-source-content">
        <div class="data-source-add">
            <el-button size="mini" icon="plus" @click="addDataSource">新增数据源</el-button>
            <el-button size="mini" type="warning" @click="importDs"><i class="fa fa-arrow-circle-o-down"></i>导入Mysql数据源</el-button>
            <el-button size="mini" v-show="dataSources.length > 0 && dataSources[0].ip != '' && indexId " type="warning" @click="exportDs"><i class="fa fa-arrow-circle-o-up"></i>导出Mysql数据源</el-button>
        </div>
        <el-collapse v-model="index" v-for="(item, index) in dataSources" :key="index">
            <el-collapse-item :name="index">
                <template slot="title">
                    <span style="margin-right: 10px;">数据源({{item.dbname}} - {{item.tableName}})</span>
                    <el-button type="danger" size="mini" icon="minus" @click.stop="deleteDataSource(item)"></el-button>
                    <span style="color: red;margin-left: 10px;">{{item.errorMessage}}</span>
                </template>
                <index-data-source-item ref="dataSourceItem" :index-operation="indexOperation" :data-source="item" @db-validate="dbValidate"></index-data-source-item>
            </el-collapse-item>
        </el-collapse>
    </div>
</template>

<script>

export default {
  props: ['dataSources', 'showImportObject', 'indexId', 'indexOperation'],
  data() {
    return {
      dataSourceInfo: {
        ip: '',
        port: '',
        username: '',
        password: '',
        dbname: '',
        tableName: '',
        isGeneratePwd: true,
      },
    };
  },
  methods: {
    dbValidate(dataSource, callback) {
      const arr = this.dataSources.filter(ele =>
        (ele.dbname === dataSource.dbname && ele.tableName === dataSource.tableName),
      );
      if (arr.length > 1) {
        callback(new Error('数据库名和表名需唯一，不能重复'));
      } else {
        callback();
      }
    },
    importDs() {
      this.showImportObject.show = true;
    },
    exportDs() {
      window.location.href = `/pallas/ds/export.json?indexId=${this.indexId}`;
    },
    addDataSource() {
      const dataSource = JSON.parse(JSON.stringify(this.dataSourceInfo));
      this.dataSources.push(dataSource);
    },
    deleteDataSource(item) {
      this.$array.removeByValue(this.dataSources, item);
    },
  },
};
</script>
<style>
.data-source-content {
    margin: -10px 0 10px 0;
}
.data-source-add {
    padding: 5px;
}

</style>
