<template>
  <div class="platform-view">
    <el-page-header @back="goBack" :title="platformName">
      <template #content>
        <span class="platform-title">{{ platformName }}</span>
      </template>
    </el-page-header>

    <div class="hot-search-list" v-loading="loading">
      <el-card 
        v-for="(item, index) in hotSearches" 
        :key="item.id"
        class="hot-search-item"
        :class="{ 'top-three': index < 3 }"
        shadow="hover"
      >
        <div class="item-content">
          <div class="rank" :class="`rank-${index + 1}`">
            {{ index + 1 }}
          </div>
          <div class="info">
            <h3 class="title">
              <a :href="item.url" target="_blank" rel="noopener noreferrer">
                {{ item.title }}
              </a>
            </h3>
            <p v-if="item.description" class="description">
              {{ item.description }}
            </p>
          </div>
          <div class="meta">
            <el-tag v-if="item.category" size="small" type="info">
              {{ item.category }}
            </el-tag>
            <span v-if="item.heatValue" class="heat-value">
              <el-icon><Fire /></el-icon>
              {{ formatHeatValue(item.heatValue) }}
            </span>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Fire } from '@element-plus/icons-vue'
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
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);

  .platform-title {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
  }

  .hot-search-list {
    margin-top: 24px;

    .hot-search-item {
      margin-bottom: 12px;
      border-radius: 8px;
      transition: all 0.3s ease;

      &:hover {
        transform: translateX(4px);
      }

      &.top-three {
        border-left: 4px solid #f56c6c;
      }

      .item-content {
        display: flex;
        align-items: center;
        gap: 16px;

        .rank {
          width: 32px;
          height: 32px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: 600;
          font-size: 14px;
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

        .info {
          flex: 1;
          min-width: 0;

          .title {
            margin: 0;
            font-size: 15px;
            line-height: 1.5;

            a {
              color: #303133;
              text-decoration: none;

              &:hover {
                color: #409eff;
              }
            }
          }

          .description {
            margin: 4px 0 0;
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
            color: #f56c6c;
            font-size: 13px;
            font-weight: 500;
          }
        }
      }
    }
  }
}
</style>
