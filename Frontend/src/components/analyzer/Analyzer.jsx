import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './Analyzer.css';
import JSZip from 'jszip';
window.JSZip = JSZip;

const ProblemItem = ({ problem, index, aiExplanation, expandedSnippets, toggleSnippet, fileName }) => {
  const problemId = `${fileName}-${index}`;
  const isExpanded = expandedSnippets[problemId];
  const severityIcon = { HIGH: '🔴', MEDIUM: '🟡', LOW: '🔵' }[problem.severity] || '⚪';

  return (
    <div className={`problem-item severity-${problem.severity?.toLowerCase() || 'info'}`}>
      <div className="problem-header">
        <span className="problem-severity">{severityIcon} {problem.severity || 'INFO'}</span>
        {problem.lineNumber && <span className="problem-line">Строка: {problem.lineNumber}</span>}
      </div>
      <div className="problem-message">{problem.message || problem.description || 'Неизвестная проблема'}</div>
      {problem.ruleName && <div className="problem-rule"><span className="rule-label">Правило:</span> <code>{problem.ruleName}</code></div>}
      
      {aiExplanation && (
        <div className="ai-explanation">
          <div className="ai-header"><span className="ai-icon">🤖</span><span className="ai-label">AI-помощник</span></div>
          <div className="ai-text">{aiExplanation}</div>
        </div>
      )}
      
      {problem.snippet && (
        <div className="problem-snippet-container">
          <button className="snippet-toggle-btn" onClick={() => toggleSnippet(problemId)}>
            {isExpanded ? '▼ Скрыть код' : '▶ Показать код'}
          </button>
          {isExpanded && <div className="problem-snippet"><pre className="code-snippet"><code>{problem.snippet}</code></pre></div>}
        </div>
      )}
    </div>
  );
};

const ResultCard = ({ item, expandedSnippets, toggleSnippet }) => {
  const problems = item.problems || [];
  const fileName = item.fileName || 'Неизвестный файл';
  const success = item.success !== false;
  const icon = !success ? '❌' : problems.length > 0 ? '⚠️' : '✅';

  return (
    <div className={`result-card ${!success ? 'error' : problems.length > 0 ? 'warning' : 'success'}`}>
      <div className="result-header">
        <div className="file-info"><span className="file-icon">{icon}</span><span className="filename">{fileName}</span></div>
        <span className="file-status">{!success ? 'Ошибка анализа' : problems.length > 0 ? `${problems.length} проблем` : 'Проблем нет'}</span>
      </div>
      
      {!success && item.errorMessage ? (
        <div className="error-details"><div className="error-message"><strong>Ошибка:</strong> {item.errorMessage}</div></div>
      ) : (
        <div className="analysis-details">
          {item.packageName && <div className="info-item"><span className="info-label">Пакет:</span> <span className="info-value">{item.packageName}</span></div>}
          
          {problems.length > 0 && (
            <div className="problems-section">
              <h4>🔍 Найденные проблемы ({problems.length})</h4>
              <div className="problems-list">
                {problems.map((problem, i) => (
                  <ProblemItem key={i} problem={problem} index={i} aiExplanation={item.aiExplanations?.[i]} expandedSnippets={expandedSnippets} toggleSnippet={toggleSnippet} fileName={fileName} />
                ))}
              </div>
            </div>
          )}
          {problems.length === 0 && success && <div className="no-problems"><span className="success-icon">✅</span><p>Проблем не найдено!</p></div>}
        </div>
      )}
    </div>
  );
};

const Analysis = () => {
  const navigate = useNavigate();
  const [file, setFile] = useState(null);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [dragActive, setDragActive] = useState(false);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [analysisType, setAnalysisType] = useState('file');
  const [showSaveDialog, setShowSaveDialog] = useState(false);
  const [saveProjectName, setSaveProjectName] = useState('');
  const [expandedSnippets, setExpandedSnippets] = useState({});

  const toggleSnippet = (id) => setExpandedSnippets(prev => ({ ...prev, [id]: !prev[id] }));

  const validateZipContent = async (zipFile) => {
  try {
    const JSZip = window.JSZip;
    if (!JSZip) {
      console.warn('JSZip не подключён, проверка содержимого ZIP пропущена');
      return true;
    }
    
    const zip = await JSZip.loadAsync(zipFile);
    const javaFiles = Object.keys(zip.files).filter(name => 
      name.toLowerCase().endsWith('.java') && !zip.files[name].dir
    );
    
    if (javaFiles.length === 0) {
      setError('ZIP архив не содержит .java файлов');
      return false;
    }
    
    console.log(`Найдено ${javaFiles.length} .java файлов в архиве`);
    return true;
  } catch (err) {
    console.error('Ошибка чтения ZIP:', err);
    setError('Не удалось прочитать ZIP архив. Возможно, файл повреждён.');
    return false;
  }
};

const validateAndSetFile = async (f) => {
  if (!f) return;
  
  const ext = analysisType === 'file' ? '.java' : '.zip';
  const maxSize = analysisType === 'file' ? 10 : 50;
  
  if (!f.name.toLowerCase().endsWith(ext)) { 
    setError(`Пожалуйста, выберите ${ext} файл`); 
    return; 
  }
  if (f.size > maxSize * 1024 * 1024) { 
    setError(`Файл слишком большой. Максимум ${maxSize}MB`); 
    return; 
  }
  if (analysisType === 'archive') {
    setError('⏳ Проверка содержимого архива...');
    const isValid = await validateZipContent(f);
    if (!isValid) return;
  }
  
  setFile(f); 
  setError(''); 
  setResult(null);
};

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!file) { setError('Выберите файл'); return; }
    setLoading(true); setError('');
    const formData = new FormData(); formData.append('file', file);
    const endpoint = analysisType === 'file' ? 'http://localhost:8080/api/analysis/file' : 'http://localhost:8080/api/analysis/archive';
    try {
      const res = await fetch(endpoint, { method: 'POST', body: formData, credentials: 'include' });
      const text = await res.text();
        
      if (!res.ok) 
          throw new Error(await res.text() || `Ошибка ${res.status}`);
      
      const data = text ? JSON.parse(text) : null;
      setResult(data);
    } catch (err) { setError(err.message); }
    finally { setLoading(false); }
  };

  const handleSave = async () => {
    if (!result) return;
    setSaving(true);
    const items = Array.isArray(result) ? result : [result];
    try {
      await Promise.all(items.map(item => fetch('http://localhost:8080/api/analysis/saveAnalysisResult', {
        method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ ...item, projectName: saveProjectName || 'default' }), credentials: 'include'
      })));
      setShowSaveDialog(false);
      alert('✅ Сохранено!');
    } catch (err) { setError('Ошибка сохранения'); }
    finally { setSaving(false); }
  };

  if (result) {
    const items = Array.isArray(result) ? result : [result];
    return (
      <div className="file-analyzer-container">
        <div className="analyzer-card">
          <div className="results-header">
            <h3>📊 Результаты анализа</h3>
            <div className="results-actions">
              <button onClick={() => { setResult(null); setFile(null); }} className="action-btn secondary">Новый анализ</button>
              <button onClick={() => { setSaveProjectName(''); setShowSaveDialog(true); }} className="action-btn primary">💾 Сохранить</button>
            </div>
          </div>
          <div className="results-summary">
            <span className="badge success">✅ Успешно: {items.filter(r => r.success !== false).length}</span>
            <span className="badge error">⚠️ С ошибками: {items.filter(r => r.success === false).length}</span>
            <span className="badge warning">🔍 Проблем: {items.reduce((a, r) => a + (r.problems?.length || 0), 0)}</span>
          </div>
          <div className="results-list">
            {items.map((item, i) => <ResultCard key={i} item={item} expandedSnippets={expandedSnippets} toggleSnippet={toggleSnippet} />)}
          </div>
          <div className="nav-buttons">
            <button onClick={() => navigate('/dashboard')} className="submit-btn secondary">В профиль</button>
            <button onClick={() => navigate('/history')} className="submit-btn secondary">История</button>
          </div>
        </div>
        {showSaveDialog && (
          <div className="modal-overlay"><div className="modal-content">
            <h3>💾 Сохранить</h3>
            <input value={saveProjectName} onChange={e => setSaveProjectName(e.target.value)} placeholder="Название проекта" className="modal-input" autoFocus />
            <div className="modal-actions">
              <button onClick={handleSave} className="modal-btn primary" disabled={saving}>{saving ? 'Сохранение...' : 'Сохранить'}</button>
              <button onClick={() => setShowSaveDialog(false)} className="modal-btn secondary">Отмена</button>
            </div>
          </div></div>
        )}
      </div>
    );
  }

  return (
    <div className="file-analyzer-container">
      <div className="analyzer-card">
        <h2>Анализ Java кода</h2>
        <p className="description">Загрузите .java файл или ZIP архив для анализа</p>
        <form onSubmit={handleSubmit}>
          <div className="analysis-type-switcher">
            {['file', 'archive'].map(t => (
              <button key={t} type="button" className={`type-btn ${analysisType === t ? 'active' : ''}`}
                onClick={() => { setAnalysisType(t); setFile(null); setError(''); }} disabled={loading}>
                {t === 'file' ? 'Файл .java' : 'ZIP архив'}
              </button>
            ))}
          </div>
          <div className={`file-upload-area ${dragActive ? 'drag-active' : ''} ${file ? 'file-selected' : ''}`}
            onDragEnter={e => { e.preventDefault(); setDragActive(true); }}
            onDragOver={e => { e.preventDefault(); setDragActive(true); }}
            onDragLeave={() => setDragActive(false)}
            onDrop={e => { e.preventDefault(); setDragActive(false); validateAndSetFile(e.dataTransfer.files[0]); }}>
            <input type="file" onChange={e => validateAndSetFile(e.target.files[0])} accept={analysisType === 'file' ? '.java' : '.zip'} className="file-input" />
            {file ? (
              <div className="selected-file">
                <div className="file-details">
                  <div className="file-name">{file.name}</div>
                    <div className="file-size">
                      {file.size > 1024 * 1024 
                        ? `${(file.size / 1024 / 1024).toFixed(2)} MB` 
                        : `${(file.size / 1024).toFixed(2)} KB`}
                    </div>
                  </div>
                <button type="button" onClick={() => { setFile(null); setResult(null); }} className="remove-file-btn">Сбросить</button>
              </div>
            ) : (
              <label className="file-label">
                <div className="upload-text"><span className="primary-text">Выберите или перетащите файл</span><span className="secondary-text">{analysisType === 'file' ? '.java до 10MB' : '.zip до 50MB'}</span></div>
              </label>
            )}
          </div>
          {error && <div className="error-message"><span className="error-icon">⚠️</span>{error}</div>}
          <button type="submit" className="submit-btn" disabled={loading || !file}>
            {loading ? <><span className="spinner"></span>Анализ...</> : 'Анализировать'}
          </button>
        </form>
        <div className="nav-buttons">
          <button onClick={() => navigate('/dashboard')} className="submit-btn secondary">В профиль</button>
          <button onClick={() => navigate('/history')} className="submit-btn secondary">История</button>
        </div>
      </div>
    </div>
  );
};

export default Analysis;