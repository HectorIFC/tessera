# Bootstrap Guide — Tessera

This guide describes the steps you need to run on your machine to get the Tessera project up and running and start developing with Claude Code.

---

## Step 1 — Create the GitHub repository

You have two options:

### Option A — Via web (simpler)

1. Go to https://github.com/new
2. Fill in:
   - **Repository name:** `tessera`
   - **Description:** `A byte-level BPE tokenizer in pure Kotlin.`
   - **Visibility:** Public (recommended) or Private (your choice)
   - **Do NOT** check "Add a README" (we will add our own)
   - **Do NOT** add a `.gitignore` or a license yet
3. Click **Create repository**
4. Copy the repository URL (something like `https://github.com/YOUR_USERNAME/tessera.git`)

### Option B — Via GitHub CLI

If you have the `gh` CLI installed and authenticated:

```bash
gh repo create tessera \
  --public \
  --description "A byte-level BPE tokenizer in pure Kotlin." \
  --clone
```

(Replace `--public` with `--private` if preferred.)

This creates and clones in a single operation. Skip to Step 3.

---

## Step 2 — Clone locally

Choose where you want to store the project (e.g. `~/projects/`):

```bash
cd ~/projects
git clone https://github.com/YOUR_USERNAME/tessera.git
cd tessera
```

---

## Step 3 — Add the initial files

Copy the two files delivered by Claude (chat) into the repository:

```bash
# Inside the tessera/ directory
cp /path/where/you/saved/PRD.md ./PRD.md
cp /path/where/you/saved/README.md ./README.md
```

Make the first commit:

```bash
git add PRD.md README.md
git commit -m "docs: add initial PRD and README"
git push origin main
```

---

## Step 4 — Open Claude Code in the project

Prerequisites:
- Node.js 18+ installed
- Claude Code installed (if not: `npm install -g @anthropic-ai/claude-code`)

Inside the project directory:

```bash
cd ~/projects/tessera
claude
```

---

## Step 5 — First message to Claude Code

Paste this as your first message in Claude Code:

> Hello! This is the **Tessera** project, a byte-level BPE tokenizer in pure Kotlin.
>
> Before any action:
>
> 1. Read the `PRD.md` file in full. It contains the scope, architectural decisions already made, phase plan, acceptance criteria, and known pitfalls.
> 2. Also read `README.md` for additional context.
> 3. When done, present a **summary of your understanding** of the project, highlighting: what we are building, the architectural decisions you must NOT re-debate, and which Phase 0 we will start.
> 4. Wait for my confirmation before starting to implement.
>
> Important: strictly follow the conventions defined in the PRD (commits in English with prefix `feat:`, `fix:`, etc.; one phase at a time; status report at the end of each phase).

Claude Code will read both documents, present its understanding, and ask before proceeding. From there, development flows.

---

## Step 6 — During development

Key reminders:

- **Work one phase at a time.** Do not let Claude Code skip phases.
- **Review commits.** Periodically run `git log --oneline` to track progress.
- **Run tests.** After each phase, run `./gradlew test` to confirm everything passes.
- **Ask questions.** If something does not make sense or seems out of scope, ask.

---

## Expected repository structure (after Phase 0)

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
    │   └── (empty files or skeletons)
    └── test/kotlin/dev/tessera/
        └── HelloWorldTest.kt
```

---

## Personal checklist before starting

- [ ] Repository created on GitHub
- [ ] Repository cloned locally
- [ ] `PRD.md` in the project root
- [ ] `README.md` in the project root
- [ ] First commit made and pushed
- [ ] Claude Code installed
- [ ] Claude Code opened in the project directory
- [ ] First message sent

When all boxes are checked, you are ready to start Phase 0.

Good luck! 🎨🧩
