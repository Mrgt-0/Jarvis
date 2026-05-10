import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './History.css';

const ProblemItem = ({ problem, idx, analysis, expandedSnippets, toggleSnippet }) => {
  const pid = `${analysis.id}-${idx}`;
  const expanded = expandedSnippets[pid];
  const sev = { HIGH: '🔴', MEDIUM: '🟡', LOW: '🔵' }[problem.severity] || '⚪';

  return (
    <div className={`problem-item severity-${problem.severity?.toLowerCase() || 'info'}`}>
      <div className="problem-header">
        <span className="problem-severity">{sev} {problem.severity || 'INFO'}</span>
        {problem.lineNumber && <span className="problem-line">Строка: {problem.lineNumber}</span>}
      </div>
      <div className="problem-message">{problem.message || problem.description || 'Неизвестная проблема'}</div>
      {problem.ruleName && <div className="problem-rule"><span className="rule-label">Правило:</span> <code>{problem.ruleName}</code></div>}
      
      {analysis.aiExplanations?.[idx] && (
        <div className="ai-explanation">
          <div className="ai-header"><span className="ai-icon">🤖</span><span className="ai-label">AI-помощник</span></div>
          <div className="ai-text">{analysis.aiExplanations[idx]}</div>
        </div>
      )}
      
      {problem.snippet && (
        <div className="problem-snippet-container">
          <button className="snippet-toggle-btn" onClick={() => toggleSnippet(pid)}>{expanded ? '▼ Скрыть' : '▶ Показать'} код</button>
          {expanded && <div className="problem-snippet"><div className="formatted-snippet">{problem.snippet.split('\n').filter(l => l.trim()).map((l, i) => <div key={i} className={`snippet-line ${l.includes('⚠️') ? 'problem-line' : ''}`}>{l}</div>)}</div></div>}
        </div>
      )}
    </div>
  );
};

const DetailModal = ({ analysis, onClose, expandedSnippets, toggleSnippet, formatDate, getStatusClass }) => (
  <div className="modal-overlay" onClick={onClose}>
    <div className="modal-content history-modal" onClick={e => e.stopPropagation()}>
      <div className="modal-header"><h2>Детали анализа</h2><button className="close-btn" onClick={onClose}>✕</button></div>
      <div className="modal-body">
        <div className="info-section">
          <h4>Основная информация</h4>
          <div className="info-grid">
            {[['Проект', analysis.projectName], ['Файл', analysis.fileName], ['Пакет', analysis.packageName], ['Дата', formatDate(analysis.analysisTime)]].map(([label, value]) => (
              <div key={label} className="info-row"><span className="info-label">{label}:</span><span className="info-value">{value || 'Не указан'}</span></div>
            ))}
            <div className="info-row"><span className="info-label">Статус:</span>
              <span className={`info-value status-badge ${getStatusClass(analysis)}`}>
                {analysis.success === false ? 'Ошибка' : analysis.problems?.length > 0 ? 'Проблемы найдены' : 'Успешно'}
              </span>
            </div>
          </div>
        </div>

        {analysis.success === false ? (
          <div className="error-section"><h4>❌ Ошибка</h4><div className="error-details">{analysis.errorMessage || 'Неизвестная ошибка'}</div></div>
        ) : (
          analysis.problems?.length > 0 ? (
            <div className="problems-section">
              <h4>🔍 Проблемы ({analysis.problems.length})</h4>
              <div className="problems-list">
                {analysis.problems.map((p, i) => <ProblemItem key={i} problem={p} idx={i} analysis={analysis} expandedSnippets={expandedSnippets} toggleSnippet={toggleSnippet} />)}
              </div>
            </div>
          ) : (
            <div className="no-problems"><span className="success-icon">✅</span><p>Проблем не найдено!</p></div>
          )
        )}
      </div>
    </div>
  </div>
);

const History = () => {
  const navigate = useNavigate();
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [selected, setSelected] = useState(null);
  const [search, setSearch] = useState('');
  const [expanded, setExpanded] = useState({});

  const fetchHistory = async () => {
    setLoading(true); setError('');
    try {
      const res = await fetch('http://localhost:8080/api/analysis/history', { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' } });
      if (!res.ok) throw new Error(`Ошибка ${res.status}`);
      const data = await res.json();
      console.log('История:', data);
      data.forEach(item => console.log('ID:', item.id, 'Тип:', typeof item.id));  
      setHistory(data);
    } catch (err) { setError(err.message); }
    finally { setLoading(false); }
  };

  useEffect(() => { fetchHistory(); }, []);

  const formatDate = s => {
    if (!s) return 'Дата не указана';
    const c = s.replace('T', ' ').split('.')[0].split(' ');
    const [y, m, d] = c[0].split('-');
    const [h, min] = c[1] ? c[1].split(':') : ['00', '00'];
    return `${d}.${m}.${y} ${h}:${min}`;
  };

 const handleDeleteAnalysis = async (itemToDelete) => {
    const id = itemToDelete.id;
    console.log('ID для удаления:', id);
    if (!id) {
        console.error('Нет ID!');
        return;
    }
    if (!window.confirm('Удалить этот анализ из истории?')) return;
    try {
        const response = await fetch(`http://localhost:8080/api/analysis/deleteAnalysisResult/${id}`, {
            method: 'DELETE',
            credentials: 'include'
        });
        if (!response.ok) throw new Error('Ошибка при удалении');
        setHistory(prev => prev.filter(i => i.id !== id));
        if (selected?.id === id) setSelected(null); 
    } catch (err) {
        console.error('Ошибка при удалении:', err);
    }
};

  const statusIcon = i => i.success === false ? '❌' : i.problems?.length > 0 ? '⚠️' : '✅';
  const statusClass = i => i.success === false ? 'status-error' : i.problems?.length > 0 ? 'status-warning' : 'status-success';
  const summary = i => i.success === false ? 'Ошибка' : i.problems?.length ? `${i.problems.length} проблем` : 'Проблем нет';

  const filtered = search
    ? history.filter(i => 
        (i.projectName || '').toLowerCase().includes(search.toLowerCase()) ||
        (i.fileName || '').toLowerCase().includes(search.toLowerCase()) ||
        (i.packageName || '').toLowerCase().includes(search.toLowerCase())
      )
    : history;

  const grouped = filtered.reduce((g, i) => {
    const d = formatDate(i.analysisTime);
    (g[d] = g[d] || []).push(i);
    return g;
  }, {});

  if (loading && !history.length) return <div className="history-container"><div className="history-card"><div className="loading-state"><div className="spinner-large"></div><p>Загрузка...</p></div></div></div>;

  return (
    <div className="history-container">
      <div className="history-card">
        <div className="history-header">
          <h1>📊 История анализов</h1>
          <div className="header-actions">
            <button onClick={() => navigate('/analyzer')} className="nav-btn">К анализатору</button>
            <button onClick={() => navigate('/dashboard')} className="nav-btn secondary">В профиль</button>
          </div>
        </div>

        {error && <div className="error-message"><span className="error-icon">⚠️</span>{error}<button onClick={fetchHistory} className="retry-btn">Повторить</button></div>}

        <div className="search-box">
          <span className="search-icon">🔍</span>
          <input placeholder="Поиск..." value={search} onChange={e => setSearch(e.target.value)} className="search-input" />
          {search && <button className="clear-search" onClick={() => setSearch('')}>✕</button>}
        </div>

        {!history.length ? (
          <div className="empty-state"><span className="empty-icon">📭</span><h3>Пусто</h3><p>Загрузите файл для анализа</p><button onClick={() => navigate('/analyzer')} className="primary-btn">К анализатору</button></div>
        ) : Object.keys(grouped).length === 0 ? (
          <div className="empty-state">
            <span className="empty-icon">🔍</span>
            <h3>Ничего не найдено</h3>
            <p>Попробуйте изменить поисковый запрос</p>
          </div>
        ) : (
          <div className="history-timeline">
            {Object.entries(grouped).map(([date, items]) => (
              <div key={date} className="timeline-group">
                <div className="timeline-date"><span className="date-badge">{date}</span><span className="items-count">{items.length} анализ(а)</span></div>
                <div className="timeline-items">
                  {items.map(item => (
                    <div key={item.id} className={`history-item ${statusClass(item)} ${selected?.id === item.id ? 'selected' : ''}`} onClick={() => { setSelected(item); setExpanded({}); }}>
                      <div className="item-icon">{statusIcon(item)}</div>
                      <div className="item-content">
                        <div className="item-header"><span className="item-name">{item.projectName || 'Без проекта'}</span><span className="item-time">{formatDate(item.analysisTime)}</span></div>
                        <div className="item-details"><span className="item-file">📄 {item.fileName || 'Неизвестно'}</span><span className="item-status">{summary(item)}</span></div>
                        {item.packageName && <div className="item-package">📦 {item.packageName}</div>}
                      </div>
                      <button 
                        className="delete-btn"
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDeleteAnalysis(item.id);
                        }}
                        title="Удалить из истории"
                      >
                        🗑️
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {selected && <DetailModal analysis={selected} onClose={() => setSelected(null)} expandedSnippets={expanded} toggleSnippet={id => setExpanded(p => ({ ...p, [id]: !p[id] }))} formatDate={formatDate} getStatusClass={statusClass} />}
    </div>
  );
};

export default History;