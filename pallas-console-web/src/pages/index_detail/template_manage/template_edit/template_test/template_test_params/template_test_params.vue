<template>
    <div>
        <div class="template-test-content">
            <div class="template-test-head">设置请求参数</div>
            <div class="template-test-body">
                <el-table :data="paramsInfo" border style="width: 100%">
                    <el-table-column prop="paramName" label="请求参数"></el-table-column>
                    <el-table-column label="包含">
                        <template scope="scope">
                            <el-checkbox v-model="scope.row.include"></el-checkbox>
                        </template>
                    </el-table-column>
                    <el-table-column label="请求值类型">
                        <template scope="scope">
                            <el-select v-model="scope.row.valueType" :disabled="!scope.row.include" @change="valueTypeChange(scope.row)" size="small">
                                <el-option label="固定值" value="1" key="1">固定值</el-option>
                                <el-option label="数据源" value="2" key="2">数据源</el-option>
                            </el-select>
                        </template>
                    </el-table-column>
                    <el-table-column label="数据源参数映射">
                        <template scope="scope">
                            <el-input v-show="scope.row.valueType === '1'" :disabled="!scope.row.include" v-model="scope.row.value"></el-input>
                            <el-select style="width:100%;" v-show="scope.row.valueType === '2'" :disabled="!scope.row.include" v-model="scope.row.value">
                                <el-option v-for="item in paramValues" :label="item" :value="item" :key="item"></el-option>
                            </el-select>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
        </div>
        <div align="center" class="template-test-content">
            <el-button type="primary" @click="handleGen">生成测试脚本</el-button>
        </div>
    </div>
</template>

<script>
export default {
  props: ['indexId', 'templateName', 'paramsInfo', 'dataSource'],
  data() {
    return {
    };
  },
  methods: {
    valueTypeChange(row) {
      this.$set(row, 'value', '');
    },
    handleGen() {
      const arr = [];
      this.paramsInfo.forEach((element) => {
        if (element.include) {
          arr.push(element);
        }
      });

      const isError = arr.some((element) => {
        if (!element.value) {
          this.$message.errorMessage('数据源参数映射不能为空!');
          return true;
        }
        return false;
      });

      if (arr.length) {
        if (!isError) {
          const dataParams = {
            indexId: this.indexId,
            templateName: this.templateName,
            params: arr,
          };
          const dataParamsJson = encodeURIComponent(JSON.stringify(dataParams));
          window.location.href = `/pallas/index_template/performance_script/gen.json?params=${dataParamsJson}`;
        }
      } else {
        this.$message.errorMessage('请至少选择一个参数！');
      }
    },
  },
  computed: {
    paramValues() {
      const arr = [];
      this.dataSource.forEach((element1) => {
        if (element1.paramNameDef.indexOf(';') <= 0) {
          arr.push(element1.paramNameDef);
        } else {
          const arr1 = element1.paramNameDef.split(';');
          arr1.forEach((element2) => {
            arr.push(element2);
          });
        }
      });
      this.paramsInfo.forEach((element2) => {
        if (element2.valueType === '2') {
          const isFind = arr.some((elm) => {
            if (elm === element2.value) {
              return true;
            }
            return false;
          });
          if (!isFind) {
            this.$set(element2, 'value', '');
          }
        }
      });
      return arr;
    },
  },
};
</script>
