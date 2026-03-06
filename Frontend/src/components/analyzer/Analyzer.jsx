import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './Analyzer.css';

const Analysis = () => {
  const navigate = useNavigate();
  const [file, setFile] = useState(null);
  const [projectName, setProjectName] = useState('default');
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [dragActive, setDragActive] = useState(false);
  const [loading, setLoading] = useState(false);
  const [analysisType, setAnalysisType] = useState('file'); // 'file' или 'archive'

  const handleGoToDashboard = () => {
    navigate("/dashboard");
  };

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];
    validateAndSetFile(selectedFile);
  };

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") {
      setDragActive(true);
    } else if (e.type === "dragleave") {
      setDragActive(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    
    const droppedFile = e.dataTransfer.files[0];
    validateAndSetFile(droppedFile);
  };

  const validateAndSetFile = (selectedFile) => {
    if (!selectedFile) return;

    const fileName = selectedFile.name.toLowerCase();
    
    // Для файлового анализа - только .java
    if (analysisType === 'file') {
      if (!fileName.endsWith('.java')) {
        setError('Пожалуйста, выберите файл с расширением .java');
        setFile(null);
        return;
      }
      
      // Проверяем размер файла (10MB для .java)
      if (selectedFile.size > 10 * 1024 * 1024) {
        setError('Файл слишком большой. Максимальный размер - 10MB');
        setFile(null);
        return;
      }
    } 
    // Для анализа архива - только .zip
    else {
      if (!fileName.endsWith('.zip')) {
        setError('Пожалуйста, выберите ZIP архив');
        setFile(null);
        return;
      }
      
      // Проверяем размер архива (50MB для .zip)
      if (selectedFile.size > 50 * 1024 * 1024) {
        setError('Архив слишком большой. Максимальный размер - 50MB');
        setFile(null);
        return;
      }
    }

    setFile(selectedFile);
    setError('');
    setResult(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!file) {
      setError('Пожалуйста, выберите файл');
      return;
    }

    setLoading(true);
    setError('');
    setResult(null);

    const formData = new FormData();
    formData.append('file', file);
    formData.append('projectName', projectName);

    try {
      console.log('Отправка файла:', file.name);
      console.log('Project name:', projectName);
      console.log('Тип анализа:', analysisType);

      // Выбираем правильный endpoint в зависимости от типа файла
      const endpoint = analysisType === 'file' 
        ? 'http://localhost:8080/api/analysis/file'
        : 'http://localhost:8080/api/analysis/archive';

      const response = await fetch(endpoint, {
        method: 'POST',
        body: formData,
        credentials: 'include'
      });

      console.log('Статус ответа:', response.status);

      // Обрабатываем разные типы ответов
      const responseData = await response.json();
      console.log('Данные ответа:', responseData);

      if (!response.ok) {
        // Для архива может прийти массив с ошибками
        if (Array.isArray(responseData) && responseData.length > 0) {
          throw new Error(responseData[0]?.error || 'Ошибка при анализе');
        }
        // Для обычных ошибок
        throw new Error(responseData?.error || responseData?.message || `Ошибка ${response.status}`);
      }

      setResult(responseData);

    } catch (err) {
      console.error('Ошибка при загрузке:', err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const removeFile = () => {
    setFile(null);
    setResult(null);
  };

  // Функция для отображения результата (может быть объектом или массивом)
  const renderResult = () => {
    if (!result) return null;

    if (Array.isArray(result)) {
      return (
        <div className="archive-results">
          <h3>Результаты анализа архива</h3>
          {result.map((item, index) => (
            <div key={index} className="result-item">
              <h4>{item.filename || `Файл ${index + 1}`}</h4>
              {item.error ? (
                <div className="error-message">Ошибка: {item.error}</div>
              ) : (
                <pre className="result-content">
                  {JSON.stringify(item, null, 2)}
                </pre>
              )}
            </div>
          ))}
        </div>
      );
    } else {
      return (
        <div className="single-result">
          <h3>Результат анализа</h3>
          <pre className="result-content">
            {JSON.stringify(result, null, 2)}
          </pre>
        </div>
      );
    }
  };

  return (
    <div className="file-analyzer-container">
      <div className="analyzer-card">
        <h2>Анализ Java кода</h2>
        <p className="description">Загрузите .java файл или ZIP архив для анализа</p>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="projectName">Название проекта (необязательно):</label>
            <input
              type="text"
              id="projectName"
              value={projectName}
              onChange={(e) => setProjectName(e.target.value)}
              placeholder="Введите название проекта"
              disabled={loading}
              className="project-input"
            />
          </div>

          {/* Переключатель типа анализа */}
          <div className="analysis-type-switcher">
            <button
              type="button"
              className={`type-btn ${analysisType === 'file' ? 'active' : ''}`}
              onClick={() => {
                setAnalysisType('file');
                setFile(null);
                setError('');
                setResult(null);
              }}
              disabled={loading}
            >
              Файл .java
            </button>
            <button
              type="button"
              className={`type-btn ${analysisType === 'archive' ? 'active' : ''}`}
              onClick={() => {
                setAnalysisType('archive');
                setFile(null);
                setError('');
                setResult(null);
              }}
              disabled={loading}
            >
              ZIP архив
            </button>
          </div>

          <div 
            className={`file-upload-area ${dragActive ? 'drag-active' : ''} ${file ? 'file-selected' : ''}`}
            onDragEnter={handleDrag}
            onDragLeave={handleDrag}
            onDragOver={handleDrag}
            onDrop={handleDrop}
          >
            <input
              type="file"
              id="fileInput"
              onChange={handleFileChange}
              accept={analysisType === 'file' ? ".java" : ".zip"}
              disabled={loading}
              className="file-input"
            />
            
            {!file ? (
              <label htmlFor="fileInput" className="file-label">
                <div className="upload-text">
                  <span className="primary-text">
                    {analysisType === 'file' ? 'Нажмите для выбора .java файла' : 'Нажмите для выбора ZIP архива'}
                  </span>
                  <span className="secondary-text">или перетащите его сюда</span>
                </div>
                <div className="file-info">
                  {analysisType === 'file' 
                    ? 'Поддерживаются только .java файлы до 10MB'
                    : 'Поддерживаются только ZIP архивы до 50MB'}
                </div>
              </label>
            ) : (
              <div className="selected-file">
                <div className="file-details">
                  <div className="file-name">
                    {analysisType === 'file' ? '☕' : '📦'} {file.name}
                  </div>
                  <div className="file-size">{(file.size / 1024).toFixed(2)} KB</div>
                </div>
                <button 
                  type="button" 
                  onClick={removeFile}
                  className="remove-file-btn"
                  disabled={loading}
                >
                  Сбросить
                </button>
              </div>
            )}
          </div>

          {error && (
            <div className="error-message">
              <span className="error-icon">⚠️</span>
              {error}
            </div>
          )}

          <button 
            type="submit" 
            className="submit-btn"
            disabled={loading || !file}
          >
            {loading ? (
              <>
                <span className="spinner"></span>
                Анализ...
              </>
            ) : (
              analysisType === 'file' ? 'Анализировать файл' : 'Анализировать архив'
            )}
          </button>
        </form>

        {result && renderResult()}
        
        <div>
          <button
            type="button"
            onClick={handleGoToDashboard}
            className="submit-btn"
            disabled={loading}
          >
            Вернуться в профиль
          </button>
        </div>
      </div>
    </div>
  );
};

export default Analysis;