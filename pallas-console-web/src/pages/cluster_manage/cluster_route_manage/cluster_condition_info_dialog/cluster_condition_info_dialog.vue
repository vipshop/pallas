<template>
    <el-dialog :title="conditionInfoTitle" size="large" v-model="isVisible" :before-close="closeDialog">
        <el-form :model="conditionInfo" :rules="rules" ref="conditionInfo" label-width="120px">
            <el-row :gutter="30">
                <el-col :span="10">
                    <el-form-item prop="name" label="规则名称">
                        <el-input v-model="conditionInfo.name" :disabled="isEditable"></el-input>
                    </el-form-item>
                    <el-form-item label="优先级" prop="priority">
                        <el-select v-model="conditionInfo.priority" placeholder="优先于选中规则" style="width: 100%;">
                            <el-option v-for="item in alternativeConditions" :key="item" :label="item" :value="item"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="是否启用" prop="enable">
                        <div class="my-switch">
                            <el-switch v-model="conditionInfo.enable"></el-switch>
                        </div>
                    </el-form-item>
                    <el-form-item prop="conditionRelation" label="条件关系" v-if="conditionInfo.conditions.length > 1">
                        <div class="my-switch">
                            <el-select v-model="conditionInfo.conditionRelation" style="width: 100%;">
                                <el-option v-for="item in conditionRelations" :key="item" :label="item" :value="item"></el-option>
                            </el-select>
                        </div>
                    </el-form-item>
                </el-col>
                <el-col :span="14">
                    <el-form-item prop="" label="" label-width="0px">
                        <div class="target-group-transfer">
                            <el-transfer
                              ref="targetGroupTransfer"
                              v-model="conditionInfo.targetGroupsId"
                              :render-content="renderFunc"
                              :titles="['所有节点集', '应用']"
                              :data="routingTargetGroups"
                              @change="handleChange"
                              :props="defaultProps">
                            </el-transfer>
                        </div>
                    </el-form-item>
                </el-col>
            </el-row>
        </el-form>
        <conditions :conditions="conditionInfo.conditions" ref="conditions"></conditions>
        <div slot="footer" class="dialog-footer">    
           <el-button @click="closeDialog()">取消</el-button>
           <el-button type="confirm" @click="submitInfo()">保存</el-button> 
        </div>
    </el-dialog>
</template>

<script>
import Conditions from './cluster_conditions';

export default {
  props: ['conditions', 'conditionInfo', 'conditionInfoTitle', 'conditionOperation', 'routingTargetGroups'],
  data() {
    return {
      isVisible: true,
      rules: {
        name: [{ required: true, message: '规则名称不能为空', trigger: 'blur' }],
        sourceType: [{ required: true, message: '数据源类型不能为空', trigger: 'blur' }],
        sourceParameter: [{ required: true, message: '条件判断参数名不能为空', trigger: 'blur' }],
        condExpr: [{ required: true, message: '条件判断运算不能为空', trigger: 'blur' }],
        sourceValue: [{ required: true, message: '条件判断值不能为空', trigger: 'blur' }],
        conditionRelation: [{ required: true, message: '请选择规则关系', trigger: 'change' }],
      },
      conditionRelations: ['OR', 'AND'],
      defaultProps: {
        key: 'id',
        label: 'name',
      },
    };
  },
  methods: {
    handleChange(arr, direction, movedKeys) {
      if (direction === 'right') {
        this.changeDirection(movedKeys, 'right');
      } else {
        this.changeDirection(movedKeys, 'left');
      }
    },
    changeDirection(arr, direction) {
      arr.forEach((ele1) => {
        this.routingTargetGroups.forEach((ele2) => {
          if (ele1 === ele2.id) {
            this.$set(ele2, 'position', direction);
          }
        });
      });
    },
    renderFunc(h, option) {
      const self = this;
      let renderHtml;
      if (option.position === 'left') {
        renderHtml = h(
          'div',
          [
            h('span', { style: { float: 'left' } }, option.name),
          ],
        );
      } else {
        renderHtml = h(
          'div',
          [
            h('span', { style: { float: 'left' } }, option.name),
            h('input', {
              domProps: { value: option.weight || '0' },
              class: { 'el-input__inner': true },
              style: { float: 'right', width: '50px', height: '20px', margin: '6px' },
              on: {
                input: function setInput(event) {
                  self.$set(option, 'weight', event.target.value);
                  self.$emit('input', event.target.value);
                },
              },
            }),
          ],
        );
      }
      return renderHtml;
    },
    submitInfo() {
      this.$refs.conditionInfo.validate((valid1) => {
        if (valid1) {
          let count = 0;
          if (this.conditionInfo.conditions.length !== 0) {
            this.$refs.conditions.$refs.conditionItem.forEach((element) => {
              element.$refs.conditionItemForm.validate((valid2) => {
                if (valid2) {
                  count += 1;
                }
              });
            });
          }
          if (count === this.conditionInfo.conditions.length) {
            this.conditionInfo.targetGroups = this.getTargetGroupsList();
            const conditionInfoObject = this.setConditionInfo();
            if (this.targetGroupWeightValidate(conditionInfoObject.targetGroups)) {
              if (this.conditionOperation === 'add') {
                this.$emit('add-condition', conditionInfoObject);
              } else {
                this.$emit('edit-condition', conditionInfoObject);
              }
            }
          }
        }
      });
    },
    targetGroupWeightValidate(arr) {
      let flag = false;
      if (arr.length !== 0) {
        const parten = /^([0-9]\d{0,1}|100)$/;
        const error = arr.some((ele) => {
          if (parten.test(ele.weight)) {
            return false;
          }
          return true;
        });
        if (error) {
          flag = false;
          this.$message.errorMessage('请正确输入节点集权重, 0-100整数!');
        } else {
          flag = true;
        }
      } else {
        flag = false;
        this.$message.errorMessage('请选择节点集!');
      }
      return flag;
    },
    setConditionInfo() {
      const conditionObject = {};
      conditionObject.name = this.conditionInfo.name;
      conditionObject.targetGroups = this.conditionInfo.targetGroups;
      conditionObject.enable = this.conditionInfo.enable;
      conditionObject.conditionRelation = this.conditionInfo.conditionRelation;
      conditionObject.priority = this.conditionInfo.priority;
      conditionObject.conditions = this.conditionInfo.conditions;
      return conditionObject;
    },
    getTargetGroupsList() {
      const list = this.$refs.targetGroupTransfer.targetData.map((obj) => {
        const rObj = {};
        rObj.id = obj.id;
        rObj.weight = obj.weight;
        return rObj;
      });
      return list;
    },
    closeDialog() {
      this.$emit('close-dialog');
    },
  },
  computed: {
    isEditable() {
      return this.conditionOperation === 'edit';
    },
    alternativeConditions() {
      if (this.conditionOperation === 'edit') {
        this.$array.removeByValue(this.conditions, this.conditionInfo.name);
        return this.conditions;
      }
      return this.conditions;
    },
  },
  components: {
    Conditions,
  },
};
</script>
<style>
.target-group-transfer .el-transfer-panel {
    min-width: 43%;
}
</style>
