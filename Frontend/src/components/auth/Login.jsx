import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const Login = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    username: '',
    password: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
  e.preventDefault();
  setError('');
  setLoading(true);

  try {
    console.log('1. Отправка POST запроса на логин...');
    
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
      body: JSON.stringify(formData),
      redirect: 'manual' // ВАЖНО: предотвращаем автоматическое следование редиректам
    });

    console.log('2. Статус ответа:', response.status);
    console.log('3. Тип ответа:', response.type);

    // Проверяем, не является ли ответ редиректом
    if (response.type === 'opaqueredirect' || response.status === 302 || response.status === 301) {
      console.log('4. Получен редирект, но мы его обработаем сами');
      // Не следуем редиректу
      throw new Error('Редирект получен, но мы его игнорируем');
    }

    const data = await response.json();
    console.log('5. Данные ответа:', data);

    if (!response.ok) {
      throw new Error(data.message || 'Ошибка при входе');
    }

    // Сохраняем данные
    localStorage.setItem('user', JSON.stringify(data.user));
    localStorage.setItem('token', data.token || 'authenticated');
    
    console.log('6. Данные сохранены, перенаправляем на dashboard');
    navigate('/dashboard', { replace: true });
    
  } catch (err) {
    console.error('7. Ошибка:', err);
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
        <label htmlFor="password">Пароль</label>
        <input
          type="password"
          id="password"
          name="password"
          value={formData.password}
          onChange={handleChange}
          required
          placeholder="Введите пароль"
          disabled={loading}
        />
      </div>

      <button 
        type="submit" 
        className="submit-btn"
        disabled={loading}
      >
        {loading ? 'Вход...' : 'Войти'}
      </button>
    </form>
  );
};

export default Login;