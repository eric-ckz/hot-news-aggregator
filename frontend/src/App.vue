<template>
  <div class="app">
    <header class="header">
      <div class="header-content">
        <div class="logo">
          <div class="logo-icon">
            <el-icon size="24"><TrendCharts /></el-icon>
          </div>
          <span class="title">热搜聚合</span>
        </div>
        <div class="actions">
          <el-button 
            type="primary" 
            :icon="Refresh" 
            :loading="loading"
            @click="refreshData"
            class="refresh-btn"
          >
            刷新数据
          </el-button>
        </div>
      </div>
    </header>
    <main class="main">
      <router-view />
    </main>
    <footer class="footer">
      <p>热搜聚合平台 © 2024</p>
    </footer>
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

<style lang="scss">
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 25%, #f093fb 50%, #4facfe 75%, #00f2fe 100%);
  background-attachment: fixed;
  min-height: 100vh;
  color: #fff;
}

.app {
  min-height: 100vh;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.header {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  position: sticky;
  top: 0;
  z-index: 100;

  .header-content {
    max-width: 1400px;
    margin: 0 auto;
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
  }

  .logo {
    display: flex;
    align-items: center;
    gap: 12px;

    .logo-icon {
      width: 40px;
      height: 40px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
    }

    .title {
      font-size: 22px;
      font-weight: 700;
      color: #fff;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }
  }

  .refresh-btn {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    border-radius: 10px;
    padding: 10px 20px;
    font-weight: 500;
    color: #fff;
    transition: all 0.3s ease;
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 25px rgba(102, 126, 234, 0.5);
    }
  }
}

.main {
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px 24px;
  min-height: calc(100vh - 128px);
}

.footer {
  text-align: center;
  padding: 24px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 13px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}
</style>
