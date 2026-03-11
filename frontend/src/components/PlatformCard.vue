<template>
  <div class="platform-card">
    <div class="card-header">
      <div class="platform-info">
        <div class="platform-icon">
          <el-icon size="16" color="#fff">
            <component :is="platformIcon" />
          </el-icon>
        </div>
        <span class="platform-name">{{ platformName }}</span>
        <span class="item-count">({{ hotSearches.length }})</span>
      </div>
      <div class="header-actions">
        <el-button
          text
          type="primary"
          :icon="Refresh"
          :loading="refreshing"
          @click.stop="handleRefresh"
          class="refresh-btn"
          title="刷新此平台"
        />
        <el-button
          text
          type="primary"
          @click="viewMore"
          class="view-more"
        >
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
    </div>

    <div class="hot-search-list" v-if="hotSearches.length > 0" v-loading="refreshing">
      <div
        v-for="(item, index) in hotSearches"
        :key="item.id"
        class="hot-search-item"
        @click="openLink(item.url)"
      >
        <div class="rank" :class="`rank-${item.rankNum || index + 1}`">
          {{ item.rankNum || index + 1 }}
        </div>
        <div class="content">
          <div class="title" :title="item.title">
            {{ item.title }}
          </div>
          <div v-if="item.heatValue" class="heat">
            {{ formatHeatValue(item.heatValue) }}
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <el-empty description="暂无数据" :image-size="40" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, Histogram, Refresh } from '@element-plus/icons-vue'
import { PLATFORM_CONFIG } from '../types/hotSearch'
import type { HotSearch } from '../types/hotSearch'
import { hotSearchApi } from '../api/hotSearch'

const props = defineProps<{
  platform: string
  hotSearches: HotSearch[]
}>()

const emit = defineEmits<{
  (e: 'refresh', platform: string): void
}>()

const router = useRouter()
const refreshing = ref(false)

const platformConfig = computed(() => PLATFORM_CONFIG[props.platform] || {
  name: props.platform,
  icon: 'TrendCharts',
  color: '#667eea',
})

const platformName = computed(() => platformConfig.value.name)
const platformIcon = computed(() => platformConfig.value.icon)

const handleRefresh = async () => {
  refreshing.value = true
  try {
    // 先触发后端刷新
    await hotSearchApi.refreshPlatformHotSearches(props.platform)
    // 然后重新获取数据
    emit('refresh', props.platform)
  } catch (error) {
    console.error('刷新失败:', error)
  } finally {
    refreshing.value = false
  }
}

const viewMore = () => {
  router.push(`/platform/${props.platform}`)
}

const openLink = (url: string) => {
  window.open(url, '_blank', 'noopener,noreferrer')
}

const formatHeatValue = (value: number): string => {
  if (value >= 10000) {
    return (value / 10000).toFixed(1) + 'w'
  }
  return value.toString()
}
</script>

<style scoped lang="scss">
.platform-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.15);
  transition: all 0.3s ease;
  overflow: hidden;
  backdrop-filter: blur(10px);
  display: flex;
  flex-direction: column;
  height: 380px;

  &:hover {
    box-shadow: 0 8px 30px rgba(102, 126, 234, 0.25);
  }

  // 拖拽时的样式
  &.sortable-ghost {
    opacity: 0.5;
    background: rgba(102, 126, 234, 0.1);
  }

  &.sortable-drag {
    cursor: grabbing;
    transform: rotate(2deg) scale(1.02);
    box-shadow: 0 15px 40px rgba(102, 126, 234, 0.35);
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 12px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    flex-shrink: 0;
    cursor: grab;

    &:active {
      cursor: grabbing;
    }

    .platform-info {
      display: flex;
      align-items: center;
      gap: 6px;

      .platform-icon {
        width: 26px;
        height: 26px;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 6px;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      .platform-name {
        font-size: 14px;
        font-weight: 600;
        color: #fff;
      }

      .item-count {
        font-size: 11px;
        color: rgba(255, 255, 255, 0.7);
        background: rgba(255, 255, 255, 0.15);
        padding: 1px 6px;
        border-radius: 8px;
      }
    }

    .header-actions {
      display: flex;
      align-items: center;
      gap: 2px;

      .refresh-btn,
      .view-more {
        font-size: 12px;
        color: rgba(255, 255, 255, 0.9);
        padding: 4px;
        height: 28px;
        width: 28px;

        &:hover {
          color: #fff;
          background: rgba(255, 255, 255, 0.15);
        }
      }

      .refresh-btn:hover {
        transform: rotate(180deg);
        transition: transform 0.5s ease;
      }
    }
  }

  .hot-search-list {
    flex: 1;
    overflow-y: auto;
    padding: 8px;

    // 自定义滚动条样式
    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-track {
      background: rgba(102, 126, 234, 0.05);
      border-radius: 2px;
    }

    &::-webkit-scrollbar-thumb {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 2px;

      &:hover {
        background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
      }
    }

    .hot-search-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 6px 8px;
      border-radius: 6px;
      cursor: pointer;
      transition: all 0.2s ease;
      margin-bottom: 2px;

      &:last-child {
        margin-bottom: 0;
      }

      &:hover {
        background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
      }

      .rank {
        width: 20px;
        height: 20px;
        border-radius: 4px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 11px;
        font-weight: 700;
        background: #f0f2f5;
        color: #606266;
        flex-shrink: 0;

        &.rank-1 {
          background: linear-gradient(135deg, #ffd700, #ffaa00);
          color: #000;
          box-shadow: 0 2px 8px rgba(255, 215, 0, 0.3);
        }

        &.rank-2 {
          background: linear-gradient(135deg, #c0c0c0, #a0a0a0);
          color: #000;
          box-shadow: 0 2px 8px rgba(192, 192, 192, 0.3);
        }

        &.rank-3 {
          background: linear-gradient(135deg, #cd7f32, #b87333);
          color: #fff;
          box-shadow: 0 2px 8px rgba(205, 127, 50, 0.3);
        }
      }

      .content {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 8px;
        min-width: 0;

        .title {
          flex: 1;
          font-size: 12px;
          color: #303133;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          line-height: 1.3;
        }

        .heat {
          font-size: 10px;
          color: #667eea;
          flex-shrink: 0;
          background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
          padding: 2px 6px;
          border-radius: 4px;
          font-weight: 500;
        }
      }
    }
  }

  .empty-state {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>
