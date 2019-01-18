<template>
    <el-dialog :title="title" v-model="isVisible" :before-close="closeDialog">
        <fieldset class="no-border">
            <div class="view-content">
                <pre>{{content}}</pre>
            </div>
        </fieldset>
    </el-dialog>
</template>

<script>
export default {
  props: ['title', 'content'],
  data() {
    return {
      isVisible: true,
    };
  },
  methods: {
    closeDialog() {
      this.$emit('close-dialog');
    },
  },
};
</script>
