import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

import Login from './login';
import Dashboard from './Dashboard';
import Admin from './Admin';
import api from './api';

function ProtectedRoute({ children, adminOnly = false }) {
  const [state, setState] = useState(() => ({
    loading: Boolean(localStorage.getItem('jwt')),
    user: null,
  }));
  const location = useLocation();

  useEffect(() => {
    if (!localStorage.getItem('jwt')) return;
    api.get('/auth/me')
      .then(response => setState({ loading: false, user: response.data }))
      .catch(() => setState({ loading: false, user: null }));
  }, []);

  if (state.loading) return <div className="min-h-screen grid place-items-center">Loading...</div>;
  if (!state.user) return <Navigate to="/" replace state={{ from: location }} />;
  if (adminOnly && state.user.role !== 'ADMIN') return <Navigate to="/dashboard" replace />;
  return children;
}

function App() {
  return (
    <BrowserRouter>
      <ToastContainer position="top-right" autoClose={3000} hideProgressBar={false} />
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
        <Route path="/admin" element={<ProtectedRoute adminOnly><Admin /></ProtectedRoute>} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;