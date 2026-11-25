import { useEffect, type ReactNode } from 'react';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router';
import { RequireAuth } from './auth';
import { HOME } from './safeRedirect';
import LoginPage from './pages/LoginPage';
import InvoiceListPage from './pages/InvoiceListPage';

// Rutele aplicației. Ecranele de după login stau în RequireAuth (DS-p21-app-shell).
export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/facturi" element={<RequireAuth><Titled title="Facturi"><InvoiceListPage /></Titled></RequireAuth>} />
        <Route path="*" element={<Navigate to={HOME} replace />} />
      </Routes>
    </BrowserRouter>
  );
}

function Titled({ title, children }: { title: string; children: ReactNode }) {
  useEffect(() => {
    document.title = `${title} · Facturare`;
  }, [title]);
  return children;
}
