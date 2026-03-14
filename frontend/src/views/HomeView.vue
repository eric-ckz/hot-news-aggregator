<template>
  <div class="home-view">
    <!-- 错误提示 -->
    <div v-if="store.error" class="error-alert">
      <el-alert :title="store.error" type="error" show-icon closable @close="store.error = null" />
    </div>

    <div v-if="loading" class="loading-container">
      <div class="loading-grid">
        <div v-for="i in 6" :key="i" class="loading-card">
          <el-skeleton animated>
            <template #template>
              <div style="padding: 16px">
                <el-skeleton-item variant="h3" style="width: 50%; margin-bottom: 16px" />
                <el-skeleton-item v-for="j in 5" :key="j" variant="text" style="margin-bottom: 8px" />
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>
    </div>

    <template v-else>
      <div class="drag-hint">
        <el-icon><Rank /></el-icon>
        <span>拖拽卡片可调整位置</span>
      </div>

      <VueDraggable
        v-model="orderedPlatforms"
        class="platforms-grid"
        :animation="300"
        handle=".card-header"
        ghost-class="sortable-ghost"
        drag-class="sortable-drag"
      >
        <PlatformCard
          v-for="platform in orderedPlatforms"
          :key="platform"
          :platform="platform"
          :hot-searches="hotSearches[platform] || []"
          @refresh="handlePlatformRefresh"
        />
      </VueDraggable>

      <!-- 空状态提示 -->
      <div v-if="orderedPlatforms.length === 0" class="empty-state">
        <el-empty description="暂无平台数据，请检查后端服务是否正常" />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed, ref, watch } from 'vue'
import { VueDraggable } from 'vue-draggable-plus'
import { Rank } from '@element-plus/icons-vue'
import { useHotSearchStore } from '../stores/hotSearch'
import PlatformCard from '../components/PlatformCard.vue'

const store = useHotSearchStore()

const hotSearches = computed(() => store.allHotSearches)
const loading = computed(() => store.isLoading)

// 可拖拽排序的平台列表
const orderedPlatforms = ref<string[]>([])

// 从 store 获取平台列表并初始化
watch(() => store.allPlatforms, (platforms) => {
  if (platforms.length > 0 && orderedPlatforms.value.length === 0) {
    // 尝试从 localStorage 读取保存的顺序
    const savedOrder = localStorage.getItem('platformOrder')
    if (savedOrder) {
      const parsed = JSON.parse(savedOrder)
      // 只保留当前存在的平台
      orderedPlatforms.value = parsed.filter((p: string) => platforms.includes(p))
      // 添加新平台（如果有）
      const newPlatforms = platforms.filter(p => !orderedPlatforms.value.includes(p))
      orderedPlatforms.value.push(...newPlatforms)
    } else {
      orderedPlatforms.value = [...platforms]
    }
  }
}, { immediate: true })

// 保存排序到 localStorage
watch(orderedPlatforms, (newOrder) => {
  localStorage.setItem('platformOrder', JSON.stringify(newOrder))
}, { deep: true })

// 处理单个平台刷新
const handlePlatformRefresh = async (platform: string) => {
  await store.fetchHotSearchesByPlatform(platform, 50)
}

onMounted(async () => {
  await store.fetchPlatforms()
  await store.fetchAllHotSearches(50) // 获取更多数据
})
</script>

<style scoped lang="scss">
.home-view {
  .drag-hint {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    margin-bottom: 20px;
    padding: 12px 20px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 20px;
    color: rgba(255, 255, 255, 0.8);
    font-size: 14px;
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.1);

    .el-icon {
      font-size: 16px;
    }
  }

  .loading-container {
    .loading-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 16px;

      .loading-card {
        background: rgba(255, 255, 255, 0.95);
        border-radius: 12px;
        overflow: hidden;
        box-shadow: 0 4px 20px rgba(102, 126, 234, 0.15);
      }
    }
  }

  .platforms-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .home-view {
    .platforms-grid,
    .loading-grid {
      grid-template-columns: 1fr;
    }

    .drag-hint {
      font-size: 12px;
      padding: 10px 16px;
    }
  }
}

.error-alert {
  margin-bottom: 20px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}
</style>
