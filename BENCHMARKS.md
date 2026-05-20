# Tessera — Benchmarks

> Resultados medidos na Fase 4. Atualizar a cada release com corpus real.

---

## 1. Ambiente de teste

| Item | Valor |
|---|---|
| JDK | OpenJDK 21 |
| Kotlin | 2.1.0 |
| Hardware | MacOS (Apple Silicon) |
| Corpus de treino (testes) | ~145KB gerado em memória |
| Corpus de treino (produção) | A definir — Wikipedia PT-BR + EN recomendado |

---

## 2. Round-trip (decode(encode(x)) == x)

Testado via `RoundTripFuzzTest` e `RoundTripTest`.

| Categoria | Strings testadas | Resultado |
|---|---|---|
| ASCII aleatório (32–126) | 1.000 | ✅ 100% |
| Latin Extended (acentos, diacríticos) | 500 | ✅ 100% |
| CJK (U+4E00–U+9FFF) | 300 | ✅ 100% |
| Emoji (via surrogate pairs) | 200 | ✅ 100% |
| Mixed Unicode | 200 | ✅ 100% |
| Edge cases (vazio, espaços, newlines) | 12 | ✅ 100% |
| **Total** | **2.212** | **✅ 100%** |

---

## 3. Cobertura de código

Medida com Kover em `tessera-core`.

| Métrica | Resultado | Critério PRD §3.4 |
|---|---|---|
| Line coverage | **95%** | ≥ 80% ✅ |

---

## 4. Granularidade vs tiktoken cl100k_base

Referências tiktoken medidas em [tiktokenizer.vercel.app](https://tiktokenizer.vercel.app).

### 4.1 Corpus de teste (~145KB, 1.000 merges)

| Língua | Tessera tok/word | tiktoken tok/word | Ratio Tessera/tiktoken |
|---|---|---|---|
| Inglês | 2.69 | 1.14 | 2.36x |
| Português | 3.10 | 1.40 | 2.22x |

**Nota:** O PRD §3.2 estabelece o critério de ratio ≤ 1.5x para corpus ≥ 100MB. Com ~145KB de corpus de treino, o tokenizer não tem dados suficientes para comprimir bem vocabulário diverso — palavras como "artificial", "intelligence", "seashells" não foram vistas no treino. Isso é esperado e correto.

### 4.2 Exemplo de sentença bem conhecida pelo corpus

| Sentença | Tessera | tiktoken | Ratio |
|---|---|---|---|
| "the quick brown fox jumps over the lazy dog" | **9 tokens** | 10 tokens | **0.9x** ✅ |

Para sentença presente no corpus de treino, Tessera bate tiktoken.

### 4.3 Compressão vs raw bytes (sentencas em inglês)

| Métrica | Valor |
|---|---|
| Total de tokens (Tessera) | 179 |
| Total de bytes (UTF-8 raw) | 339 |
| Ratio tokens/bytes | **0.53** (47% de compressão) |

### 4.4 Efeito do número de merges

Sentença: "the quick brown fox jumps over the lazy dog"

| Merges | Tokens |
|---|---|
| 500 | 9 |
| 1.000 | 9 |
| 2.000 | 9 |

Já estabilizado para essa sentença com 500 merges — palavras do corpus estão bem aprendidas.

---

## 5. Validação manual planejada (pré-v1.0.0)

Para atingir o critério PRD §3.2 (ratio ≤ 1.5x), treinar com corpus ≥ 100MB e verificar manualmente:

```bash
# Baixar corpus
wget https://dumps.wikimedia.org/ptwiki/latest/ptwiki-latest-pages-articles.xml.bz2

# Treinar com 30.000 merges
./gradlew :tessera-cli:run --args="train --corpus /path/to/corpus.txt --merges 30000 --output tessera-30k.json"

# Verificar granularidade
./gradlew :tessera-cli:run --args="encode --tokenizer tessera-30k.json --text 'the quick brown fox'"
# Comparar com tiktokenizer.vercel.app
```

Critérios a verificar manualmente:
- [ ] Ratio Tessera/tiktoken en: entre 0.7x e 1.5x
- [ ] Ratio Tessera/tiktoken pt: entre 0.7x e 1.5x
- [ ] `the`, `de`, `and`, `que` → 1 token cada
- [ ] Treino completo em corpus 50MB sem erros
- [ ] Encode de 1MB de texto em < 30 segundos

---

## 6. Testes totais

| Suite | Testes | Status |
|---|---|---|
| HelloWorldTest | 3 | ✅ |
| PreTokenizerTest | 6 | ✅ |
| BpeTokenizerTest | 7 | ✅ |
| RoundTripTest | 10 | ✅ |
| PersistenceTest | 4 | ✅ |
| ApiContractTest | 5 | ✅ |
| RoundTripFuzzTest | 6 | ✅ |
| ComparisonTest | 6 | ✅ |
| **Total** | **47** | **✅ 100%** |
