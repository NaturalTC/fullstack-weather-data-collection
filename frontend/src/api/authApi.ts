const API_BASE = import.meta.env.VITE_API_BASE ?? '';

export interface AuthResponse {
  token: string;
  email: string;
  name: string;
}

export async function register(name: string, email: string, password: string): Promise<string> {
  const res = await fetch(`${API_BASE}/api/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, email, password }),
  });
  const text = await res.text();
  if (!res.ok) throw new Error(text || 'Registration failed');
  return text;
}

export async function login(email: string, password: string): Promise<AuthResponse> {
  const res = await fetch(`${API_BASE}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || 'Login failed');
  }
  return res.json();
}

export function saveAuth(auth: AuthResponse) {
  localStorage.setItem('nc_token', auth.token);
  localStorage.setItem('nc_email', auth.email);
  localStorage.setItem('nc_name', auth.name);
}

export function getToken(): string | null {
  return localStorage.getItem('nc_token');
}

export function getUser(): { email: string; name: string } | null {
  const email = localStorage.getItem('nc_email');
  const name  = localStorage.getItem('nc_name');
  if (!email || !name) return null;
  return { email, name };
}

export function logout() {
  localStorage.removeItem('nc_token');
  localStorage.removeItem('nc_email');
  localStorage.removeItem('nc_name');
}

// ── API Key management ────────────────────────────────────────────────────────

export interface ApiKeyDTO {
  id: number;
  keyValue: string;
  name: string;
  active: boolean;
  createdAt: string;
}

function authHeaders(): HeadersInit {
  const token = getToken();
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
}

export async function generateApiKey(name: string): Promise<ApiKeyDTO> {
  const res = await fetch(`${API_BASE}/api/keys/generate`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ name }),
  });
  if (!res.ok) throw new Error('Failed to generate key');
  return res.json();
}

export async function listApiKeys(): Promise<ApiKeyDTO[]> {
  const res = await fetch(`${API_BASE}/api/keys`, { headers: authHeaders() });
  if (!res.ok) throw new Error('Failed to fetch keys');
  return res.json();
}

export async function revokeApiKey(id: number): Promise<void> {
  const res = await fetch(`${API_BASE}/api/keys/${id}`, {
    method: 'DELETE',
    headers: authHeaders(),
  });
  if (!res.ok) throw new Error('Failed to revoke key');
}

// ── Password reset ────────────────────────────────────────────────────────────

export async function forgotPassword(email: string): Promise<string> {
  const res = await fetch(`${API_BASE}/api/auth/forgot-password`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email }),
  });
  const data = await res.json();
  if (!res.ok) throw new Error(data.error ?? 'Request failed');
  return data.message;
}

export async function resetPassword(token: string, newPassword: string): Promise<string> {
  const res = await fetch(`${API_BASE}/api/auth/reset-password`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ token, newPassword }),
  });
  const data = await res.json();
  if (!res.ok) throw new Error(data.error ?? 'Reset failed');
  return data.message;
}

// ── User profile ──────────────────────────────────────────────────────────────

export interface UserProfileDTO {
  name: string;
  email: string;
  role: string;
  plan: string;
  createdAt: string;
}

export interface UpdateProfileRequest {
  name?: string;
  currentPassword?: string;
  newPassword?: string;
}

export async function getProfile(): Promise<UserProfileDTO> {
  const res = await fetch(`${API_BASE}/api/user/profile`, { headers: authHeaders() });
  if (!res.ok) throw new Error('Failed to fetch profile');
  return res.json();
}

export async function updateProfile(req: UpdateProfileRequest): Promise<UserProfileDTO> {
  const res = await fetch(`${API_BASE}/api/user/profile`, {
    method: 'PATCH',
    headers: authHeaders(),
    body: JSON.stringify(req),
  });
  const data = await res.json();
  if (!res.ok) throw new Error(data.error ?? 'Update failed');
  return data;
}

// ── Stripe billing ────────────────────────────────────────────────────────────

export async function createCheckoutSession(plan: 'PRO' | 'SCALE'): Promise<string> {
  const res = await fetch(`${API_BASE}/api/stripe/create-checkout-session`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ plan }),
  });
  const data = await res.json();
  if (!res.ok) throw new Error(data.error ?? 'Checkout failed');
  return data.url;
}
