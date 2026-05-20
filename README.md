# Tessera

> A byte-level BPE tokenizer **library** in pure Kotlin.
>
> *Tessera*, do latim, é uma peça de mosaico. Cada token é uma tessera; juntos, formam o mosaico da linguagem.

## Status

🚧 **Em desenvolvimento** — Fase 0 (Setup multi-módulo)

Veja [PRD.md](./PRD.md) para a especificação completa.

## Sobre

Tessera é uma **biblioteca Kotlin** que implementa um tokenizador **Byte-Pair Encoding** (BPE) byte-level, no estilo do `cl100k_base` usado pelo GPT-4. Foi construída from-scratch em **Kotlin puro**, sem dependências de bibliotecas de ML, com o objetivo de aprender em profundidade como tokenizadores modernos funcionam e fornecer uma lib enxuta e legível pra projetos Kotlin/JVM.

### Princípios

- **Biblioteca, não aplicação** — destinada a ser consumida por outros projetos Kotlin
- **Kotlin puro** — sem DJL, sem KInference, sem frameworks de ML
- **Standard library only** para a lógica de tokenização
- **Byte-level** — vocab base de 256 bytes, suporta qualquer texto UTF-8
- **Compatível com a abordagem do GPT-4** — pré-tokenização com regex `cl100k_base`
- **API pública minimalista** — só o necessário, marcado explicitamente

## Instalação (após release v1.0.0)

### Gradle (Kotlin DSL)

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

## Uso básico

```kotlin
import dev.tessera.BpeTokenizer
import dev.tessera.Trainer
import dev.tessera.TrainingConfig

fun main() {
    // 1. Treinar um tokenizer a partir de um corpus
    val tokenizer = Trainer(TrainingConfig(numMerges = 5000))
        .trainFromFile("corpus/text.txt")

    // 2. Salvar pra reusar depois
    tokenizer.save("tessera.json")

    // 3. Carregar e usar
    val loaded = BpeTokenizer.load("tessera.json")
    val ids = loaded.encode("Olá, mundo!")
    val text = loaded.decode(ids)
    println("$ids → $text")
}
```

Mais exemplos no módulo [`tessera-samples`](./tessera-samples/).

## Estrutura do projeto

Este é um projeto **Gradle multi-módulo**:

```
tessera/
├── tessera-core/      ← A biblioteca (artefato publicado)
├── tessera-cli/       ← Aplicação CLI consumindo a lib
└── tessera-samples/   ← Exemplos de uso da lib
```

- **`tessera-core`**: o JAR consumível. API pública minimalista, sem dependências runtime além do Kotlin stdlib e kotlinx-serialization.
- **`tessera-cli`**: aplicação rodável (`./gradlew :tessera-cli:run`) que demonstra a lib em uso.
- **`tessera-samples`**: pequenos programas Kotlin com `main()` mostrando padrões de uso.

## Como rodar localmente

```bash
# Buildar tudo
./gradlew build

# Rodar os testes
./gradlew test

# Instalar a lib no Maven Local pra testar em outros projetos
./gradlew publishToMavenLocal

# Rodar o CLI
./gradlew :tessera-cli:run --args="train --corpus corpus/text.txt --merges 5000 --output tessera.json"

# Rodar um sample
./gradlew :tessera-samples:run -PmainClass=dev.tessera.samples.QuickStartSampleKt
```

## Arquitetura

Em alto nível:

1. **Pré-tokenização**: o texto passa por um regex (estilo GPT-4) que o divide em chunks lógicos (palavras, contrações, números, espaços, pontuação).
2. **Conversão pra bytes**: cada chunk vira uma sequência de bytes UTF-8 (0-255).
3. **BPE**: o algoritmo aprendido funde iterativamente os pares de tokens mais frequentes, criando tokens compostos.
4. **Encode greedy**: na hora de tokenizar texto novo, sempre aplica o merge com menor rank (aprendido primeiro), reproduzindo o comportamento do GPT.

Veja [ARCHITECTURE.md](./ARCHITECTURE.md) (criado na Fase 5) para detalhes técnicos.

## Roadmap

- [x] Definir escopo e arquitetura (ver PRD.md)
- [ ] **Fase 0**: Setup Gradle multi-módulo
- [ ] **Fase 1**: Core lib com round-trip e API pública estável
- [ ] **Fase 2**: Sample apps consumindo a lib
- [ ] **Fase 3**: CLI consumindo a lib
- [ ] **Fase 4**: Validação contra `tiktoken`
- [ ] **Fase 5**: Publicação no JitPack e polish

## Projeto irmão

Quando Tessera estiver pronta, o próximo passo é uma **codebase separada** para embeddings, que vai consumir `tessera-core` como dependência Gradle.

## Licença

MIT (a definir).
