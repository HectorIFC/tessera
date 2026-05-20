# PRD — Tessera

> **Biblioteca Kotlin para tokenização BPE byte-level.**
>
> *"Tessera" — do latim, peça de mosaico. Tokens são peças que, montadas, formam o mosaico da linguagem.*

---

## 📋 Documento de requisitos para implementação assistida via Claude Code

Este PRD descreve o projeto completo, decisões já tomadas, escopo, plano de fases, critérios de aceitação e armadilhas conhecidas. Foi construído após discussão detalhada sobre arquitetura. **Leia tudo antes de começar.**

---

## 1. Contexto e Motivação

### 1.1. Quem sou eu e por que esse projeto existe

Sou um desenvolvedor estudando LLMs **fazendo do zero**. Quero entender, na prática e na unha, como um tokenizador moderno funciona. Esse projeto é o **primeiro de dois codebases**:

1. **Tessera** (este projeto) — **biblioteca Kotlin** para tokenização BPE: converte texto ↔ IDs de tokens
2. **Próximo projeto (codebase separada)** — biblioteca de embeddings: converte IDs ↔ vetores semânticos. Vai **depender** de Tessera como uma das dependências Gradle.

Os dois projetos serão **codebases independentes**. O segundo vai consumir Tessera como dependência (`implementation("dev.tessera:tessera-core:X.Y.Z")`).

### 1.2. Natureza do projeto: BIBLIOTECA, não aplicação

**Tessera é uma biblioteca (library), não uma aplicação CLI.** Isso muda decisões importantes:

- O artefato principal é um **JAR consumível por outros projetos Kotlin/JVM**
- A **API pública** deve ser cuidadosamente projetada (estabilidade, ergonomia, documentação)
- O **CLI existe**, mas é um **módulo separado** (aplicação que consome a lib) — não o foco
- A biblioteca deve ser **publicável** (Maven Central, JitPack ou GitHub Packages)
- Versionamento semântico (SemVer) é obrigatório
- Backward compatibility de API matters

### 1.3. Princípios não-negociáveis

- **Kotlin puro, JVM target.** Sem dependências de bibliotecas terceiras de ML (sem DJL, KInference, Multik, KMath, DL4J, etc). O objetivo é aprender, então cada linha vem do zero.
- **Standard library do Kotlin apenas** para a lógica de tokenização. Exceções permitidas para: build system (Gradle), testes (JUnit/Kotest), serialização JSON (`kotlinx.serialization` — é first-party Kotlin), e logging básico.
- **Sem ML frameworks.** Mesmo que tornem mais fácil, derrota o propósito do projeto.
- **API pública minimalista e estável.** Tudo que não for parte da API pública deve ser `internal`.

### 1.4. Decisões arquiteturais já tomadas (NÃO RE-DECIDIR)

Essas decisões já foram tomadas após análise. **Não as questione** — siga-as:

1. **Byte-level BPE.** Vocabulário base = 256 bytes (0-255), igual GPT-2/GPT-4. Nunca opera em caracteres Unicode diretamente.
2. **Pré-tokenização com regex estilo `cl100k_base` (GPT-4).** Aplicada antes do BPE pra impedir merges entre fronteiras lógicas (palavras, contrações, números).
3. **Separador entre chunks no treino.** Implementado via ID sentinela (`-1`) que impede pares cruzando fronteiras de chunk.
4. **Encode greedy por menor rank.** No encode, sempre aplica o merge com menor rank (= aprendido primeiro), não o mais frequente.
5. **Persistência cedo.** Save/load do tokenizer treinado é requisito da Fase 1, não "depois".
6. **Round-trip determinístico.** `decode(encode(text)) == text` para qualquer string UTF-8 válida. Esse é o teste de sanidade definitivo.
7. **Multi-módulo desde o início.** O projeto é Gradle multi-módulo com pelo menos: `tessera-core` (lib), `tessera-cli` (aplicação), `tessera-samples` (exemplos de uso).

---

## 2. Escopo

### 2.1. Dentro do escopo (MUST HAVE)

#### Como biblioteca

- API pública estável e bem documentada
- Implementação completa de byte-level BPE em Kotlin
- Pré-tokenização com regex GPT-4 `cl100k_base`
- Treino a partir de corpus de texto (String ou InputStream)
- Encode (texto → IntArray)
- Decode (IntArray → texto)
- Persistência (save/load do vocab + merges em JSON)
- Suporte a tokens especiais customizáveis (`<|endoftext|>` por padrão)
- Configuração de publicação Maven (POM, sources jar, javadoc jar)
- Versionamento semântico
- KDoc completo em toda API pública
- Suite de testes unitários cobrindo round-trip, casos Unicode, edge cases

#### Como aplicação CLI (módulo separado)

- CLI mínimo demonstrando uso da biblioteca: treinar, encode, decode, inspecionar vocab
- Serve como exemplo "vivo" de como consumir a lib

#### Samples (módulo separado)

- Pelo menos 2 sample apps Kotlin que importem a lib e mostrem uso real
- Documentação inline mostrando padrões de uso recomendados

#### Validação

- Benchmark básico comparando com `tiktoken` (referência manual via tiktokenizer.vercel.app)

### 2.2. Fora do escopo (NÃO FAZER)

- Treinar modelo de embeddings (próximo projeto)
- Treinar modelo de linguagem
- GPU/aceleração de hardware
- API REST / servidor
- Suporte a múltiplos esquemas BPE (cl100k, p50k, r50k) — só um
- Tokenizadores não-BPE (WordPiece, SentencePiece, Unigram)
- Otimização extrema de performance (basta ser "razoável" — segundos pra encodar 1MB)
- Multiplatform (Kotlin/JS, Kotlin/Native) — só JVM por enquanto

### 2.3. Stretch goals (NICE TO HAVE, só se sobrar tempo após Fase 5)

- Treinamento paralelo usando coroutines
- Exportar/importar formato compatível com `tiktoken` (`.tiktoken`)
- Visualização colorida dos tokens no terminal
- Migração para Kotlin Multiplatform (após v1.0 estável)
- Publicação no Maven Central (vs apenas JitPack)

---

## 3. Definição de "Pronto" (Done)

O projeto é considerado finalizado quando **todos** estes critérios forem atendidos:

### 3.1. Critérios funcionais

- [ ] `decode(encode(text)) == text` passa para 100% de um conjunto de 1000+ strings UTF-8 aleatórias (incluindo emojis, CJK, árabe, hebraico, símbolos matemáticos)
- [ ] Treina sem erros num corpus de pelo menos 50MB
- [ ] Encode de 1MB de texto roda em menos de 30 segundos em hardware comum
- [ ] Save + load preserva o estado exatamente (vocab e merges idênticos antes/depois)

### 3.2. Critérios de qualidade comparativa

- [ ] Tokenizando 100 frases em inglês e 100 em português, a **granularidade média** (tokens por palavra) fica entre 0.7x e 1.5x da granularidade do `tiktoken` `cl100k_base` para o mesmo texto
- [ ] Palavras comuns (`the`, `de`, `and`, `que`) viram 1 token só após treino com corpus ≥ 100MB
- [ ] Caracteres Unicode raros (emojis, CJK) nunca quebram o decode

### 3.3. Critérios de biblioteca (NOVOS — críticos)

- [ ] Sample app consegue importar `tessera-core` via Gradle e usar a API sem fricção
- [ ] JAR publicado (ao menos via JitPack — instruções no README)
- [ ] API pública 100% documentada com KDoc
- [ ] Sample app demonstra: treino simples, encode/decode, save/load, uso de tokens especiais
- [ ] `tessera-core` não tem dependências runtime além de `kotlin-stdlib` e `kotlinx-serialization-json`
- [ ] Tudo que não é API pública está marcado como `internal`

### 3.4. Critérios de código

- [ ] Cobertura de testes ≥ 80% no módulo core
- [ ] Sem warnings do compilador Kotlin
- [ ] README com instruções de uso como **biblioteca** (não como app)
- [ ] CLI funcional documentado no README do seu próprio módulo
- [ ] Tag `v1.0.0` no git

---

## 4. Especificação Técnica

### 4.1. Identidade do projeto

- **Nome:** Tessera
- **Group ID:** `dev.tessera` (ajustar de acordo com seu domínio/preferência)
- **Artifact ID core:** `tessera-core`
- **Artifact ID CLI:** `tessera-cli`
- **Package base:** `dev.tessera`
- **Tagline:** "A byte-level BPE tokenizer library in pure Kotlin."

### 4.2. Stack

- **Linguagem:** Kotlin 2.0+ (target JVM 17+)
- **Build:** Gradle multi-módulo com Kotlin DSL (`build.gradle.kts`)
- **Testes:** Kotest (recomendado) ou JUnit 5
- **Serialização:** `kotlinx.serialization` (JSON) para save/load
- **Publicação:** `maven-publish` plugin do Gradle, configurado para JitPack inicialmente

### 4.3. Estrutura do projeto (Gradle multi-módulo)

```
tessera/                              # repo raiz
├── settings.gradle.kts               # define os módulos
├── build.gradle.kts                  # config compartilhada (versões, repos)
├── gradle.properties                 # versão global do projeto
├── README.md                         # documentação principal (foco em uso como lib)
├── PRD.md
├── ARCHITECTURE.md                   # criado na Fase 5
├── BENCHMARKS.md                     # criado na Fase 4
├── CHANGELOG.md                      # mantido desde a Fase 0
├── LICENSE
├── .gitignore
├── corpus/                           # corpus de treino (gitignored)
│   └── .gitkeep
│
├── tessera-core/                     # 🎯 A BIBLIOTECA
│   ├── build.gradle.kts              # configurado pra publicação
│   ├── README.md                     # docs específicas do módulo lib
│   └── src/
│       ├── main/kotlin/dev/tessera/
│       │   ├── Tokenizer.kt              # interface pública
│       │   ├── BpeTokenizer.kt           # impl principal
│       │   ├── Trainer.kt                # API de treino
│       │   ├── TrainingConfig.kt         # config tipada (data class)
│       │   ├── SpecialTokens.kt          # API de tokens especiais
│       │   └── internal/                 # tudo aqui é `internal`
│       │       ├── PreTokenizer.kt
│       │       ├── Persistence.kt
│       │       ├── ByteUtils.kt
│       │       └── Gpt4Pattern.kt
│       └── test/kotlin/dev/tessera/
│           ├── BpeTokenizerTest.kt
│           ├── PreTokenizerTest.kt
│           ├── RoundTripTest.kt
│           ├── PersistenceTest.kt
│           ├── ApiContractTest.kt        # garante API estável
│           └── ComparisonTest.kt
│
├── tessera-cli/                      # aplicação CLI consumindo a lib
│   ├── build.gradle.kts              # depende de tessera-core
│   ├── README.md
│   └── src/main/kotlin/dev/tessera/cli/
│       ├── Main.kt
│       ├── TrainCommand.kt
│       ├── EncodeCommand.kt
│       ├── DecodeCommand.kt
│       └── InspectCommand.kt
│
└── tessera-samples/                  # exemplos de uso da lib
    ├── build.gradle.kts              # depende de tessera-core
    ├── README.md
    └── src/main/kotlin/dev/tessera/samples/
        ├── QuickStartSample.kt       # encode/decode básico
        ├── TrainingSample.kt         # treinar do zero
        ├── SpecialTokensSample.kt    # uso de tokens especiais
        └── PersistenceSample.kt      # save/load
```

### 4.4. API pública (do módulo `tessera-core`)

**Esta é a única superfície que outros projetos vão tocar. Pensa com carinho.**

```kotlin
package dev.tessera

/**
 * A trained BPE tokenizer.
 *
 * Use [Trainer] to train a new tokenizer from a corpus, or [BpeTokenizer.load]
 * to load one from disk.
 */
public class BpeTokenizer internal constructor(
    private val merges: Map<Pair<Int, Int>, Int>,
    private val vocab: Map<Int, ByteArray>,
    private val specialTokens: SpecialTokens
) {
    public val vocabSize: Int

    public fun encode(
        text: String,
        allowedSpecialTokens: Set<String> = emptySet()
    ): IntArray

    public fun decode(ids: IntArray): String

    public fun tokenAsString(id: Int): String
    public fun tokenAsBytes(id: Int): ByteArray

    public fun save(path: String)
    public fun save(file: java.io.File)

    public companion object {
        public fun load(path: String): BpeTokenizer
        public fun load(file: java.io.File): BpeTokenizer
    }
}

/**
 * Trains a new [BpeTokenizer] from a text corpus.
 */
public class Trainer(public val config: TrainingConfig = TrainingConfig()) {
    public fun train(corpus: String): BpeTokenizer
    public fun trainFromFile(path: String): BpeTokenizer
    public fun trainFromFile(file: java.io.File): BpeTokenizer
}

/**
 * Configuration for training.
 */
public data class TrainingConfig(
    val numMerges: Int = 5000,
    val specialTokens: SpecialTokens = SpecialTokens.default(),
    val verbose: Boolean = true,
    val progressCallback: ((TrainingProgress) -> Unit)? = null
)

public data class TrainingProgress(
    val mergesCompleted: Int,
    val totalMerges: Int,
    val lastMergeTokens: Pair<String, String>?
)

/**
 * Special tokens that should never be split by BPE.
 */
public class SpecialTokens(public val tokens: Map<String, Int>) {
    public companion object {
        public fun default(): SpecialTokens   // só <|endoftext|>
        public fun of(vararg names: String): SpecialTokens
    }
}
```

Exemplo de uso (deve funcionar como sample):

```kotlin
import dev.tessera.BpeTokenizer
import dev.tessera.Trainer
import dev.tessera.TrainingConfig

fun main() {
    // Treinar
    val tokenizer = Trainer(TrainingConfig(numMerges = 5000))
        .trainFromFile("corpus/text.txt")

    // Salvar
    tokenizer.save("tessera.json")

    // Carregar e usar
    val loaded = BpeTokenizer.load("tessera.json")
    val ids = loaded.encode("Olá, mundo!")
    val text = loaded.decode(ids)
    println("$ids → $text")
}
```

### 4.5. Regex de pré-tokenização (cl100k_base do GPT-4)

**USE EXATAMENTE ESSA STRING** (já validada):

```
(?i:'s|'t|'re|'ve|'m|'ll|'d)|[^\r\n\p{L}\p{N}]?\p{L}+|\p{N}{1,3}| ?[^\s\p{L}\p{N}]+[\r\n]*|\s*[\r\n]+|\s+(?!\S)|\s+
```

Em Kotlin, com raw string:

```kotlin
internal val GPT4_PATTERN = Regex(
    """(?i:'s|'t|'re|'ve|'m|'ll|'d)|[^\r\n\p{L}\p{N}]?\p{L}+|\p{N}{1,3}| ?[^\s\p{L}\p{N}]+[\r\n]*|\s*[\r\n]+|\s+(?!\S)|\s+"""
)
```

### 4.6. Formato de persistência (JSON)

```json
{
  "version": 1,
  "name": "tessera",
  "specialTokens": {
    "<|endoftext|>": 256
  },
  "merges": [
    {"a": 116, "b": 104, "id": 257},
    {"a": 257, "b": 101, "id": 258}
  ]
}
```

Note: `vocab` é reconstruível a partir de `merges` (bytes base 0-255 + merges em ordem). Não precisa serializar.

### 4.7. Tokens especiais

- `<|endoftext|>` é o único default (ID = 256, antes do primeiro merge)
- Tokens especiais **nunca** passam pelo BPE — são detectados antes da pré-tokenização e injetados diretamente como seus IDs
- API: `encode(text, allowedSpecialTokens = setOf("<|endoftext|>"))` — comportamento default é tratá-los como texto comum (segurança contra injection)

### 4.8. `.gitignore`

O arquivo `.gitignore` na raiz do repositório **deve ser exatamente este** (cobre Kotlin/Gradle multi-módulo + os artefatos específicos do projeto):

```gitignore
# ===== Gradle =====
.gradle/
build/
!gradle/wrapper/gradle-wrapper.jar
!**/src/**/build/
gradle-app.setting
.gradletasknamecache

# ===== IntelliJ IDEA / Android Studio =====
.idea/
*.iml
*.ipr
*.iws
out/

# ===== VS Code =====
.vscode/
*.code-workspace

# ===== Eclipse =====
.classpath
.project
.settings/
bin/

# ===== macOS =====
.DS_Store
.AppleDouble
.LSOverride

# ===== Windows =====
Thumbs.db
Thumbs.db:encryptable
ehthumbs.db
Desktop.ini
$RECYCLE.BIN/

# ===== Linux =====
*~
.directory
.Trash-*

# ===== Kotlin / JVM =====
*.class
*.jar
*.war
*.ear
*.nar
hs_err_pid*
replay_pid*

# ===== Logs e temporários =====
*.log
*.tmp
*.bak
*.swp
*.swo

# ===== Específicos do projeto Tessera =====

# Corpus de treino — nunca commitar (são GB de dados, baixáveis externamente)
/corpus/*
!/corpus/.gitkeep

# Tokenizers treinados — artefatos gerados, não fonte
*.tessera.json
/tessera.json
/tessera-*.json
!/tessera-core/src/test/resources/**/*.json

# Benchmarks e relatórios gerados localmente
/benchmark-results/
/coverage/

# Credenciais de publicação (NUNCA commitar)
local.properties
gradle.properties.local
publish.gradle.properties
secring.gpg
*.gpg
**/secrets.properties

# Dependency caches
.kotlin/
kotlin-js-store/
```

#### Explicações importantes

- **`/corpus/*` + `!/corpus/.gitkeep`**: corpus de treino são datasets externos (Wikipedia dumps, livros). Não fazem parte do repositório. O `.gitkeep` preserva a estrutura da pasta.
- **`*.tessera.json` + paths específicos**: tokenizers treinados são artefatos gerados, não fonte. Exceção: arquivos em `src/test/resources/` (fixtures de teste pequenos) ficam commitados.
- **Credenciais (`local.properties`, `*.gpg`, `secrets.properties`)**: credenciais pra publicação no JitPack/Maven Central/GitHub Packages **nunca** podem ir pro repo. Se a Fase 5 exigir secrets, eles ficam em variáveis de ambiente ou em arquivos ignorados.
- **`gradle-wrapper.jar`** é **commitado** (`!gradle/wrapper/gradle-wrapper.jar`). É a única exceção dentro de `.gradle/`. Sem ele, ninguém consegue rodar `./gradlew`.
- **`build/`** sempre ignorado, mas `!**/src/**/build/` preserva eventuais diretórios `build` que sejam parte de código-fonte (raro, mas a regra é defensiva).

#### Convenção

Se durante o desenvolvimento aparecer um novo tipo de artefato gerado (cache, log, dump de profiling), **adicionar ao `.gitignore` no mesmo commit** que introduz a feature que o gera. Não deixar pra "depois" — esquecer e commitar binários grandes é doloroso de reverter.

---

### 4.9. Configuração de publicação (módulo `tessera-core`)

O `build.gradle.kts` de `tessera-core` deve aplicar `maven-publish` e configurar:

```kotlin
plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
    `java-library`
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "dev.tessera"
            artifactId = "tessera-core"
            version = project.version.toString()
            from(components["java"])
            pom {
                name.set("Tessera Core")
                description.set("A byte-level BPE tokenizer library in pure Kotlin.")
                url.set("https://github.com/SEU_USUARIO/tessera")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
            }
        }
    }
}
```

A versão fica em `gradle.properties` no root:

```properties
version=0.1.0-SNAPSHOT
```

---

## 5. Plano de Implementação em Fases

### Fase 0 — Setup multi-módulo (estimativa: 2-3h)

**Objetivo:** Projeto compilando, com a estrutura multi-módulo correta, testes rodando.

- [ ] `gradle init` com Kotlin DSL
- [ ] Configurar `settings.gradle.kts` declarando os 3 módulos
- [ ] `build.gradle.kts` raiz com config compartilhada (Kotlin 2.0, JVM 17)
- [ ] Criar `tessera-core/build.gradle.kts` com `maven-publish` + `java-library`
- [ ] Criar `tessera-cli/build.gradle.kts` (depende de `tessera-core`)
- [ ] Criar `tessera-samples/build.gradle.kts` (depende de `tessera-core`)
- [ ] `gradle.properties` com versão `0.1.0-SNAPSHOT`
- [ ] `.gitignore` **exatamente conforme seção 4.8** (não inventar variações)
- [ ] README inicial focado em **uso como biblioteca**
- [ ] Estrutura de pacotes criada (arquivos placeholder)
- [ ] Um teste "hello world" passando em `tessera-core`
- [ ] `CHANGELOG.md` inicial

**Critério de saída:** `./gradlew build` roda sem erro em todos os módulos. `./gradlew publishToMavenLocal` instala `tessera-core` no Maven local com sucesso.

### Fase 1 — Core da lib (estimativa: 2-3 dias)

**Objetivo:** API pública estável do `tessera-core`, com round-trip garantido.

- [ ] `internal/ByteUtils.kt`: `stringToBytes` / `bytesToString` com tratamento UTF-8
- [ ] `internal/Gpt4Pattern.kt`: regex constante
- [ ] `internal/PreTokenizer.kt`: aplicar regex e retornar chunks
- [ ] `SpecialTokens.kt`: classe pública configurável
- [ ] `BpeTokenizer.kt`: encode/decode, vocab base 0-255
- [ ] `TrainingConfig.kt`: data class com configs
- [ ] `Trainer.kt`: API de treino retornando `BpeTokenizer`
- [ ] `internal/Persistence.kt`: save/load JSON
- [ ] **API pública revisada** — tudo que é público deve ter `public` explícito e KDoc

**Testes obrigatórios:**

- [ ] Round-trip pra ASCII puro
- [ ] Round-trip pra UTF-8 (acentos, emojis, CJK)
- [ ] Treinar com corpus pequeno (10KB)
- [ ] Save → load → encode produz mesmo resultado
- [ ] `ApiContractTest`: testa que a API pública compila e funciona como esperada

**Critério de saída:** Round-trip de 100 strings aleatórias passando. API pública completa e documentada.

### Fase 2 — Samples (estimativa: meio dia)

**Objetivo:** Provar que a biblioteca é consumível por código externo.

- [ ] `QuickStartSample.kt`: encode/decode mais simples possível
- [ ] `TrainingSample.kt`: treino básico
- [ ] `SpecialTokensSample.kt`: uso de tokens especiais
- [ ] `PersistenceSample.kt`: save + load round-trip
- [ ] Cada sample com seu próprio `main()` rodável
- [ ] `tessera-samples/README.md` listando os samples

**Critério de saída:** `./gradlew :tessera-samples:run -PmainClass=...` executa qualquer sample. Os samples só usam classes `public` do `tessera-core` (sem hacks com `internal`).

### Fase 3 — CLI (estimativa: meio dia)

**Objetivo:** Aplicação CLI demonstrando uso da lib em contexto real.

```bash
./gradlew :tessera-cli:run --args="train --corpus corpus/text.txt --merges 5000 --output tessera.json"
./gradlew :tessera-cli:run --args="encode --tokenizer tessera.json --text 'Olá, mundo!'"
./gradlew :tessera-cli:run --args="decode --tokenizer tessera.json --ids '156,234,89'"
./gradlew :tessera-cli:run --args="inspect --tokenizer tessera.json"
```

- [ ] Parser de argumentos (manual ou `kotlinx-cli`)
- [ ] Comandos `train`, `encode`, `decode`, `inspect`
- [ ] Distribuição via `application` plugin (`./gradlew :tessera-cli:installDist`)

**Critério de saída:** Todos os comandos funcionam. O CLI **apenas chama a API pública** do `tessera-core` — nenhum acesso a internals.

### Fase 4 — Validação e qualidade (estimativa: 1-2 dias)

**Objetivo:** Garantir qualidade comparável a tokenizadores reais.

- [ ] Baixar corpus pt-br + en
- [ ] Treinar com 5.000, 10.000 e 30.000 merges
- [ ] Comparar granularidade contra `tiktoken cl100k_base` em 100+ frases
- [ ] Suite de fuzz testing: 1000+ strings UTF-8 aleatórias, round-trip
- [ ] Documentar resultados em `BENCHMARKS.md`
- [ ] Cobertura de testes ≥ 80% (medir com `kover` ou similar)

**Critério de saída:** Todos os critérios da seção 3.2 atendidos.

### Fase 5 — Publicação e polish (estimativa: 1 dia)

**Objetivo:** Projeto entregável e consumível.

- [ ] README principal: focado em **como usar como biblioteca** (instalação via Gradle, exemplo mínimo, links pros samples)
- [ ] `tessera-core/README.md`: docs específicas do módulo lib
- [ ] `ARCHITECTURE.md` explicando como o BPE funciona
- [ ] KDoc completo em toda API pública (gerar com Dokka — opcional)
- [ ] Publicação no JitPack: criar tag git, validar que `https://jitpack.io/#SEU_USUARIO/tessera` mostra o build verde
- [ ] Adicionar badge JitPack no README
- [ ] Zero warnings
- [ ] Tag `v1.0.0` no git
- [ ] `CHANGELOG.md` finalizado para v1.0.0

**Critério de saída:** Outra pessoa consegue adicionar `tessera-core` como dependência num projeto Kotlin novo e usar a API em < 5 minutos seguindo só o README.

Snippet que deve funcionar pra terceiros após publicação no JitPack:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}

// build.gradle.kts
dependencies {
    implementation("com.github.SEU_USUARIO:tessera:tessera-core-v1.0.0")
}
```

---

## 6. Armadilhas Conhecidas (LER ANTES DE CODAR)

### 6.1. UTF-16 vs UTF-8

Kotlin `String` é UTF-16 internamente. Mas BPE byte-level opera em UTF-8. **Toda conversão deve ser explícita** via `.toByteArray(Charsets.UTF_8)`. Nunca itere caracteres com `.toChar()` para BPE.

### 6.2. Byte signed vs unsigned

Kotlin `Byte` é signed (-128 a 127). Pra trabalhar com bytes como inteiros 0-255, **sempre** use `.toUByte().toInt()`. Esquecer disso causa IDs negativos e bugs sutis.

### 6.3. `maxBy` deprecated

Use `maxByOrNull` em Kotlin moderno.

### 6.4. Performance no encode

`getStats` no encode é desperdício. Use a versão otimizada:

```kotlin
var bestPair: Pair<Int, Int>? = null
var bestRank = Int.MAX_VALUE
for (i in 0 until ids.size - 1) {
    val pair = ids[i] to ids[i + 1]
    val rank = merges[pair] ?: continue
    if (rank < bestRank) {
        bestRank = rank
        bestPair = pair
    }
}
```

### 6.5. Merge entre chunks

Use ID sentinela negativo (`-1`) entre chunks no treino. **Garanta** que `getStats` filtra pares envolvendo `-1`.

### 6.6. Encode "greedy" não é frequência

No encode, o critério é **menor rank**, não mais frequente.

### 6.7. `List<Int>` vs `IntArray`

Comece com `MutableList<Int>` por simplicidade, meça, e migre pra `IntArray` se for gargalo.

### 6.8. Tokens especiais ≠ texto comum

`<|endoftext|>` **NUNCA** pode ser tokenizado pelos seus bytes. Detecte-o antes da pré-tokenização.

### 6.9. Determinismo do treino

Em empate de frequência, use desempate determinístico (ordem lexicográfica dos IDs).

### 6.10. Encoding de arquivos

Sempre especifique encoding: `File(path).readText(Charsets.UTF_8)`.

### 6.11. ⚠️ Específicas de biblioteca (NOVAS)

#### Visibilidade

Tudo que não é parte da API pública deve ser `internal`. **Não use `public` por padrão.** Use `public` explícito só onde a API pública exige. Configure `explicitApi()` no `tessera-core/build.gradle.kts`:

```kotlin
kotlin {
    explicitApi()
}
```

Isso força o compilador a exigir modificador de visibilidade explícito em todo símbolo no módulo. Pega esquecimentos.

#### Estabilidade de API

A partir da v1.0.0, mudanças que quebram a API pública requerem bump de major version. Pra evitar arrependimento, mantenha a API **pequena**. É melhor uma API minimalista que cresce do que uma grande que precisa ser depreciada.

#### Dependências runtime

`tessera-core` **não deve** ter dependências runtime além de `kotlin-stdlib` e `kotlinx-serialization-json`. **Em particular**, dependências de teste (Kotest/JUnit) precisam estar em `testImplementation`, não `implementation`. Verifique com `./gradlew :tessera-core:dependencies --configuration runtimeClasspath`.

#### Vazamento de tipos internos na API

Se uma classe `internal` aparece numa assinatura `public`, o compilador reclama. Se você usa `MutableList`, `MutableMap`, etc. em retornos públicos, prefira tipos imutáveis (`List`, `Map`) pra não vazar mutabilidade.

#### Configuração do `publishToMavenLocal`

Sempre que mudar API pública significativa, rode `./gradlew publishToMavenLocal` e teste num projeto externo (`tessera-samples` serve). Isso pega problemas de empacotamento cedo.

---

## 7. Recursos e Referências

### 7.1. Referências canônicas

- **minbpe** (Andrej Karpathy): https://github.com/karpathy/minbpe — **leia antes de começar**
- **tiktoken** (OpenAI): https://github.com/openai/tiktoken
- **Visualizador online:** tiktokenizer.vercel.app
- **Paper original BPE:** Sennrich et al. 2016
- **GPT-2 paper** (byte-level BPE): Radford et al. 2019

### 7.2. Referências sobre design de bibliotecas Kotlin

- **Kotlin API guidelines:** https://kotlinlang.org/docs/api-guidelines-introduction.html
- **`explicitApi` mode:** https://kotlinlang.org/docs/whatsnew14.html#explicit-api-mode-for-library-authors
- **JitPack guide:** https://jitpack.io/docs/BUILDING/#gradle-projects
- **Multi-module Gradle:** https://docs.gradle.org/current/userguide/multi_project_builds.html

### 7.3. Corpus sugeridos para treino

- **Project Gutenberg (PT-BR):** Os Lusíadas, Machado de Assis, Eça de Queirós
- **Wikipedia PT-BR dump:** https://dumps.wikimedia.org/ptwiki/
- **OSCAR corpus (pt):** https://oscar-corpus.com/
- **Tatoeba sentences (pt)**

---

## 8. Workflow com Claude Code

### 8.1. Como você (Claude Code) deve operar

1. **Leia este PRD inteiro antes de qualquer ação.**
2. Confirme que entendeu o escopo e as decisões já tomadas. Em particular, confirme entendimento sobre Tessera ser uma **biblioteca**, não uma aplicação.
3. Trabalhe **fase por fase**. Não pule fases.
4. Ao começar uma fase, mostre o plano específico antes de codar.
5. Commite frequentemente, mensagens descritivas. Cada subtarefa = 1 commit mínimo.
6. Rode os testes após cada mudança significativa.
7. Ao terminar uma fase, mostre os critérios de saída atingidos antes de prosseguir.
8. Sempre que mudar a API pública do `tessera-core`, rode `publishToMavenLocal` e teste em `tessera-samples`.
9. Se encontrar decisão não coberta pelo PRD, pergunte.

### 8.2. Convenções de código

- `explicitApi()` ativo no módulo core — todo símbolo precisa de visibilidade explícita
- Imutabilidade por padrão (`val`, não `var`)
- Mutação só onde necessária por performance (treino do BPE)
- Funções pequenas (< 30 linhas idealmente)
- Nomes claros, em inglês
- Comentários explicando **por que**, não **o que**
- KDoc em **toda** API pública (obrigatório, não opcional)

### 8.3. Convenções de git

- Branch principal: `main`
- Feature branches: `feat/nome-curto`
- Mensagens em inglês, formato Conventional Commits:
  - `feat(core): add byte-level encoder`
  - `feat(cli): add inspect command`
  - `feat(samples): add quickstart sample`
  - `fix(core): handle empty corpus in trainer`
  - `test(core): add round-trip fuzz tests`
  - `docs: update README with installation instructions`
  - `refactor(core): mark internal classes as internal`
  - `build: configure maven-publish for jitpack`

### 8.4. Versionamento

- `0.1.0-SNAPSHOT` → Fase 0
- `0.x.y` → fases 1-4 (API ainda instável)
- `0.9.0` → fim da Fase 4 (preparação pra release)
- `1.0.0` → fim da Fase 5 (release pública, API estável)

Após v1.0.0, mudanças breaking exigem bump de major.

---

## 9. Comunicação e Bloqueios

### 9.1. Quando perguntar ao usuário (eu)

- Decisões fora do escopo do PRD
- Trade-offs significativos de design (especialmente em API pública)
- Resultados de benchmarks
- Ao final de cada fase

### 9.2. Quando NÃO perguntar

- Detalhes de implementação cobertos pelo PRD
- Escolhas estéticas de código
- Quais testes adicionar

### 9.3. Status report ideal ao final de cada fase

```
✅ Fase X concluída.

Implementado:
- item 1
- item 2

API pública mudou? Sim/Não. Se sim:
- diff resumido

Testes adicionados:
- N testes, todos passando

Critérios de saída:
- [x] critério A
- [x] critério B

Próximos passos: iniciando Fase Y. Posso prosseguir?
```

---

## 10. Apêndice — Glossário

- **Tessera:** peça individual de um mosaico. Aqui, sinônimo de "token". O nome do projeto.
- **BPE (Byte-Pair Encoding):** algoritmo que parte de bytes e funde os pares mais frequentes iterativamente.
- **Vocab:** mapeamento `id → bytes`.
- **Merges:** mapeamento `(id1, id2) → newId`, na ordem em que foram aprendidos.
- **Pré-tokenização:** quebrar o texto em chunks via regex antes do BPE.
- **Round-trip:** propriedade `decode(encode(x)) == x`.
- **Granularidade:** tokens por palavra.
- **API pública:** símbolos `public` em `tessera-core`, consumíveis por terceiros.
- **`internal`:** símbolos visíveis apenas dentro do módulo — não fazem parte da API pública.
- **Multi-módulo Gradle:** projeto com sub-módulos independentes que se compõem.

---

## 11. Checklist mestre

- [ ] Fase 0: Setup multi-módulo completo
- [ ] Fase 1: Core lib funcional com round-trip e API pública estável
- [ ] Fase 2: Samples consumindo a lib
- [ ] Fase 3: CLI consumindo a lib
- [ ] Fase 4: Validação contra tiktoken
- [ ] Fase 5: Publicação no JitPack e polish
- [ ] Todos os critérios da seção 3 atingidos
- [ ] README focado em uso como biblioteca
- [ ] Tag `v1.0.0` criada
- [ ] JitPack badge verde

**Quando esse checklist estiver completo, Tessera está pronta como biblioteca pública. O segundo codebase (embeddings) pode começar, importando `tessera-core` como dependência Gradle.**

---

## 📜 Sobre o nome

> *"Tessera"* — do latim *tessera*, peça quadrada usada em mosaicos romanos. Cada tessera é insignificante isoladamente, mas o conjunto forma a imagem completa.
>
> Da mesma forma, cada token produzido por esta biblioteca é uma peça aparentemente arbitrária (um pedaço de bytes), mas o conjunto representa toda a riqueza da linguagem humana, pronta para alimentar um modelo de linguagem.
