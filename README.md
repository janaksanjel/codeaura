# ◈ CodeAura

> Smart Contextual Code Formatter — IntelliJ IDEA Plugin

---

## Features

| Feature | Shortcut |
|---|---|
| Format File | `Ctrl+Alt+F` |
| Remove Comments | `Ctrl+Alt+C` |
| Clean Imports | `Ctrl+Alt+I` |
| Sort Lines | `Ctrl+Alt+S` |
| Trim Whitespace | `Ctrl+Alt+T` |
| Toggle Sidebar | `Ctrl+Alt+B` |

- Per-file settings (not global)
- Sidebar auto-opens on file switch
- **Select code** to format selection only — no selection = full file
- Processing pipeline: Comment Remover → Import Cleaner → Formatter → Bracket Fixer → Sort Lines → Trim Whitespace
- Supports: JS, TS, HTML, CSS, JSON, Java, Kotlin, Python, Ruby, Shell, YAML, SQL, Go, Rust, PHP, Swift

---

## Processing Tools

| Tool | What it does |
|---|---|
| Remove Comments | Strips `//` `/* */` `<!-- -->` `#` `--` per file type |
| Clean Imports | Deduplicates and sorts import statements |
| Auto Format | Collapses 3+ blank lines to 1, trims end |
| Fix Brackets | Ensures space before `{` |
| Sort Lines | Sorts selected or all lines A→Z |
| Trim Whitespace | Removes trailing spaces from every line |

---

## Build & Run

**Requirements:** JDK 17+, IntelliJ IDEA

```bash
# Run plugin in sandbox IDE
./gradlew runIde

# Build distributable ZIP
./gradlew buildPlugin
```

Output: `build/distributions/codeaura-forge-1.0.0.zip`

---

## Project Structure

```
src/main/kotlin/com/codeaura/forge/
├── engine/
│   ├── Processors.kt         # CommentRemover, ImportCleaner, Formatter,
│   │                         # BracketFixer, SortLines, TrimWhitespace
│   └── PipelineRunner.kt     # Runs pipeline on selection or full file
├── settings/
│   ├── FileConfig.kt         # Per-file config data class
│   └── FileConfigRegistry.kt # Application-level registry
├── ui/
│   ├── CodeAuraPanel.kt      # Sidebar UI with checkboxes + shortcuts
│   └── CodeAuraToolWindowFactory.kt
├── actions/
│   ├── FormatFileAction.kt
│   ├── RemoveCommentsAction.kt
│   ├── CleanImportsAction.kt
│   ├── SortLinesAction.kt
│   ├── TrimWhitespaceAction.kt
│   └── ToggleSidebarAction.kt
└── listeners/
    └── FileOpenListener.kt   # Auto-opens sidebar on file switch
```

---

## How Selection Works

- **No selection** → processes the entire file
- **Text selected** → processes only the selected code

---

## Logo

Located at `src/main/resources/icons/codeaura.svg`
