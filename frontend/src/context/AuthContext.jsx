import { createContext, useCallback, useContext, useEffect, useMemo, useReducer, useRef } from 'react'
import api, { bindAuthHandlers, clearRequestCache, messageFromError, resetUnauthorizedHandling } from '../services/api'

const AuthContext = createContext(null)
const TOKEN_KEY = 'campuscare_token'
const ALLOWED_ROLES = new Set(['ROLE_STUDENT', 'ROLE_STAFF', 'ROLE_ADMIN'])

const initialState = {
  token: null,
  user: null,
  role: null,
  isAuthenticated: false,
  loading: true,
  initialized: false,
  complaintRevision: 0,
  notice: '',
}

function authReducer(state, action) {
  switch (action.type) {
    case 'VERIFY_START':
      return { ...state, token: action.token, user: null, role: null, isAuthenticated: false, loading: true, notice: '' }
    case 'LOGIN_START':
      return { ...state, token: null, user: null, role: null, isAuthenticated: false, loading: true, initialized: true, complaintRevision: 0, notice: '' }
    case 'TOKEN_STORED':
      return { ...state, token: action.token, user: null, role: null, isAuthenticated: false, loading: true, initialized: true, notice: '' }
    case 'AUTHENTICATED':
      return { ...state, token: action.token, user: action.user, role: action.role, isAuthenticated: true, loading: false, initialized: true, complaintRevision: state.complaintRevision + 1, notice: '' }
    case 'ANONYMOUS':
      return { ...initialState, token: null, user: null, role: null, isAuthenticated: false, loading: false, initialized: true, notice: action.notice || '' }
    case 'AUTH_UNAVAILABLE':
      return { ...state, token: action.token, user: null, role: null, isAuthenticated: false, loading: false, initialized: true, notice: action.notice || '' }
    case 'CLEAR_NOTICE':
      return { ...state, notice: '' }
    case 'COMPLAINTS_INVALIDATED':
      return { ...state, complaintRevision: state.complaintRevision + 1 }
    default:
      return state
  }
}

function readStoredToken() {
  try {
    const token = window.localStorage.getItem(TOKEN_KEY)
    if (!token) return null
    if (!token.trim() || token === 'undefined' || token === 'null') {
      removeStoredToken()
      return null
    }
    return token.trim()
  } catch {
    return null
  }
}

function writeStoredToken(token) {
  window.localStorage.setItem(TOKEN_KEY, token)
}

function removeStoredToken() {
  try { window.localStorage.removeItem(TOKEN_KEY) } catch { /* storage may be unavailable */ }
}

function roleForProfile(profile) {
  const role = profile?.role
  return ALLOWED_ROLES.has(role) ? role : null
}

export function dashboardPathForRole(role) {
  if (role === 'ROLE_ADMIN') return '/admin/dashboard'
  if (role === 'ROLE_STAFF') return '/staff/dashboard'
  if (role === 'ROLE_STUDENT') return '/student/dashboard'
  return '/login'
}

export function AuthProvider({ children }) {
  const [state, dispatch] = useReducer(authReducer, initialState)
  const tokenRef = useRef(null)
  const authAttemptRef = useRef(0)
  const initializedRef = useRef(false)

  const clearSession = useCallback((notice = '') => {
    authAttemptRef.current += 1
    tokenRef.current = null
    removeStoredToken()
    clearRequestCache()
    dispatch({ type: 'ANONYMOUS', notice })
  }, [])

  const verifyToken = useCallback(async (token) => {
    const attempt = ++authAttemptRef.current
    tokenRef.current = token
    resetUnauthorizedHandling()
    clearRequestCache()
    dispatch({ type: 'VERIFY_START', token })
    try {
      const { data: profile } = await api.get('/auth/me')
      if (attempt !== authAttemptRef.current || tokenRef.current !== token) return false
      const role = roleForProfile(profile)
      if (!role) {
        clearSession('The account role could not be verified. Please sign in again.')
        return false
      }
      dispatch({ type: 'AUTHENTICATED', token, user: profile, role })
      return true
    } catch (error) {
      if (attempt !== authAttemptRef.current || tokenRef.current !== token) return false
      if (error?.response?.status === 401) return false // The centralized 401 handler already cleared the session.
      dispatch({ type: 'AUTH_UNAVAILABLE', token, notice: messageFromError(error) })
      return false
    }
  }, [clearSession])

  const signIn = useCallback(async (credentials) => {
    let activeAttempt = ++authAttemptRef.current
    tokenRef.current = null
    removeStoredToken()
    clearRequestCache()
    dispatch({ type: 'LOGIN_START' })

    try {
      const { data } = await api.post('/auth/login', credentials)
      if (activeAttempt !== authAttemptRef.current) throw new Error('Your sign-in session changed. Please try again.')
      if (data?.otpRequired) {
        dispatch({ type: 'ANONYMOUS' })
        return { otpRequired: true, challengeId: data.otpChallengeId, maskedEmail: data.maskedEmail,
          purpose: data.role === 'ROLE_ADMIN' ? 'ADMIN_LOGIN' : 'STAFF_LOGIN' }
      }
      const token = typeof data?.token === 'string' ? data.token.trim() : ''
      if (!token) throw new Error('The sign-in response did not include a valid session token.')
      writeStoredToken(token)
      tokenRef.current = token
      resetUnauthorizedHandling()
      activeAttempt = ++authAttemptRef.current
      dispatch({ type: 'TOKEN_STORED', token })
      const { data: profile } = await api.get('/auth/me')
      if (activeAttempt !== authAttemptRef.current || tokenRef.current !== token) throw new Error('Your sign-in session changed. Please try again.')
      const role = roleForProfile(profile)
      if (!role) {
        clearSession('The account role could not be verified. Please contact your college administrator.')
        throw new Error('The account role could not be verified. Please contact your college administrator.')
      }
      dispatch({ type: 'AUTHENTICATED', token, user: profile, role })
      return { destination: dashboardPathForRole(role) }
    } catch (error) {
      if (activeAttempt === authAttemptRef.current) {
        if (tokenRef.current) dispatch({ type: 'AUTH_UNAVAILABLE', token: tokenRef.current, notice: messageFromError(error) })
        else dispatch({ type: 'ANONYMOUS' })
      }
      throw error
    }
  }, [clearSession])

  const completeOtpSignIn = useCallback(async ({ challengeId, otp, purpose }) => {
    let activeAttempt = ++authAttemptRef.current
    dispatch({ type: 'LOGIN_START' })
    try {
      const { data } = await api.post('/auth/login/verify-otp', { challengeId, otp, purpose })
      if (activeAttempt !== authAttemptRef.current) throw new Error('Your sign-in session changed. Please try again.')
      const token = typeof data?.token === 'string' ? data.token.trim() : ''
      if (!token) throw new Error('The sign-in response did not include a valid session token.')
      writeStoredToken(token); tokenRef.current = token; resetUnauthorizedHandling(); clearRequestCache()
      activeAttempt = ++authAttemptRef.current
      dispatch({ type: 'TOKEN_STORED', token })
      const { data: profile } = await api.get('/auth/me')
      if (activeAttempt !== authAttemptRef.current || tokenRef.current !== token) throw new Error('Your sign-in session changed. Please try again.')
      const role = roleForProfile(profile)
      if (!role) throw new Error('The account role could not be verified. Please contact your college administrator.')
      dispatch({ type: 'AUTHENTICATED', token, user: profile, role })
      return dashboardPathForRole(role)
    } catch (error) {
      if (activeAttempt === authAttemptRef.current) dispatch({ type: 'ANONYMOUS' })
      throw error
    }
  }, [])

  const signOut = useCallback(() => clearSession(), [clearSession])
  const clearNotice = useCallback(() => dispatch({ type: 'CLEAR_NOTICE' }), [])
  const invalidateComplaints = useCallback(() => {
    clearRequestCache()
    dispatch({ type: 'COMPLAINTS_INVALIDATED' })
  }, [])

  useEffect(() => bindAuthHandlers({
    getToken: () => tokenRef.current,
    onUnauthorized: (error) => clearSession(messageFromError(error)),
  }), [clearSession])

  useEffect(() => {
    if (initializedRef.current) return
    initializedRef.current = true
    const token = readStoredToken()
    if (!token) {
      tokenRef.current = null
      dispatch({ type: 'ANONYMOUS' })
      return
    }
    void verifyToken(token)
  }, [verifyToken])

  useEffect(() => {
    function syncStorage(event) {
      if (event.key !== TOKEN_KEY) return
      const nextToken = event.newValue?.trim()
      if (!nextToken || nextToken === 'undefined' || nextToken === 'null') {
        clearSession()
        return
      }
      if (nextToken !== tokenRef.current) void verifyToken(nextToken)
    }
    window.addEventListener('storage', syncStorage)
    return () => window.removeEventListener('storage', syncStorage)
  }, [clearSession, verifyToken])

  const value = useMemo(() => ({
    ...state,
    signIn,
    completeOtpSignIn,
    signOut,
    clearNotice,
    invalidateComplaints,
  }), [state, signIn, completeOtpSignIn, signOut, clearNotice, invalidateComplaints])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const value = useContext(AuthContext)
  if (!value) throw new Error('useAuth must be used within AuthProvider')
  return value
}
