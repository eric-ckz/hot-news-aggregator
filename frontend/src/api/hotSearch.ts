import axios from 'axios'
import type { HotSearch, ApiResponse } from '../types/hotSearch'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

export const hotSearchApi = {
  // 获取所有平台
  getPlatforms(): Promise<ApiResponse<string[]>> {
    return api.get('/hot-search/platforms')
  },

  // 获取指定平台的热搜
  getHotSearchesByPlatform(platform: string, limit: number = 50): Promise<ApiResponse<HotSearch[]>> {
    return api.get(`/hot-search/${platform}`, {
      params: { limit },
    })
  },

  // 获取所有平台的热搜
  getAllHotSearches(limit: number = 50): Promise<ApiResponse<Record<string, HotSearch[]>>> {
    return api.get('/hot-search/all', {
      params: { limit },
    })
  },

  // 刷新所有热搜
  refreshHotSearches(): Promise<ApiResponse<string>> {
    return api.get('/hot-search/refresh')
  },

  // 刷新指定平台的热搜
  refreshPlatformHotSearches(platform: string): Promise<ApiResponse<string>> {
    return api.get(`/hot-search/refresh/${platform}`)
  },
}
