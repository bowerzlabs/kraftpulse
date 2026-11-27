const API_BASE = '/kraft/pulse/api'

export async function apiFetch<T>(
    path: string,
    options?: RequestInit
): Promise<T> {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  const response = await fetch(`${API_BASE}${normalizedPath}`, {
    ...options,
    headers: {
      'Accept': 'application/json',
      ...options?.headers
    }
  })

  if (!response.ok) {
    throw new Error(
        `KraftPulse API request failed: ${response.status} ${response.statusText}`
    )
  }

  return response.json() as Promise<T>
}