<template>
  <div class="home-view">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    
    <div v-else class="platforms-grid">
      <el-row :gutter="20">
        <el-col 
          v-for="platform in platforms" 
          :key="platform"
          :xs="24" 
          :sm="12" 
          :md="12" 
          :lg="8"
          class="platform-col"
        >
          <PlatformCard 
            :platform="platform"
            :hot-searches="hotSearches[platform] || []"
          />
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useHotSearchStore } from '../stores/hotSearch'
import PlatformCard from '../components/PlatformCard.vue'

const store = useHotSearchStore()

const platforms = computed(() => store.allPlatforms)
const hotSearches = computed(() => store.allHotSearches)
const loading = computed(() => store.isLoading)

onMounted(async () => {
  await store.fetchPlatforms()
  await store.fetchAllHotSearches(20)
})
</script>

<style scoped lang="scss">
.home-view {
  .loading-container {
    background: white;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  }

  .platforms-grid {
    .platform-col {
      margin-bottom: 20px;
    }
  }
}
</style>
