<template>
    <div>
        <el-form :model="conditionItem" :rules="rules" ref="conditionItemForm" label-width="120px">
            <el-row :gutter="20">
                <el-col :span="8">
                    <el-form-item label="判断参数名" prop="paramName">
                        <el-select filterable allow-create v-model="conditionItem.paramName" style="width: 100%;">
                            <el-option v-for="item in paramNames" :key="item" :label="item" :value="item"></el-option>
                        </el-select>
                    </el-form-item>
                </el-col>
                <el-col :span="8">
                    <el-form-item label="判断运算" prop="exprOp">
                        <el-select v-model="conditionItem.exprOp" style="width: 100%;">
                            <el-option v-for="item in exprOps" :key="item" :label="item" :value="item"></el-option>
                        </el-select>
                    </el-form-item>
                </el-col>
                <el-col :span="8">
                    <el-form-item label="判断值" prop="paramValue">
                        <el-input v-model="conditionItem.paramValue" placeholder="多个判断值请以 , 分隔"></el-input>
                    </el-form-item>
                </el-col>
            </el-row>
        </el-form>
    </div>
</template>

<script>

export default {
  props: ['conditionItem'],
  data() {
    return {
      rules: {
        exprOp: [{ required: true, message: '请选择判断运算', trigger: 'change' }],
        paramName: [{ required: true, message: '请输入判断参数名', trigger: 'change' }],
        paramValue: [{ required: true, message: '请输入判断值', trigger: 'blur' }],
      },
      exprOps: ['=', 'ip_mask'],
      paramNames: ['X-PALLAS-SEARCH-CLIENT-IP', 'X-PALLAS-SEARCH-TEMPLATE-ID'],
    };
  },
};
</script>
