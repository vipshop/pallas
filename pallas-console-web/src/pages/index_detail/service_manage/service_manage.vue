<template>
    <div class="my-tab-content">
        <el-tabs v-model="tabActive">
            <el-tab-pane label="超时重试" name="timeout_retry">
                <timeout-retry-manage></timeout-retry-manage>
            </el-tab-pane>
            <el-tab-pane label="限流配置" name="throttling">
                <throttling-manage></throttling-manage>
            </el-tab-pane>
        </el-tabs>
    </div>
</template>

<script>
    import TimeoutRetryManage from './timeout_retry_dialog/timeout_retry_manage';
    import ThrottlingManage from './throttling_dialog/throttling_manage';

    export default {
      components: {
        'timeout-retry-manage': TimeoutRetryManage,
        'throttling-manage': ThrottlingManage,
      },
      data() {
        return {
          tabActive: 'timeout_retry',
        };
      },
    };
</script>
