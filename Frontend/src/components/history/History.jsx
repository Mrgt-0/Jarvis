import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './History.css';

const History = () => {
    const navigate = useNavigate();
    const [history, setHistory] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [selectedAnalysis, setSelectedAnalysis] = useState(null);
    const [filter, setFilter] = useState('all'); 
    const [searchTerm, setSearchTerm] = useState('');
    const [expandedSnippets, setExpandedSnippets] = useState({}); 
    const [expandedProblems, setExpandedProblems] = useState({});

    useEffect(() => {
    fetchHistory();
  }, []);

  const fetchHistory = async () => {
    setLoading(true);
    setError('');

    try {
      console.log('Запрос истории анализов...');
      
      const response = await fetch('http://localhost:8080/api/analysis/history', {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        }
      });

      console.log('Статус ответа:', response.status);

      if (!response.ok) {
        throw new Error(`Ошибка ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      console.log('Получена история:', data);
      
      setHistory(Array.isArray(data) ? data : []);

    } catch (err) {
      console.error('Ошибка при загрузке истории:', err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleGoBack = () => {
    navigate('/analyzer');
  };

  const handleGoToDashboard = () => {
    navigate('/dashboard');
  };

  const handleViewDetails = (analysis) => {
    setSelectedAnalysis(analysis);
    setExpandedSnippets({}); 
    setExpandedProblems({});
  };

  const handleCloseDetails = () => {
    setSelectedAnalysis(null);
    setExpandedSnippets({});
    setExpandedProblems({});
  };

  const handleRefresh = () => {
    fetchHistory();
  };

  const toggleSnippet = (problemId) => {
        setExpandedSnippets(prev => ({
            ...prev,
            [problemId]: !prev[problemId]
        }));
  };

   const toggleAllProblems = () => {
        if (!selectedAnalysis?.problems) return;
        
        const allExpanded = Object.keys(expandedProblems).length === selectedAnalysis.problems.length;
        
        if (allExpanded) {
            setExpandedProblems({});
        } else {
            const newExpanded = {};
            selectedAnalysis.problems.forEach((_, index) => {
                newExpanded[index] = true;
            });
            setExpandedProblems(newExpanded);
        }
  };

  // const handleDeleteAnalysis = async (id) => {
  //   if (!window.confirm('Удалить этот анализ из истории?')) {
  //     return;
  //   }

  //   try {
  //     const response = await fetch(`http://localhost:8080/api/analysis/history/${id}`, {
  //       method: 'DELETE',
  //       credentials: 'include'
  //     });

  //     if (!response.ok) {
  //       throw new Error('Ошибка при удалении');
  //     }

  //     setHistory(history.filter(item => item.id !== id));
      
  //     if (selectedAnalysis?.id === id) {
  //       setSelectedAnalysis(null);
  //     }

  //   } catch (err) {
  //     console.error('Ошибка удаления:', err);
  //     setError('Не удалось удалить запись');
  //   }
  // };

  // Фильтрация истории
  const filteredHistory = history.filter(item => {
        if (filter === 'success' && !item.success) return false;
        if (filter === 'error' && item.success !== false) return false;

        if (searchTerm) {
            const searchLower = searchTerm.toLowerCase();
            const projectMatch = item.projectName?.toLowerCase().includes(searchLower);
            const fileMatch = item.fileName?.toLowerCase().includes(searchLower);
            return projectMatch || fileMatch;
        }

        return true;
  });

  // Группировка по датам
  const groupedHistory = filteredHistory.reduce((groups, item) => {
    const date = new Date(item.analysisDate || item.createdAt).toLocaleDateString('ru-RU');
    if (!groups[date]) {
      groups[date] = [];
    }
    groups[date].push(item);
    return groups;
  }, {});

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleString('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getStatusIcon = (item) => {
    if (item.success === false) return '❌';
    const problemsCount = item.problems?.length || 0;
    if (problemsCount > 0) return '⚠️';
    return '✅';
  };

  const getStatusClass = (item) => {
    if (item.success === false) return 'status-error';
    const problemsCount = item.problems?.length || 0;
    if (problemsCount > 0) return 'status-warning';
    return 'status-success';
  };

  const getProblemsSummary = (item) => {
    if (item.success === false) return 'Ошибка анализа';
    const count = item.problems?.length || 0;
    if (count === 0) return 'Проблем не найдено';
    return `${count} ${count === 1 ? 'проблема' : count < 5 ? 'проблемы' : 'проблем'}`;
  };

  const renderFormattedSnippet = (snippet, problemLine) => {
        if (!snippet) return null;

        const lines = snippet.split('\n').filter(line => line.trim() !== '');
        
        return (
            <div className="formatted-snippet">
                {lines.map((line, index) => {
                    const isProblemLine = line.includes('⚠️') || line.includes('↑');
                    const lineNumber = line.match(/^\s*(\d+)\s*\|/);
                    
                    return (
                        <div 
                            key={index} 
                            className={`snippet-line ${isProblemLine ? 'problem-line' : ''}`}
                        >
                            {lineNumber && (
                                <span className="snippet-line-number">{lineNumber[1]}</span>
                            )}
                            <span className="snippet-line-content">{line}</span>
                        </div>
                    );
                })}
            </div>
        );
    };

  if (loading && history.length === 0) {
    return (
      <div className="history-container">
        <div className="history-card">
          <div className="loading-state">
            <div className="spinner-large"></div>
            <p>Загрузка истории анализов...</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="history-container">
      <div className="history-card">
        <div className="history-header">
          <h1>📊 История анализов</h1>
          <div className="header-actions">
            <button onClick={handleRefresh} className="icon-btn" title="Обновить">
              🔄
            </button>
            <button onClick={handleGoBack} className="nav-btn">
              ← К анализатору
            </button>
            <button onClick={handleGoToDashboard} className="nav-btn secondary">
              В профиль
            </button>
          </div>
        </div>

        {error && (
          <div className="error-message">
            <span className="error-icon">⚠️</span>
            {error}
            <button onClick={fetchHistory} className="retry-btn">
              Повторить
            </button>
          </div>
        )}

        <div className="history-controls">
          <div className="search-box">
            <span className="search-icon">🔍</span>
            <input
              type="text"
              placeholder="Поиск по проекту или файлу..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="search-input"
            />
            {searchTerm && (
              <button 
                className="clear-search"
                onClick={() => setSearchTerm('')}
              >
                ✕
              </button>
            )}
          </div>

          <div className="filter-buttons">
            <button
              className={`filter-btn ${filter === 'all' ? 'active' : ''}`}
              onClick={() => setFilter('all')}
            >
              Все
            </button>
            <button
              className={`filter-btn ${filter === 'success' ? 'active' : ''}`}
              onClick={() => setFilter('success')}
            >
              ✅ Успешные
            </button>
            <button
              className={`filter-btn ${filter === 'error' ? 'active' : ''}`}
              onClick={() => setFilter('error')}
            >
              ❌ С ошибками
            </button>
          </div>
        </div>

        {history.length === 0 ? (
          <div className="empty-state">
            <span className="empty-icon">📭</span>
            <h3>История анализов пуста</h3>
            <p>Загрузите файл для анализа, и результаты появятся здесь</p>
            <button onClick={handleGoBack} className="primary-btn">
              Перейти к анализатору
            </button>
          </div>
        ) : filteredHistory.length === 0 ? (
          <div className="empty-state">
            <span className="empty-icon">🔍</span>
            <h3>Ничего не найдено</h3>
            <p>Попробуйте изменить параметры поиска</p>
            <button 
              onClick={() => {
                setSearchTerm('');
                setFilter('all');
              }} 
              className="secondary-btn"
            >
              Сбросить фильтры
            </button>
          </div>
        ) : (
          <div className="history-timeline">
            {Object.entries(groupedHistory).map(([date, items]) => (
              <div key={date} className="timeline-group">
                <div className="timeline-date">
                  <span className="date-badge">{date}</span>
                  <span className="items-count">{items.length} анализ(ов)</span>
                </div>
                
                <div className="timeline-items">
                  {items.map((item) => (
                    <div 
                      key={item.id} 
                      className={`history-item ${getStatusClass(item)} ${selectedAnalysis?.id === item.id ? 'selected' : ''}`}
                      onClick={() => handleViewDetails(item)}
                    >
                      <div className="item-icon">{getStatusIcon(item)}</div>
                      
                      <div className="item-content">
                        <div className="item-header">
                          <span className="item-name">
                            {item.projectName || 'Без проекта'}
                          </span>
                          <span className="item-time">
                            {formatDate(item.analysisDate || item.createdAt)}
                          </span>
                        </div>
                        
                        <div className="item-details">
                          <span className="item-file">
                            📄 {item.fileName || 'Неизвестный файл'}
                          </span>
                          <span className="item-status">
                            {getProblemsSummary(item)}
                          </span>
                        </div>

                        {item.packageName && (
                          <div className="item-package">
                            📦 {item.packageName}
                          </div>
                        )}
                      </div>

                      {/* <button 
                        className="delete-btn"
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDeleteAnalysis(item.id);
                        }}
                        title="Удалить из истории"
                      >
                        🗑️
                      </button> */}
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        )}

        {loading && (
          <div className="loading-overlay">
            <div className="spinner"></div>
          </div>
        )}
      </div>

      {selectedAnalysis && (
                <div className="modal-overlay" onClick={handleCloseDetails}>
                    <div className="modal-content history-modal" onClick={e => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>Детали анализа</h2>
                            <button className="close-btn" onClick={handleCloseDetails}>✕</button>
                        </div>

                        <div className="modal-body">
                            <div className="analysis-info">
                                <div className="info-section">
                                    <h4>Основная информация</h4>
                                    <div className="info-grid">
                                        <div className="info-row">
                                            <span className="info-label">Проект:</span>
                                            <span className="info-value">{selectedAnalysis.projectName || 'Не указан'}</span>
                                        </div>
                                        <div className="info-row">
                                            <span className="info-label">Файл:</span>
                                            <span className="info-value">{selectedAnalysis.fileName || 'Не указан'}</span>
                                        </div>
                                        <div className="info-row">
                                            <span className="info-label">Пакет:</span>
                                            <span className="info-value">{selectedAnalysis.packageName || 'Не указан'}</span>
                                        </div>
                                        <div className="info-row">
                                            <span className="info-label">Дата:</span>
                                            <span className="info-value">
                                                {formatDate(selectedAnalysis.analysisDate || selectedAnalysis.createdAt)}
                                            </span>
                                        </div>
                                        <div className="info-row">
                                            <span className="info-label">Статус:</span>
                                            <span className={`info-value status-badge ${getStatusClass(selectedAnalysis)}`}>
                                                {selectedAnalysis.success === false ? 'Ошибка' : 
                                                 selectedAnalysis.problems?.length > 0 ? 'Проблемы найдены' : 'Успешно'}
                                            </span>
                                        </div>
                                    </div>
                                </div>

                                {selectedAnalysis.success === false ? (
                                    <div className="error-section">
                                        <h4>❌ Ошибка анализа</h4>
                                        <div className="error-details">
                                            {selectedAnalysis.errorMessage || 'Неизвестная ошибка'}
                                        </div>
                                    </div>
                                ) : (
                                    <>
                                        {selectedAnalysis.classNames?.length > 0 && (
                                            <div className="classes-section">
                                                <h4>📚 Классы в файле</h4>
                                                <div className="classes-list">
                                                    {selectedAnalysis.classNames.map((className, idx) => (
                                                        <span key={idx} className="class-tag">{className}</span>
                                                    ))}
                                                </div>
                                            </div>
                                        )}

                                        {selectedAnalysis.problems?.length > 0 && (
                                            <div className="problems-section">
                                                <div className="problems-header">
                                                    <h4>🔍 Найденные проблемы ({selectedAnalysis.problems.length})</h4>
                                                    <button 
                                                        className="toggle-all-btn"
                                                        onClick={toggleAllProblems}
                                                    >
                                                        {Object.keys(expandedProblems).length === selectedAnalysis.problems.length 
                                                            ? '▼ Свернуть все' 
                                                            : '▶ Развернуть все'}
                                                    </button>
                                                </div>
                                                
                                                <div className="problems-list">
                                                    {selectedAnalysis.problems.map((problem, idx) => {
                                                        const problemId = `${selectedAnalysis.id}-${idx}`;
                                                        const isExpanded = expandedSnippets[problemId] || expandedProblems[idx];
                                                        
                                                        return (
                                                            <div key={idx} className={`problem-item severity-${problem.severity?.toLowerCase() || 'info'}`}>
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
                                                                                {renderFormattedSnippet(problem.snippet, problem.lineNumber)}
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

                                        {selectedAnalysis.problems?.length === 0 && (
                                            <div className="no-problems">
                                                <span className="success-icon">✅</span>
                                                <p>Проблем не найдено! Код соответствует стандартам качества.</p>
                                            </div>
                                        )}
                                    </>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
  );
};
export default History;