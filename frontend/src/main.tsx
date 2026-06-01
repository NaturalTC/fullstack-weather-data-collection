import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import './index.css'
import App from './App.tsx'
import AdminPage from './components/AdminPage.tsx'
import MetricsDashboard from './components/MetricsDashboard.tsx'
import LandingPage from './components/LandingPage.tsx'
import SignUpPage from './pages/SignUpPage.tsx'
import SignInPage from './pages/SignInPage.tsx'
import DeveloperPage from './pages/DeveloperPage.tsx'
import ForgotPasswordPage from './pages/ForgotPasswordPage.tsx'
import ResetPasswordPage from './pages/ResetPasswordPage.tsx'
import ProfilePage from './pages/ProfilePage.tsx'
import StripeSuccessPage from './pages/StripeSuccessPage.tsx'
import StripeCancelPage from './pages/StripeCancelPage.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/dashboard" element={<App />} />
        <Route path="/admin" element={<AdminPage />} />
        <Route path="/metrics" element={<MetricsDashboard />} />
        <Route path="/signup" element={<SignUpPage />} />
        <Route path="/login" element={<SignInPage />} />
        <Route path="/developer" element={<DeveloperPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/stripe/success" element={<StripeSuccessPage />} />
        <Route path="/stripe/cancel" element={<StripeCancelPage />} />
      </Routes>
    </BrowserRouter>
  </StrictMode>,
)
