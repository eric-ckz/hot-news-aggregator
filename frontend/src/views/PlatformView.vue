<template>
  <div class="platform-view">
    <div class="page-header">
      <el-button 
        text 
        :icon="ArrowLeft" 
        @click="goBack"
        class="back-btn"
      >
        返回首页
      </el-button>
      <h1 class="platform-title">{{ platformName }}</h1>
      <div class="placeholder"></div>
    </div>

    <div class="hot-search-list" v-loading="loading">
      <div
        v-for="(item, index) in hotSearches"
        :key="item.id"
        class="hot-search-item"
        :class="{ 'top-three': index < 3 }"
        @click="openLink(item.url)"
      >
        <div class="rank" :class="`rank-${index + 1}`">
          {{ index + 1 }}
        </div>
        <div class="info">
          <h3 class="title">{{ item.title }}</h3>
          <p v-if="item.description" class="description">
            {{ item.description }}
          </p>
        </div>
        <div class="meta">
          <el-tag v-if="item.category" size="small" type="info" effect="dark">
            {{ item.category }}
          </el-tag>
          <span v-if="item.heatValue" class="heat-value">
            <el-icon><Histogram /></el-icon>
            {{ formatHeatValue(item.heatValue) }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Histogram } from '@element-plus/icons-vue'
import { hotSearchApi } from '../api/hotSearch'
import { PLATFORM_CONFIG } from '../types/hotSearch'
import type { HotSearch } from '../types/hotSearch'

const route = useRoute()
const router = useRouter()
const platform = computed(() => route.params.platform as string)
const platformName = computed(() => PLATFORM_CONFIG[platform.value]?.name || platform.value)

const hotSearches = ref<HotSearch[]>([])
const loading = ref(false)

const goBack = () => {
  router.push('/')
}

const openLink = (url: string) => {
  window.open(url, '_blank', 'noopener,noreferrer')
}

const formatHeatValue = (value: number): string => {
  if (value >= 10000) {
    return (value / 10000).toFixed(1) + '万'
  }
  return value.toString()
}

onMounted(async () => {
  loading.value = true
  try {
    const response = await hotSearchApi.getHotSearchesByPlatform(platform.value, 50)
    if (response.data.code === 200) {
      hotSearches.value = response.data.data
    }
  } catch (error) {
    console.error('获取热搜数据失败:', error)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped lang="scss">
.platform-view {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.2);
  backdrop-filter: blur(10px);

  .page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 24px;
    padding-bottom: 20px;
    border-bottom: 1px solid rgba(102, 126, 234, 0.1);

    .back-btn {
      color: #667eea;
      font-size: 14px;
      font-weight: 500;

      &:hover {
        color: #764ba2;
      }
    }

    .platform-title {
      font-size: 24px;
      font-weight: 700;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      margin: 0;
    }

    .placeholder {
      width: 80px;
    }
  }

  .hot-search-list {
    .hot-search-item {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 16px 20px;
      margin-bottom: 8px;
      border-radius: 12px;
      background: #f8f9fa;
      border: 1px solid rgba(102, 126, 234, 0.1);
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
        transform: translateX(4px);
        border-color: rgba(102, 126, 234, 0.2);
      }

      &.top-three {
        border-left: 3px solid #ffd700;
      }

      .rank {
        width: 36px;
        height: 36px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 700;
        font-size: 14px;
        background: #e9ecef;
        color: #606266;
        flex-shrink: 0;

        &.rank-1 {
          background: linear-gradient(135deg, #ffd700, #ffaa00);
          color: #000;
          box-shadow: 0 4px 15px rgba(255, 215, 0, 0.3);
        }

        &.rank-2 {
          background: linear-gradient(135deg, #c0c0c0, #a0a0a0);
          color: #000;
          box-shadow: 0 4px 15px rgba(192, 192, 192, 0.3);
        }

        &.rank-3 {
          background: linear-gradient(135deg, #cd7f32, #b87333);
          color: #fff;
          box-shadow: 0 4px 15px rgba(205, 127, 50, 0.3);
        }
      }

      .info {
        flex: 1;
        min-width: 0;

        .title {
          margin: 0;
          font-size: 15px;
          font-weight: 500;
          color: #303133;
          line-height: 1.5;
        }

        .description {
          margin: 6px 0 0;
          font-size: 13px;
          color: #909399;
          line-height: 1.4;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
        }
      }

      .meta {
        display: flex;
        align-items: center;
        gap: 12px;
        flex-shrink: 0;

        .heat-value {
          display: flex;
          align-items: center;
          gap: 4px;
          color: #667eea;
          font-size: 13px;
          font-weight: 600;
          background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
          padding: 6px 12px;
          border-radius: 8px;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .platform-view {
    padding: 16px;

    .page-header {
      .platform-title {
        font-size: 18px;
      }
    }

    .hot-search-list {
      .hot-search-item {
        padding: 12px 16px;
        flex-wrap: wrap;

        .meta {
          width: 100%;
          margin-top: 8px;
          justify-content: flex-start;
        }
      }
    }
  }
}
</style>
