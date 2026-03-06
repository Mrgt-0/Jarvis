import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Login from './Login';
import Register from './Register';
import './Auth.css';

const Auth = () => {
  const [isLogin, setIsLogin] = useState(true);
  const navigate = useNavigate();

  // Проверяем, не авторизован ли уже пользователь
  useEffect(() => {
    const user = localStorage.getItem('user');
    if (user) {
      navigate('/dashboard');
    }
  }, [navigate]);

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <h2>{isLogin ? 'Вход в аккаунт' : 'Регистрация'}</h2>
        </div>
        
        {isLogin ? <Login /> : <Register />}
        
        <div className="auth-footer">
          <p>
            {isLogin ? 'Нет аккаунта?' : 'Уже есть аккаунт?'}
            <button 
              className="toggle-btn"
              onClick={() => setIsLogin(!isLogin)}
            >
              {isLogin ? 'Зарегистрироваться' : 'Войти'}
            </button>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Auth;