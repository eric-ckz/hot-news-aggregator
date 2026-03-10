<template>
  <div class="app">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <div class="logo">
            <el-icon size="28"><TrendCharts /></el-icon>
            <span class="title">热搜聚合平台</span>
          </div>
          <div class="actions">
            <el-button 
              type="primary" 
              :icon="Refresh" 
              :loading="loading"
              @click="refreshData"
            >
              刷新数据
            </el-button>
          </div>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
      <el-footer class="footer">
        <p>热搜聚合平台 © 2024 - 实时聚合各大平台热门资讯</p>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Refresh, TrendCharts } from '@element-plus/icons-vue'
import { useHotSearchStore } from './stores/hotSearch'
import { ElMessage } from 'element-plus'

const store = useHotSearchStore()
const loading = ref(false)

const refreshData = async () => {
  loading.value = true
  try {
    await store.refreshAllHotSearches()
    ElMessage.success('数据刷新成功')
  } catch (error) {
    ElMessage.error('刷新失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.app {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 0;

  .header-content {
    max-width: 1400px;
    margin: 0 auto;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
  }

  .logo {
    display: flex;
    align-items: center;
    gap: 12px;
    color: #409eff;

    .title {
      font-size: 20px;
      font-weight: 600;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
  }
}

.main {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  min-height: calc(100vh - 120px);
}

.footer {
  background: rgba(255, 255, 255, 0.9);
  text-align: center;
  color: #666;
  font-size: 14px;

  p {
    margin: 0;
    line-height: 60px;
  }
}
</style>
