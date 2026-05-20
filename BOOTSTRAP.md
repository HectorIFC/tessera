# Guia de Bootstrap — Tessera

Este guia descreve os passos que **você precisa executar na sua máquina** para colocar o projeto Tessera no ar e começar a desenvolver com o Claude Code.

---

## Passo 1 — Criar o repositório no GitHub

Você tem duas opções:

### Opção A — Via web (mais simples)

1. Acesse https://github.com/new
2. Preencha:
   - **Repository name:** `tessera`
   - **Description:** `A byte-level BPE tokenizer in pure Kotlin.`
   - **Visibility:** Public (recomendado) ou Private (sua escolha)
   - **NÃO** marque "Add a README" (vamos colocar o nosso)
   - **NÃO** adicione `.gitignore` nem licença ainda
3. Clique em **Create repository**
4. Copie a URL do repositório (algo como `https://github.com/SEU_USUARIO/tessera.git`)

### Opção B — Via GitHub CLI

Se você tem o `gh` CLI instalado e autenticado:

```bash
gh repo create tessera \
  --public \
  --description "A byte-level BPE tokenizer in pure Kotlin." \
  --clone
```

(Substitua `--public` por `--private` se preferir.)

Isso já cria e clona em uma única operação. Pule pro Passo 3.

---

## Passo 2 — Clonar localmente

Escolha onde quer guardar o projeto (ex: `~/projects/`):

```bash
cd ~/projects
git clone https://github.com/SEU_USUARIO/tessera.git
cd tessera
```

---

## Passo 3 — Adicionar os arquivos iniciais

Copie os dois arquivos que o Claude (chat) te entregou pra dentro do repositório:

```bash
# Dentro do diretório tessera/
cp /caminho/onde/voce/salvou/PRD.md ./PRD.md
cp /caminho/onde/voce/salvou/README.md ./README.md
```

Faça o primeiro commit:

```bash
git add PRD.md README.md
git commit -m "docs: add initial PRD and README"
git push origin main
```

---

## Passo 4 — Abrir o Claude Code no projeto

Pré-requisitos:
- Node.js 18+ instalado
- Claude Code instalado (se não tem: `npm install -g @anthropic-ai/claude-code`)

Dentro do diretório do projeto:

```bash
cd ~/projects/tessera
claude
```

---

## Passo 5 — Primeira mensagem ao Claude Code

Cole isso como sua primeira mensagem no Claude Code:

> Olá! Este é o projeto **Tessera**, um tokenizador BPE byte-level em Kotlin puro.
>
> Antes de qualquer ação:
>
> 1. Leia o arquivo `PRD.md` por completo. Ele contém o escopo, decisões arquiteturais já tomadas, plano de fases, critérios de aceitação e armadilhas conhecidas.
> 2. Leia também o `README.md` para contexto adicional.
> 3. Quando terminar, me apresente um **resumo do seu entendimento** do projeto, destacando: o que vamos construir, as decisões arquiteturais que você NÃO deve re-debater, e qual é a Fase 0 que vamos iniciar.
> 4. Aguarde minha confirmação antes de começar a implementar.
>
> Importante: siga estritamente as convenções definidas no PRD (commits em inglês com prefixo `feat:`, `fix:`, etc; uma fase por vez; status report ao final de cada fase).

O Claude Code vai ler os dois documentos, te apresentar o entendimento e perguntar antes de seguir. A partir daí o desenvolvimento flui.

---

## Passo 6 — Durante o desenvolvimento

Lembrete dos pontos-chave:

- **Trabalhe uma fase por vez.** Não deixe o Claude Code pular fases.
- **Revise os commits.** Periodicamente faça `git log --oneline` pra acompanhar o progresso.
- **Rode os testes.** Após cada fase, rode `./gradlew test` pra confirmar que tudo passa.
- **Tire dúvidas.** Se algo não fizer sentido ou parecer fugir do escopo, pergunte.

---

## Estrutura final esperada do repositório (após Fase 0)

```
tessera/
├── PRD.md
├── README.md
├── .gitignore
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/
│   └── wrapper/
├── gradlew
├── gradlew.bat
├── corpus/
│   └── .gitkeep
└── src/
    ├── main/kotlin/dev/tessera/
    │   └── (arquivos vazios ou esqueletos)
    └── test/kotlin/dev/tessera/
        └── HelloWorldTest.kt
```

---

## Checklist pessoal antes de começar

- [ ] Repositório criado no GitHub
- [ ] Repositório clonado localmente
- [ ] `PRD.md` no root do projeto
- [ ] `README.md` no root do projeto
- [ ] Primeiro commit feito e pushado
- [ ] Claude Code instalado
- [ ] Claude Code aberto no diretório do projeto
- [ ] Primeira mensagem enviada

Quando todas as caixas estiverem marcadas, você está pronto pra começar a Fase 0.

Boa sorte! 🎨🧩
