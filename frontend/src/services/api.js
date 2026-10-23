import axios from 'axios'

const localHosts = new Set(['localhost', '127.0.0.1', '[::1]'])
const localDevelopment = import.meta.env.DEV || (typeof window !== 'undefined' && localHosts.has(window.location.hostname))
const defaultBaseUrl = localDevelopment ? 'http://localhost:8081/api' : '/api'
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || defaultBaseUrl,
})

let authHandlers = { getToken: () => null, onUnauthorized: () => {} }
let unauthorizedHandled = false
const inFlightGets = new Map()

export function bindAuthHandlers(handlers) {
  const boundHandlers = { ...authHandlers, ...handlers }
  authHandlers = boundHandlers
  return () => {
    if (authHandlers === boundHandlers) authHandlers = { getToken: () => null, onUnauthorized: () => {} }
  }
}

export function resetUnauthorizedHandling() {
  unauthorizedHandled = false
}

export function clearRequestCache() {
  inFlightGets.clear()
}

function requestPath(url = '') {
  try {
    return new URL(url, 'http://campuscare.local').pathname.replace(/\/+$/, '') || '/'
  } catch {
    return String(url).split('?')[0].replace(/\/+$/, '') || '/'
  }
}

function isPublicRequest(method, url) {
  const path = requestPath(url)
  return (method === 'post' && (path === '/auth/login' || path === '/auth/login/verify-otp' || path === '/student-registrations'
    || path === '/otp/send' || path === '/otp/verify'))
    || (method === 'get' && path === '/academic-years')
}

function removeAuthorizationHeader(headers) {
  if (typeof headers?.delete === 'function') headers.delete('Authorization')
  if (headers) {
    delete headers.Authorization
    delete headers.authorization
  }
}

api.interceptors.request.use((config) => {
  const method = (config.method || 'get').toLowerCase()
  const url = config.url || ''
  const baseUrl = new URL(config.baseURL || '', window.location.origin)
  const requestUrl = new URL(url, baseUrl)
  const localLoopbackDevelopment = window.location.protocol === 'http:'
    && requestUrl.protocol === 'http:'
    && localHosts.has(window.location.hostname)
    && localHosts.has(requestUrl.hostname)
  if (requestUrl.protocol !== 'https:' && !localLoopbackDevelopment) {
    const error = new Error('A secure HTTPS connection is required for non-local API requests.')
    error.code = 'ERR_SECURE_TRANSPORT_REQUIRED'
    return Promise.reject(error)
  }

  if (isPublicRequest(method, url)) {
    removeAuthorizationHeader(config.headers)
    config.__campuscarePublic = true
    config.__campuscareAuthenticated = false
    return config
  }

  const token = authHandlers.getToken()
  if (token && token.trim() && token !== 'undefined' && token !== 'null') {
    config.headers = config.headers || {}
    if (typeof config.headers.set === 'function') config.headers.set('Authorization', `Bearer ${token.trim()}`)
    else config.headers.Authorization = `Bearer ${token.trim()}`
    config.__campuscareAuthenticated = true
    config.__campuscareToken = token.trim()
  } else {
    removeAuthorizationHeader(config.headers)
    config.__campuscareAuthenticated = false
  }
  return config
})

api.interceptors.response.use((response) => {
  if (response.config?.method && response.config.method.toLowerCase() !== 'get') clearRequestCache()
  return response
}, (error) => {
  const config = error?.config || {}
  if (error?.response?.status === 401 && config.__campuscareAuthenticated && !config.__campuscarePublic
    && config.__campuscareToken === authHandlers.getToken() && !unauthorizedHandled) {
    unauthorizedHandled = true
    authHandlers.onUnauthorized(error)
  }
  return Promise.reject(error)
})

function stableValue(value) {
  if (Array.isArray(value)) return value.map(stableValue)
  if (value && typeof value === 'object') {
    return Object.keys(value).sort().reduce((result, key) => {
      result[key] = stableValue(value[key])
      return result
    }, {})
  }
  return value
}

export function getShared(url, config = {}) {
  const token = authHandlers.getToken() || ''
  const key = JSON.stringify({ url, params: stableValue(config.params || {}), responseType: config.responseType || '', token })
  const existing = inFlightGets.get(key)
  if (existing) return existing

  const request = api.get(url, config).finally(() => {
    if (inFlightGets.get(key) === request) inFlightGets.delete(key)
  })
  inFlightGets.set(key, request)
  return request
}

function backendMessage(error) {
  const data = error?.response?.data
  if (typeof data === 'string' && data.trim() && !data.trim().startsWith('<')) return data.trim()
  if (typeof data?.message === 'string' && data.message.trim()) return data.message.trim()
  if (typeof data?.detail === 'string' && data.detail.trim()) return data.detail.trim()
  if (typeof data?.title === 'string' && data.title.trim()) return data.title.trim()
  if (Array.isArray(data?.errors)) {
    const messages = data.errors.map(item => typeof item === 'string' ? item : item?.defaultMessage || item?.message).filter(Boolean)
    if (messages.length) return [...new Set(messages)].join(' ')
  }
  if (data?.fieldErrors && typeof data.fieldErrors === 'object') {
    const messages = Object.values(data.fieldErrors).flat().filter(value => typeof value === 'string')
    if (messages.length) return [...new Set(messages)].join(' ')
  }
  if (typeof data?.error === 'string' && data.error.trim()) return data.error.trim()
  return ''
}

export function messageFromError(error) {
  if (error?.code === 'ERR_SECURE_TRANSPORT_REQUIRED') return error.message
  const response = error?.response
  const actualBackendMessage = backendMessage(error)
  if (actualBackendMessage) return actualBackendMessage
  if (typeof error?.message === 'string' && !response) return error.message
  if (response?.status === 401) return 'Your session has expired. Please log in again.'
  if (response?.status === 403) return 'You are not authorized to perform this action.'
  if (response?.status === 404) return 'Requested resource was not found.'
  if (response?.status === 409) return 'This request conflicts with existing information.'
  if (response?.status >= 500) return 'Something went wrong on the server.'
  if (!response) return 'Unable to reach the server. Check your connection and try again.'
  return 'The request could not be completed. Please check the submitted information.'
}

export async function getComplaintPage(params = {}) {
  const { data } = await getShared('/complaints', { params })
  return Array.isArray(data) ? data : (data.content || [])
}

export default api
