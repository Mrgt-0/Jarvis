import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Auth from './components/auth/Auth';
import Dashboard from './components/dashboard/Dashboard';
import Analyzer from './components/analyzer/Analyzer';
import History from './components/history/History';

const PrivateRoute = ({ children }) => {
  const user = localStorage.getItem('user');
  return user ? children : <Navigate to="/login" />;
};

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Navigate to="/login" />} />
        <Route path="/login" element={<Auth />} />
        <Route path="/register" element={<Auth />} />
        <Route 
          path="/dashboard" 
          element={
            <PrivateRoute>
              <Dashboard />
            </PrivateRoute>
          } 
        />
        <Route 
          path="/analyzer" 
          element={
            <PrivateRoute>
              <Analyzer />
            </PrivateRoute>
          } 
        />
        <Route
        path="/history"
        element={
          <PrivateRoute>
            <History />
          </PrivateRoute>
          }
        />
        <Route path="*" element={<Navigate to="/login" />} />
      </Routes>
    </Router>
  );
}

export default App;