<template>
    <el-dialog v-model="isVisible" :before-close="handleClose">
        <span slot="title" class="el-dialog__title">模板差异对比( <span class="del">- 删除</span> / <span class="ins">+ 增加</span> )</span>
        <div class="view-content">
            <el-scrollbar>
              <pre v-line-diff="jsonDiffInfo"></pre>
            </el-scrollbar>
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
