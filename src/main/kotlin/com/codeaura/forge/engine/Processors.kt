package com.codeaura.forge.engine

interface CodeProcessor {
    fun process(code: String, fileExt: String = ""): String
}

class CommentRemover : CodeProcessor {
    override fun process(code: String, fileExt: String): String {
        var result = code
        when (fileExt) {
            "html", "xml", "svg" -> {
                result = result.replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "")
            }
            "css" -> {
                result = result.replace(Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL), "")
            }
            "py", "rb", "sh", "bash", "zsh", "yaml", "yml", "toml" -> {
                result = result.replace(Regex("(?m)#(?!!).*$"), "")
            }
            "sql" -> {
                result = result
                    .replace(Regex("(?m)--.*$"), "")
                    .replace(Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL), "")
            }
            "json" -> result  // JSON has no comments
            else -> {
                // JS, TS, Java, Kotlin, C, C++, Go, Rust, Scala, PHP, Swift
                result = result
                    .replace(Regex("//[^\n]*"), "")
                    .replace(Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL), "")
            }
        }
        return result.trimLines()
    }
}

class ImportCleaner : CodeProcessor {
    override fun process(code: String, fileExt: String): String {
        val lines = code.lines()
        val importLines    = lines.filter { it.trimStart().startsWith("import ") }
        val nonImportLines = lines.filter { !it.trimStart().startsWith("import ") }
        val uniqueImports  = importLines.distinct().sorted()
        return (uniqueImports + nonImportLines).joinToString("\n")
    }
}

class Formatter : CodeProcessor {
    override fun process(code: String, fileExt: String): String {
        return code
            .replace(Regex("\n{3,}"), "\n\n")
            .lines()
            .dropWhile { it.isBlank() }
            .joinToString("\n")
            .trimEnd()
    }
}

class BracketFixer : CodeProcessor {
    override fun process(code: String, fileExt: String): String {
        if (fileExt in listOf("json", "html", "xml", "css", "yaml", "yml")) return code
        return code
            .replace(Regex("\\)\\{"), ") {")
            .replace(Regex("([a-zA-Z0-9_])\\{"), "$1 {")
    }
}

class SortLines : CodeProcessor {
    override fun process(code: String, fileExt: String): String {
        val lines = code.lines()
        val sorted = lines.sortedWith(compareBy { it.trimStart() })
        return sorted.joinToString("\n")
    }
}

class TrimWhitespace : CodeProcessor {
    override fun process(code: String, fileExt: String): String {
        return code.lines()
            .joinToString("\n") { it.trimEnd() }
            .trimEnd()
    }
}

class ProcessingPipeline(private val processors: List<CodeProcessor>) {
    fun run(code: String, fileExt: String = ""): String =
        processors.fold(code) { acc, p -> p.process(acc, fileExt) }
}

private fun String.trimLines(): String =
    lines().map { it.trimEnd() }.joinToString("\n")
