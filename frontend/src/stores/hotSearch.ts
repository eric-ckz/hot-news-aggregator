import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { hotSearchApi } from '../api/hotSearch'
import type { HotSearch } from '../types/hotSearch'

export const useHotSearchStore = defineStore('hotSearch', () => {
  // State
  const platforms = ref<string[]>([])
  const hotSearches = ref<Record<string, HotSearch[]>>({})
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const allPlatforms = computed(() => platforms.value)
  const allHotSearches = computed(() => hotSearches.value)
  const isLoading = computed(() => loading.value)

  // Actions
  const fetchPlatforms = async () => {
    try {
      const response = await hotSearchApi.getPlatforms()
      if (response.data.code === 200) {
        platforms.value = response.data.data
      }
    } catch (err) {
      error.value = '获取平台列表失败'
      console.error(err)
    }
  }

  const fetchAllHotSearches = async (limit: number = 50) => {
    loading.value = true
    try {
      const response = await hotSearchApi.getAllHotSearches(limit)
      if (response.data.code === 200) {
        hotSearches.value = response.data.data
      }
    } catch (err) {
      error.value = '获取热搜数据失败'
      console.error(err)
    } finally {
      loading.value = false
    }
  }

  const fetchHotSearchesByPlatform = async (platform: string, limit: number = 50) => {
    try {
      const response = await hotSearchApi.getHotSearchesByPlatform(platform, limit)
      if (response.data.code === 200) {
        hotSearches.value[platform] = response.data.data
      }
    } catch (err) {
      error.value = `获取${platform}热搜失败`
      console.error(err)
    }
  }

  const refreshAllHotSearches = async () => {
    try {
      await hotSearchApi.refreshHotSearches()
      await fetchAllHotSearches()
    } catch (err) {
      error.value = '刷新数据失败'
      console.error(err)
      throw err
    }
  }

  return {
    platforms,
    hotSearches,
    loading,
    error,
    allPlatforms,
    allHotSearches,
    isLoading,
    fetchPlatforms,
    fetchAllHotSearches,
    fetchHotSearchesByPlatform,
    refreshAllHotSearches,
  }
})
