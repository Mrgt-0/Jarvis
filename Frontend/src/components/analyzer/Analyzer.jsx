import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './Analyzer.css';

const Analysis = () => {
  const navigate = useNavigate();
  const [file, setFile] = useState(null);
  const [result, setResult] = useState(null);
  const [savedResults, setSavedResults] = useState([]);
  const [expandedSnippets, setExpandedSnippets] = useState({});
  const [error, setError] = useState('');
  const [dragActive, setDragActive] = useState(false);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false); 
  const [analysisType, setAnalysisType] = useState('file');
  const [showSaveDialog, setShowSaveDialog] = useState(false);
  const [projectName, setProjectName] = useState('default');
  const [saveProjectName, setSaveProjectName] = useState('');

  const handleGoToDashboard = () => {
    navigate("/dashboard");
  };

  const handleGoToHistory = () => {
    navigate("/history");
  }

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
    
    if (analysisType === 'file') {
      if (!fileName.endsWith('.java')) {
        setError('Пожалуйста, выберите файл с расширением .java');
        setFile(null);
        return;
      }
      
      if (selectedFile.size > 10 * 1024 * 1024) {
        setError('Файл слишком большой. Максимальный размер - 10MB');
        setFile(null);
        return;
      }
    } else {
      if (!fileName.endsWith('.zip')) {
        setError('Пожалуйста, выберите ZIP архив');
        setFile(null);
        return;
      }
      
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

    try { 
      const endpoint = analysisType === 'file' 
          ? 'http://localhost:8080/api/analysis/file'
          : 'http://localhost:8080/api/analysis/archive';

      const response = await fetch(endpoint, {
        method: 'POST',
        body: formData,
        credentials: 'include'
      });

      console.log('Статус ответа:', response.status);

      const responseData = await response.json();
      console.log('Данные ответа:', responseData);

      if (!response.ok) {
        if (Array.isArray(responseData) && responseData.length > 0) {
          throw new Error(responseData[0]?.error || 'Ошибка при анализе');
        }
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

  const handleSaveResult = async () => {
    if (!result) {
      setError('Нет результатов для сохранения');
      return;
    }
    setSaving(true);
    setError('');

    try {
      const resultsToSave = Array.isArray(result) ? result : [result];
      const resultsWithProject = resultsToSave.map(item => ({
        ...item,
        projectName: saveProjectName || projectName || 'default'
      }));
      const savePromises = resultsWithProject.map(async (item) => {
        const response = await fetch('http://localhost:8080/api/analysis/saveAnalysisResult', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(item),
          credentials: 'include'
        });
        if (!response.ok) {
          let errorMessage = `Ошибка ${response.status}: ${response.statusText}`;
          
          const errorData = await response.json();
          errorMessage = errorData.error || errorData.message || errorMessage;
          throw new Error(errorMessage);
        }
        const text = await response.text();
        if (text) {
          try {
            return JSON.parse(text);
          } catch (e) {
            return { success: true, message: 'Сохранено' };
          }
        }
        return { success: true, message: 'Сохранено' };
      });
      const savedResults = await Promise.all(savePromises);
      console.log('Результаты сохранены:', savedResults);
      setSavedResults(prev => [...prev, ...savedResults]);
      setShowSaveDialog(false);
      alert(`Результаты успешно сохранены в проекте`);
      
    } catch (err) {
      console.error('Ошибка при сохранении:', err);
      setError(`Ошибка сохранения: ${err.message}`);
    } finally {
      setSaving(false);
    }
  };
  const openSaveDialog = () => {
    setSaveProjectName(projectName || 'default');
    setShowSaveDialog(true);
  };

  const handleNewAnalysis = () => {
    setResult(null);
    setFile(null);
    setProjectName('default');
  };

  const removeFile = () => {
    setFile(null);
    setResult(null);
  };
  const extractProblems = (item) => {
    return item.problems || [];
  };
  const extractFileName = (item) => {
    return item.fileName || item.filename || 'Неизвестный файл';
  };
  const extractClassNames = (item) => {
    return item.classNames || [];
  };

  const getResultSummary = (resultData) => {
    if (!resultData) return { total: 0, errors: 0, warnings: 0 };
    
    if (Array.isArray(resultData)) {
      const total = resultData.length;
      const errors = resultData.filter(r => !r.success).length;
      const warnings = resultData.reduce((acc, r) => acc + (r.problems?.length || 0), 0);
      return { total, errors, warnings };
    } else {
      return {
        total: 1,
        errors: resultData.success === false ? 1 : 0,
        warnings: resultData.problems?.length || 0
      };
    }
  };

    const toggleSnippet = (problemId) => {
    setExpandedSnippets(prev => ({
      ...prev,
      [problemId]: !prev[problemId]
    }));
  };

  const renderResult = () => {
    if (!result) return null;

    const summary = getResultSummary(result);

    if (Array.isArray(result)) {
      return (
        <div className="archive-results">
          <div className="results-header">
            <h3>📊 Результаты анализа архива</h3>
            <div className="results-actions">
              <button 
                onClick={handleNewAnalysis}
                className="action-btn secondary"
                disabled={loading || saving}
              >
                Новый анализ
              </button>
              <button 
                onClick={openSaveDialog}
                className="action-btn primary"
                disabled={loading || saving}
              >
                {saving ? '⏳ Сохранение...' : '💾 Сохранить результаты'}
              </button>
            </div>
          </div>

          <div className="results-summary">
            <span className="badge success">
              ✅ Успешно: {result.filter(r => r.success).length}
            </span>
            <span className="badge error">
              ⚠️ С ошибками: {result.filter(r => !r.success).length}
            </span>
            <span className="badge warning">
              🔍 Всего проблем: {summary.warnings}
            </span>
          </div>

          <div className="results-list">
            {result.map((item, index) => {
              const problems = extractProblems(item);
              const classNames = extractClassNames(item);
              const fileName = extractFileName(item);
              const success = item.success !== false;
              
              return (
                <div key={index} className={`result-card ${!success ? 'error' : problems.length > 0 ? 'warning' : 'success'}`}>
                  <div className="result-header">
                    <div className="file-info">
                      <span className="file-icon">
                        {!success ? '❌' : problems.length > 0 ? '⚠️' : '✅'}
                      </span>
                      <span className="filename">{fileName}</span>
                    </div>
                    <span className="file-status">
                      {!success ? 'Ошибка анализа' : 
                       problems.length > 0 ? `${problems.length} проблем` : 'Проблем нет'}
                    </span>
                  </div>
                  
                  {!success && item.errorMessage ? (
                    <div className="error-details">
                      <div className="error-message">
                        <strong>Ошибка:</strong> {item.errorMessage}
                      </div>
                    </div>
                  ) : (
                    <div className="analysis-details">
                      {(item.projectName || item.packageName) && (
                        <div className="project-info">
                          {item.projectName && (
                            <div className="info-item">
                              <span className="info-label">Проект:</span>
                              <span className="info-value">{item.projectName}</span>
                            </div>
                          )}
                          {item.packageName && (
                            <div className="info-item">
                              <span className="info-label">Пакет:</span>
                              <span className="info-value">{item.packageName}</span>
                            </div>
                          )}
                        </div>
                      )}

                      {classNames.length > 0 && (
                        <div className="classes-section">
                          <h4>📚 Классы в файле</h4>
                          <div className="classes-list">
                            {classNames.map((className, i) => (
                              <span key={i} className="class-tag">{className}</span>
                            ))}
                          </div>
                        </div>
                      )}

                      {problems.length > 0 && (
                        <div className="problems-section">
                          <h4>🔍 Найденные проблемы ({problems.length})</h4>
                          <div className="problems-list">
                            {problems.map((problem, i) => {
                              const problemId = `${fileName}-${i}`;
                              const isExpanded = expandedSnippets[problemId];
                              
                              return (
                                <div key={i} className={`problem-item severity-${problem.severity?.toLowerCase() || 'info'}`}>
                                  <div className="problem-header">
                                    <span className="problem-severity">
                                      {problem.severity === 'HIGH' ? '🔴' : 
                                       problem.severity === 'MEDIUM' ? '🟡' : 
                                       problem.severity === 'LOW' ? '🔵' : '⚪'}
                                      {problem.severity || 'INFO'}
                                    </span>
                                    {problem.lineNumber && (
                                      <span className="problem-line">Строка: {problem.lineNumber}</span>
                                    )}
                                  </div>
                                  
                                  <div className="problem-message">
                                    {problem.message || problem.description || 'Неизвестная проблема'}
                                  </div>
                                  
                                  {problem.ruleName && (
                                    <div className="problem-rule">
                                      <span className="rule-label">Правило:</span>
                                      <code>{problem.ruleName}</code>
                                    </div>
                                  )}
                                  {problem.snippet && (
                                    <div className="problem-snippet-container">
                                      <button 
                                        className="snippet-toggle-btn"
                                        onClick={() => toggleSnippet(problemId)}
                                      >
                                        {isExpanded ? '▼ Скрыть код' : '▶ Показать код'}
                                      </button>
                                      
                                      {isExpanded && (
                                        <div className="problem-snippet">
                                          <pre className="code-snippet">
                                            <code>{problem.snippet}</code>
                                          </pre>
                                        </div>
                                      )}
                                    </div>
                                  )}
                                </div>
                              );
                            })}
                          </div>
                        </div>
                      )}

                      {problems.length === 0 && success && (
                        <div className="no-problems">
                          <span className="success-icon">✅</span>
                          <p>Проблем не найдено! Код соответствует стандартам качества.</p>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      );
    } else {
      const problems = extractProblems(result);
      const classNames = extractClassNames(result);
      const fileName = extractFileName(result);
      const success = result.success !== false;
      
      return (
        <div className="single-result">
          <div className="results-header">
            <h3>📊 Результат анализа файла</h3>
            <div className="results-actions">
              <button 
                onClick={handleNewAnalysis}
                className="action-btn secondary"
                disabled={loading || saving}
              >
                Новый анализ
              </button>
              <button 
                onClick={openSaveDialog}
                className="action-btn primary"
                disabled={loading || saving}
              >
                {saving ? '⏳ Сохранение...' : '💾 Сохранить результаты'}
              </button>
            </div>
          </div>

          <div className="results-summary">
            <span className="badge success">
              ✅ Статус: {success ? 'Успешно' : 'Ошибка'}
            </span>
            <span className="badge warning">
              🔍 Проблем: {problems.length}
            </span>
          </div>

          <div className={`result-card ${!success ? 'error' : problems.length > 0 ? 'warning' : 'success'}`}>
            <div className="result-header">
              <div className="file-info">
                <span className="file-icon">
                  {!success ? '❌' : problems.length > 0 ? '⚠️' : '✅'}
                </span>
                <span className="filename">{fileName}</span>
              </div>
            </div>
            
            <div className="analysis-details">
              {(result.projectName || result.packageName) && (
                <div className="project-info">
                  {result.projectName && (
                    <div className="info-item">
                      <span className="info-label">Проект:</span>
                      <span className="info-value">{result.projectName}</span>
                    </div>
                  )}
                  {result.packageName && (
                    <div className="info-item">
                      <span className="info-label">Пакет:</span>
                      <span className="info-value">{result.packageName}</span>
                    </div>
                  )}
                </div>
              )}

              {classNames.length > 0 && (
                <div className="classes-section">
                  <h4>📚 Классы в файле</h4>
                  <div className="classes-list">
                    {classNames.map((className, i) => (
                      <span key={i} className="class-tag">{className}</span>
                    ))}
                  </div>
                </div>
              )}

              {problems.length > 0 && (
                <div className="problems-section">
                  <h4>🔍 Найденные проблемы ({problems.length})</h4>
                  <div className="problems-list">
                    {problems.map((problem, i) => {
                      const problemId = `${fileName}-${i}`;
                      const isExpanded = expandedSnippets[problemId];
                      
                      return (
                        <div key={i} className={`problem-item severity-${problem.severity?.toLowerCase() || 'info'}`}>
                          <div className="problem-header">
                            <span className="problem-severity">
                              {problem.severity === 'HIGH' ? '🔴' : 
                               problem.severity === 'MEDIUM' ? '🟡' : 
                               problem.severity === 'LOW' ? '🔵' : '⚪'}
                              {problem.severity || 'INFO'}
                            </span>
                            {problem.lineNumber && (
                              <span className="problem-line">Строка: {problem.lineNumber}</span>
                            )}
                          </div>
                          
                          <div className="problem-message">
                            {problem.message || problem.description || 'Неизвестная проблема'}
                          </div>
                          
                          {problem.ruleName && (
                            <div className="problem-rule">
                              <span className="rule-label">Правило:</span>
                              <code>{problem.ruleName}</code>
                            </div>
                          )}

                          {/* Кнопка для показа/скрытия фрагмента кода */}
                          {problem.snippet && (
                            <div className="problem-snippet-container">
                              <button 
                                className="snippet-toggle-btn"
                                onClick={() => toggleSnippet(problemId)}
                              >
                                {isExpanded ? '▼ Скрыть код' : '▶ Показать код'}
                              </button>
                              
                              {isExpanded && (
                                <div className="problem-snippet">
                                  <pre className="code-snippet">
                                    <code>{problem.snippet}</code>
                                  </pre>
                                </div>
                              )}
                            </div>
                          )}
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}

              {problems.length === 0 && success && (
                <div className="no-problems">
                  <span className="success-icon">✅</span>
                  <p>Проблем не найдено! Код соответствует стандартам качества.</p>
                </div>
              )}
            </div>
          </div>
        </div>
      );
    }
  };

  return (
    <div className="file-analyzer-container">
      <div className="analyzer-card">
        <h2>Анализ Java кода</h2>
        <p className="description">Загрузите .java файл или ZIP архив для анализа</p>

        {!result ? (
          <form onSubmit={handleSubmit}>
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
        ) : (
          renderResult()
        )}
        
        <div className="dashboard-button">
          <button
            type="button"
            onClick={handleGoToDashboard}
            className="submit-btn secondary"
            disabled={loading || saving}
          >
            Вернуться в профиль
          </button>
        </div>

        <div className="history-button">
          <button
          type="button"
          onClick={handleGoToHistory}
          className="submit-btn secondary">История</button>
        </div>
      </div>

      {showSaveDialog && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>💾 Сохранить результаты анализа</h3>
            <p>Введите название проекта для сохранения:</p>
            <input
              type="text"
              value={saveProjectName}
              onChange={(e) => setSaveProjectName(e.target.value)}
              placeholder="Название проекта"
              className="modal-input"
              autoFocus
              disabled={saving}
            />
            {error && (
              <div className="modal-error">
                <span className="error-icon">⚠️</span>
                {error}
              </div>
            )}
            <div className="modal-actions">
              <button 
                onClick={handleSaveResult}
                className="modal-btn primary"
                disabled={saving}
              >
                {saving ? '⏳ Сохранение...' : 'Сохранить'}
              </button>
              <button 
                onClick={() => setShowSaveDialog(false)}
                className="modal-btn secondary"
                disabled={saving}
              >
                Отмена
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
export default Analysis;