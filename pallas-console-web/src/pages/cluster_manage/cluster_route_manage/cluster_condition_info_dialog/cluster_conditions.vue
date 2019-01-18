<template>
    <div>
        <div class="condition-add">
            <el-button size="mini" icon="plus" @click="addConditionRelate">新增规则条件</el-button>
            <span class="warning">
                <el-tooltip class="item" effect="dark" content="若不增加规则条件，则会默认命中节点集" placement="right">
                    <i class="fa fa-info-circle"></i>
                </el-tooltip>
            </span>
        </div>
        <el-collapse v-model="index" v-for="(item, index) in conditions" :key="index">
            <el-collapse-item :name="index">
                <template slot="title">
                    <span style="margin-right: 10px;">条件</span>
                    <el-button type="danger" size="mini" icon="minus" @click.stop="deleteConditionRelate(item)"></el-button>
                </template>
                <condition-item ref="conditionItem" :condition-item="item"></condition-item>
            </el-collapse-item>
        </el-collapse>
    </div>
</template>

<script>
import ConditionItem from './cluster_condition_item';

export default {
  props: ['conditions'],
  data() {
    return {
      conditionRelateInfo: {
        paramType: 'header',
        exprOp: '',
        paramName: '',
        paramValue: '',
      },
    };
  },
  methods: {
    addConditionRelate() {
      const conditionRelate = JSON.parse(JSON.stringify(this.conditionRelateInfo));
      this.conditions.push(conditionRelate);
    },
    deleteConditionRelate(item) {
      this.$array.removeByValue(this.conditions, item);
    },
  },
  components: {
    'condition-item': ConditionItem,
  },
};
</script>
<style scoped>
.condition-add {
  margin: 10px 5px;
}
.condition-add .warning i{
  color: red;
}
</style>
