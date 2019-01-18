<template>
    <div :id="editorId" style="width: 100%; height: 100%;"></div>
</template>

<script>
import ace from 'brace';
import 'brace/theme/monokai';
import 'brace/mode/json';

export default {
  props: ['editorId', 'content'],
  data() {
    return {
      editor: Object,
      beforeContent: '',
    };
  },
  watch: {
    content: function onContentChange(value) {
      if (this.beforeContent !== value) {
        this.editor.setValue(value, 1);
      }
    },
  },
  mounted() {
    this.editor = ace.edit(this.editorId);
    this.editor.setValue(this.content, 1);

    this.editor.getSession().setMode('ace/mode/json');
    this.editor.setTheme('ace/theme/monokai');

    this.editor.on('change', () => {
      this.beforeContent = this.editor.getValue();
      this.$emit('change-content', this.editor.getValue());
    });
  },
  destroyed() {
    this.editor.destroy();
    this.editor.container.remove();
  },
};
</script>
