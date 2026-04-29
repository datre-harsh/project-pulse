import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
})

const AUTH_KEY = 'projectPulseUser'

export const getCurrentUser = () => {
  const raw = localStorage.getItem(AUTH_KEY)
  if (!raw) return null

  try {
    return JSON.parse(raw)
  } catch {
    localStorage.removeItem(AUTH_KEY)
    return null
  }
}

export const setCurrentUser = (user) => {
  localStorage.setItem(AUTH_KEY, JSON.stringify(user))
}

export const clearCurrentUser = () => {
  localStorage.removeItem(AUTH_KEY)
}

api.interceptors.request.use((config) => {
  const user = getCurrentUser()
  if (user?.id) {
    config.headers['X-User-Id'] = user.id
  }
  return config
})

export default api
