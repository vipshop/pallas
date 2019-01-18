<template>
    <el-dialog v-model="isVisible" :before-close="handleClose">
        <span slot="title" class="el-dialog__title">模板差异对比( <span class="del">- 删除</span> / <span class="ins">+ 增加</span> )</span>
        <div class="view-content">
            <pre class="text-diff" v-line-diff="jsonDiffInfo"></pre>
        </div>
        <div slot="footer" class="dialog-footer">    
            <el-button @click="handleClose()">取消</el-button>
            <el-button type="confirm" v-if="isOverwrite" @click="handleOverwrite">覆盖当前版本</el-button>
        </div>
    </el-dialog>
</template>

<script>

export default {
  props: ['isOverwrite', 'jsonDiffInfo'],
  data() {
    return {
      isVisible: true,
    };
  },
  methods: {
    handleClose() {
      this.$emit('close-dialog');
    },
    handleOverwrite() {
      this.$emit('overwrite-operate');
    },
  },
};
</script>

<style scoped>
.text-diff {
  display: block;
  padding: 9.5px;
  margin: 0 0 10px;
  font-size: 13px;
  line-height: 1.42857143;
  color: #333;
  word-break: break-all;
  word-wrap: break-word;
  background-color: #373a3c;
  border: 1px solid #808080;
  border-radius: 4px;
  overflow: auto;
}
</style>

<style>
.match, .text-diff span {
    color: #fff
}
.ins, ins {
    color: #fff;
    background: #00AA33
}
.del, del {
    color: #fff;
    background: #980000
}
.noselect {
  user-select: none;
}
</style>
