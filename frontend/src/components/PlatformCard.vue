<template>
  <el-card class="platform-card" shadow="hover">
    <template #header>
      <div class="card-header" :style="{ borderLeftColor: platformColor }">
        <div class="platform-info">
          <el-icon :size="24" :color="platformColor">
            <component :is="platformIcon" />
          </el-icon>
          <span class="platform-name">{{ platformName }}</span>
        </div>
        <el-button 
          text 
          type="primary" 
          @click="viewMore"
          class="view-more"
        >
          查看更多
          <el-icon class="el-icon--right"><ArrowRight /></el-icon>
        </el-button>
      </div>
    </template>

    <div class="hot-search-list">
      <div 
        v-for="(item, index) in displayHotSearches" 
        :key="item.id"
        class="hot-search-item"
        @click="openLink(item.url)"
      >
        <div class="rank" :class="`rank-${index + 1}`">
          {{ item.rankNum || index + 1 }}
        </div>
        <div class="title" :title="item.title">
          {{ item.title }}
        </div>
        <div v-if="item.heatValue" class="heat">
          <el-icon><Fire /></el-icon>
          <span>{{ formatHeatValue(item.heatValue) }}</span>
        </div>
      </div>
    </div>

    <div v-if="hotSearches.length === 0" class="empty-state">
      <el-empty description="暂无数据" :image-size="60" />
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, Fire } from '@element-plus/icons-vue'
import { PLATFORM_CONFIG } from '../types/hotSearch'
import type { HotSearch } from '../types/hotSearch'

const props = defineProps<{
  platform: string
  hotSearches: HotSearch[]
}>()

const router = useRouter()

const platformConfig = computed(() => PLATFORM_CONFIG[props.platform] || {
  name: props.platform,
  icon: 'TrendCharts',
  color: '#409eff',
})

const platformName = computed(() => platformConfig.value.name)
const platformIcon = computed(() => platformConfig.value.icon)
const platformColor = computed(() => platformConfig.value.color)

const displayHotSearches = computed(() => {
  return props.hotSearches.slice(0, 10)
})

const viewMore = () => {
  router.push(`/platform/${props.platform}`)
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
</script>

<style scoped lang="scss">
.platform-card {
  border-radius: 12px;
  transition: all 0.3s ease;
  height: 100%;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  }

  :deep(.el-card__header) {
    padding: 16px 20px;
    border-bottom: 1px solid #ebeef5;
  }

  :deep(.el-card__body) {
    padding: 16px 20px;
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-left: 4px solid;
    padding-left: 12px;
    margin-left: -20px;

    .platform-info {
      display: flex;
      align-items: center;
      gap: 10px;

      .platform-name {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }
    }

    .view-more {
      font-size: 13px;
    }
  }

  .hot-search-list {
    .hot-search-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 0;
      border-bottom: 1px solid #f0f2f5;
      cursor: pointer;
      transition: all 0.2s ease;

      &:last-child {
        border-bottom: none;
      }

      &:hover {
        background: #f5f7fa;
        margin: 0 -20px;
        padding-left: 20px;
        padding-right: 20px;
      }

      .rank {
        width: 22px;
        height: 22px;
        border-radius: 4px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        font-weight: 600;
        background: #f5f7fa;
        color: #606266;
        flex-shrink: 0;

        &.rank-1 {
          background: linear-gradient(135deg, #ffd700, #ffed4e);
          color: #8b6914;
        }

        &.rank-2 {
          background: linear-gradient(135deg, #c0c0c0, #e8e8e8);
          color: #666;
        }

        &.rank-3 {
          background: linear-gradient(135deg, #cd7f32, #daa520);
          color: white;
        }
      }

      .title {
        flex: 1;
        font-size: 14px;
        color: #303133;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        line-height: 1.4;
      }

      .heat {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        color: #f56c6c;
        flex-shrink: 0;

        .el-icon {
          font-size: 14px;
        }
      }
    }
  }

  .empty-state {
    padding: 20px 0;
  }
}
</style>
