import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './Dashboard.css';

const Dashboard = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const userData = localStorage.getItem('user');
    if (userData) {
      setUser(JSON.parse(userData));
    } else {
      navigate('/login');
    }
  }, [navigate]);

  const handleLogout = async () => {
    setLoading(true);
    try {
      await fetch('http://localhost:8080/api/auth/logout', {
        method: 'POST',
        credentials: 'include'
      });
    } catch (error) {
      console.error('Ошибка при выходе:', error);
    } finally {
      localStorage.removeItem('user');
      localStorage.removeItem('token');
      navigate('/login');
      setLoading(false);
    }
  };

  const handleGoToAnalyzer = () => {
    navigate('/analyzer'); 
  };

  if (!user) {
    return <div className="dashboard-container">Загрузка...</div>;
  }

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1>Добро пожаловать, {user.username}!</h1>
        <button 
          onClick={handleLogout} 
          className="logout-btn"
          disabled={loading}
        >
          {loading ? 'Выход...' : 'Выйти'}
        </button>
      </div>
      
      <div className="dashboard-content">
        <div className="user-info-card">
          <h2>Информация о пользователе</h2>
          
          <div className="user-info-details">
            <div className="info-row">
              <span className="info-label">Имя пользователя:</span>
              <span className="info-value">{user.username}</span>
            </div>
            
            <div className="info-row">
              <span className="info-label">Email:</span>
              <span className="info-value">{user.email}</span>
            </div>
          </div>

          <div className="analyzer-section">
            <h3>Анализ Java файлов</h3>
            <p>Загрузите и проанализируйте ваши Java файлы</p>
            <button 
              onClick={handleGoToAnalyzer}
              className="analyzer-nav-btn"
            >
              Перейти в анализатор →
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
export default Dashboard;