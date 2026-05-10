from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional, List
import asyncio
import logging
from langchain_core.prompts import PromptTemplate
from langchain_ollama import OllamaLLM
import uvicorn
from fastapi.responses import JSONResponse
from functools import lru_cache

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="PMD NLP Explainer")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

class PMDViolation(BaseModel):
    fileName: str
    message: str
    severity: str
    ruleId: str
    snippet: str

class ExplainRequest(BaseModel):
    violations: List[PMDViolation]
    language: Optional[str] = "ru"

class ExplainResponse(BaseModel):
    explanations: List[str]

llm = OllamaLLM(
    base_url="http://localhost:11434",
    model="deepseek-coder:1.3b",
    temperature=0.1,
    num_predict=300,
    timeout=120
)

prompt_template = PromptTemplate.from_template("""
Ты — ассистент по улучшению качества Java кода. 
Статический анализатор PMD выдал следующее предупреждение: 
Правило: {ruleId}                                          
Сообщение анализатора: {message}      
Код:
```java
{snippet}

Ответь простым языком на русском (3-4 предложения):
1. В чём проблема?
2. Как исправить?
                                               
Формат ответа:
ПРОБЛЕМА: ...
ИСПРАВЛЕНИЕ: ...
""")

chain = prompt_template | llm

cache = {}

@app.post("/explain")
async def explain_violations(request: ExplainRequest):
    explanations = []
    
    for v in request.violations:
        cache_key = f"{v.ruleId}:{hash(v.snippet)}"
        
        if cache_key in cache:
            logger.info(f"Из кэша: {v.ruleId}")
            explanations.append(cache[cache_key])
            continue
        
        try:
            result = await asyncio.wait_for(
                chain.ainvoke({
                    "ruleId": v.ruleId,
                    "message": v.message,
                    "severity": v.severity,
                    "fileName": v.fileName,
                    "snippet": v.snippet,
                    "language": request.language
                }),
                timeout=60.0
            )
            explanation = result.strip() if isinstance(result, str) else str(result)
            cache[cache_key] = explanation
            explanations.append(explanation)
            
        except asyncio.TimeoutError:
            explanations.append(f"Таймаут: {v.ruleId}")
        except Exception as e:
            explanations.append(f"Ошибка: {v.ruleId}")
    
    return JSONResponse(content={"explanations": explanations})

@app.get("/health")
async def health():
    return {"status": "ok", "service": "nlp-explainer"}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)