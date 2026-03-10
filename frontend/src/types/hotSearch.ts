export interface HotSearch {
  id: number
  platform: string
  title: string
  url: string
  heatValue: number
  category: string
  rankNum: number
  iconUrl: string
  description: string
  createdTime: string
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: string
}

export interface PlatformInfo {
  name: string
  icon: string
  color: string
}

export const PLATFORM_CONFIG: Record<string, PlatformInfo> = {
  weibo: {
    name: '微博热搜',
    icon: 'ChatDotRound',
    color: '#e6162d',
  },
  zhihu: {
    name: '知乎热榜',
    icon: 'QuestionFilled',
    color: '#0084ff',
  },
  bilibili: {
    name: 'B站热门',
    icon: 'VideoPlay',
    color: '#00a1d6',
  },
  baidu: {
    name: '百度热搜',
    icon: 'Search',
    color: '#2932e1',
  },
  hupu: {
    name: '虎扑热帖',
    icon: 'Basketball',
    color: '#c01e2f',
  },
}
