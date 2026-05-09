package com.codeaura.forge

import com.codeaura.forge.engine.*
import com.codeaura.forge.settings.FileConfig
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class CodeAuraForgeTest {

    // ── CommentRemover ────────────────────────────────────────────────────────

    @Test
    fun `CommentRemover removes single-line comments`() {
        val result = CommentRemover().process("val x = 1 // comment\nval y = 2")
        assertFalse(result.contains("//"))
        assertTrue(result.contains("val x = 1"))
    }

    @Test
    fun `CommentRemover removes block comments`() {
        val result = CommentRemover().process("/* block */\nval x = 1")
        assertFalse(result.contains("/*"))
        assertTrue(result.contains("val x = 1"))
    }

    @Test
    fun `CommentRemover removes HTML comments`() {
        val result = CommentRemover().process("<!-- nav -->\n<div>Hello</div>")
        assertFalse(result.contains("<!--"))
        assertTrue(result.contains("<div>"))
    }

    // ── ImportCleaner ─────────────────────────────────────────────────────────

    @Test
    fun `ImportCleaner removes duplicate imports`() {
        val input = "import java.util.List\nimport java.util.List\nclass Foo {}"
        val result = ImportCleaner().process(input)
        val count = result.lines().count { it.trimStart().startsWith("import java.util.List") }
        assertEquals(1, count)
    }

    @Test
    fun `ImportCleaner sorts imports alphabetically`() {
        val input = "import z.Zoo\nimport a.Alpha\nimport m.Mid\nclass Bar {}"
        val result = ImportCleaner().process(input)
        val imports = result.lines().filter { it.trimStart().startsWith("import") }
        assertEquals(listOf("import a.Alpha", "import m.Mid", "import z.Zoo"), imports)
    }

    // ── Formatter ─────────────────────────────────────────────────────────────

    @Test
    fun `Formatter collapses multiple blank lines`() {
        val result = Formatter().process("val a = 1\n\n\n\nval b = 2")
        assertFalse(result.contains("\n\n\n"))
    }

    // ── BracketFixer ──────────────────────────────────────────────────────────

    @Test
    fun `BracketFixer adds space before opening brace`() {
        val result = BracketFixer().process("fun foo(){\n  val x = 1\n}")
        assertTrue(result.contains("foo() {"))
    }

    // ── Full pipeline ─────────────────────────────────────────────────────────

    @Test
    fun `Pipeline runs all processors in order`() {
        val input = "import a.A\nimport a.A\n// comment\nfun go(){\n}"
        val result = ProcessingPipeline(listOf(
            CommentRemover(), ImportCleaner(), Formatter(), BracketFixer()
        )).run(input)
        assertFalse(result.contains("//"))
        assertFalse(result.contains("\n\n\n"))
        assertTrue(result.contains("go() {"))
        assertEquals(1, result.lines().count { it.trimStart().startsWith("import a.A") })
    }

    // ── FileConfig defaults ───────────────────────────────────────────────────

    @Test
    fun `FileConfig defaults are all false with Dark theme`() {
        val cfg = FileConfig()
        assertFalse(cfg.removeComments)
        assertFalse(cfg.cleanImports)
        assertFalse(cfg.autoFormat)
        assertFalse(cfg.fixBrackets)
        assertEquals("Dark", cfg.theme)
    }

    // ── Demo file integration tests ───────────────────────────────────────────

    @Test
    fun `Pipeline reduces line count on demo JS file`() {
        val file = File("demo/js/app.js")
        org.junit.Assume.assumeTrue(file.exists())
        val original = file.readText()
        val result = fullPipeline().run(original)
        assertTrue(result.lines().size < original.lines().size)
    }

    @Test
    fun `Pipeline deduplicates imports in demo TS file`() {
        val file = File("demo/ts/user.service.ts")
        org.junit.Assume.assumeTrue(file.exists())
        val result = fullPipeline().run(file.readText())
        assertEquals(1, result.lines().count { it.contains("HttpClient") })
    }

    @Test
    fun `Pipeline removes comments from demo Java file`() {
        val file = File("demo/java/UserManager.java")
        org.junit.Assume.assumeTrue(file.exists())
        val result = fullPipeline().run(file.readText())
        assertFalse(result.contains("// "))
    }

    @Test
    fun `Pipeline fixes brackets in demo Kotlin file`() {
        val file = File("demo/kotlin/UserRepository.kt")
        org.junit.Assume.assumeTrue(file.exists())
        val result = fullPipeline().run(file.readText())
        assertFalse(result.contains("){"))
    }

    @Test
    fun `Pipeline removes HTML comments from demo HTML file`() {
        val file = File("demo/html/index.html")
        org.junit.Assume.assumeTrue(file.exists())
        val result = fullPipeline().run(file.readText())
        assertFalse(result.contains("<!--"))
        assertTrue(result.contains("<html"))
    }

    @Test
    fun `Pipeline removes CSS comments from demo CSS file`() {
        val file = File("demo/css/style.css")
        org.junit.Assume.assumeTrue(file.exists())
        val result = fullPipeline().run(file.readText())
        assertFalse(result.contains("/*"))
        assertTrue(result.contains("body"))
    }

    @Test
    fun `Pipeline preserves JSON structure`() {
        val file = File("demo/json/package.json")
        org.junit.Assume.assumeTrue(file.exists())
        val result = fullPipeline().run(file.readText())
        assertTrue(result.contains("\"name\""))
        assertTrue(result.contains("\"version\""))
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private fun fullPipeline() = ProcessingPipeline(listOf(
        CommentRemover(), ImportCleaner(), Formatter(), BracketFixer()
    ))
}
