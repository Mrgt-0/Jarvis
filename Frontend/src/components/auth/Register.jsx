import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const Register = ({ onSuccess }) => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const validateForm = () => {
    if (formData.password !== formData.confirmPassword) {
      setError('Пароли не совпадают');
      return false;
    }
    if (formData.password.length < 6) {
      setError('Пароль должен содержать минимум 6 символов');
      return false;
    }
    if (!formData.email.includes('@')) {
      setError('Введите корректный email');
      return false;
    }
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!validateForm()) return;

    setLoading(true);

    try {
      const userData = {
        username: formData.username,
        email: formData.email,
        password: formData.password
      };

      console.log('Отправка запроса на регистрацию...', userData);

      const response = await fetch('http://localhost:8080/api/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify(userData)
      });
      if (!response.ok) {
        const data = await response.json();
        throw new Error(data.message || 'Ошибка регистрации');
      }
      const data = await response.json();
      console.log('Ответ от сервера:', data);
      if (onSuccess) 
        onSuccess();  
    } catch (err) {
      console.error('Ошибка регистрации:', err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="auth-form">
      {error && <div className="error-message">{error}</div>}
      
      <div className="form-group">
        <label htmlFor="username">Имя пользователя</label>
        <input
          type="text"
          id="username"
          name="username"
          value={formData.username}
          onChange={handleChange}
          required
          placeholder="Введите имя пользователя"
          disabled={loading}
        />
      </div>

      <div className="form-group">
        <label htmlFor="email">Email</label>
        <input
          type="email"
          id="email"
          name="email"
          value={formData.email}
          onChange={handleChange}
          required
          placeholder="Введите email"
          disabled={loading}
        />
      </div>

      <div className="form-group">
        <label htmlFor="password">Пароль</label>
        <input
          type="password"
          id="password"
          name="password"
          value={formData.password}
          onChange={handleChange}
          required
          placeholder="Создайте пароль (мин. 6 символов)"
          disabled={loading}
        />
      </div>

      <div className="form-group">
        <label htmlFor="confirmPassword">Подтверждение пароля</label>
        <input
          type="password"
          id="confirmPassword"
          name="confirmPassword"
          value={formData.confirmPassword}
          onChange={handleChange}
          required
          placeholder="Повторите пароль"
          disabled={loading}
        />
      </div>

      <button 
        type="submit" 
        className="submit-btn"
        disabled={loading}
      >
        {loading ? 'Регистрация...' : 'Зарегистрироваться'}
      </button>
    </form>
  );
};

export default Register;