package com.codeaura.forge

import com.codeaura.forge.engine.*
import com.codeaura.forge.settings.FileConfig
import com.codeaura.forge.settings.FileConfigRegistry
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.io.File

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class SandboxTest {

    // ── Unit tests per processor ──────────────────────────────────────────────

    @Test @Order(1)
    fun `CommentRemover removes single-line comments`() {
        val input  = "val x = 1 // this is a comment\nval y = 2"
        val result = CommentRemover().process(input)
        assertFalse(result.contains("//"), "Single-line comment should be removed")
        assertTrue(result.contains("val x = 1"), "Code should remain")
    }

    @Test @Order(2)
    fun `CommentRemover removes block comments`() {
        val input  = "/* block comment */\nval x = 1"
        val result = CommentRemover().process(input)
        assertFalse(result.contains("/*"), "Block comment should be removed")
    }

    @Test @Order(3)
    fun `CommentRemover removes HTML comments`() {
        val input  = "<!-- nav -->\n<div>Hello</div>"
        val result = CommentRemover().process(input)
        assertFalse(result.contains("<!--"), "HTML comment should be removed")
        assertTrue(result.contains("<div>"), "HTML tag should remain")
    }

    @Test @Order(4)
    fun `ImportCleaner removes duplicate imports`() {
        val input = """
            import java.util.List
            import java.util.List
            import java.util.Map
            class Foo {}
        """.trimIndent()
        val result = ImportCleaner().process(input)
        val importCount = result.lines().count { it.trimStart().startsWith("import java.util.List") }
        assertEquals(1, importCount, "Duplicate import should be deduplicated")
    }

    @Test @Order(5)
    fun `ImportCleaner sorts imports alphabetically`() {
        val input = """
            import z.Zoo
            import a.Alpha
            import m.Middle
            class Bar {}
        """.trimIndent()
        val result = ImportCleaner().process(input)
        val imports = result.lines().filter { it.trimStart().startsWith("import") }
        assertEquals(listOf("import a.Alpha", "import m.Middle", "import z.Zoo"), imports)
    }

    @Test @Order(6)
    fun `Formatter collapses multiple blank lines`() {
        val input  = "val a = 1\n\n\n\nval b = 2"
        val result = Formatter().process(input)
        assertFalse(result.contains("\n\n\n"), "3+ blank lines should be collapsed")
    }

    @Test @Order(7)
    fun `BracketFixer adds space before opening brace`() {
        val input  = "fun foo(){\n  val x = 1\n}"
        val result = BracketFixer().process(input)
        assertTrue(result.contains("foo() {"), "Space before brace should be added")
    }

    @Test @Order(8)
    fun `ProcessingPipeline runs all processors in order`() {
        val input = """
            import a.A
            import a.A
            // comment
            fun go(){
            }
        """.trimIndent()
        val pipeline = ProcessingPipeline(listOf(
            CommentRemover(), ImportCleaner(), Formatter(), BracketFixer()
        ))
        val result = pipeline.run(input)
        assertFalse(result.contains("//"),       "Comments removed")
        assertFalse(result.contains("\n\n\n"),    "Extra blanks collapsed")
        assertTrue(result.contains("go() {"),    "Bracket fixed")
        val importCount = result.lines().count { it.trimStart().startsWith("import a.A") }
        assertEquals(1, importCount,             "Duplicate import removed")
    }

    // ── FileConfig tests ──────────────────────────────────────────────────────

    @Test @Order(9)
    fun `FileConfig defaults are all false with Dark theme`() {
        val cfg = FileConfig()
        assertFalse(cfg.removeComments)
        assertFalse(cfg.cleanImports)
        assertFalse(cfg.autoFormat)
        assertFalse(cfg.fixBrackets)
        assertEquals("Dark", cfg.theme)
    }

    // ── Integration tests on real demo files ──────────────────────────────────

    @Test @Order(10)
    fun `Pipeline processes demo JS file and reduces line count`() {
        val file = File("demo/js/app.js")
        assumeFileExists(file)
        val original = file.readText()
        val result   = fullPipeline().run(original)
        assertTrue(result.lines().size < original.lines().size,
            "Formatted JS should have fewer lines (comments + blanks removed)")
    }

    @Test @Order(11)
    fun `Pipeline processes demo TS file and deduplicates imports`() {
        val file = File("demo/ts/user.service.ts")
        assumeFileExists(file)
        val original = file.readText()
        val result   = fullPipeline().run(original)
        val httpImports = result.lines().count { it.contains("HttpClient") }
        assertEquals(1, httpImports, "Duplicate HttpClient import should be removed")
    }

    @Test @Order(12)
    fun `Pipeline processes demo Java file and removes comments`() {
        val file = File("demo/java/UserManager.java")
        assumeFileExists(file)
        val original = file.readText()
        val result   = fullPipeline().run(original)
        assertFalse(result.contains("// "), "Inline comments should be removed")
    }

    @Test @Order(13)
    fun `Pipeline processes demo Kotlin file and fixes brackets`() {
        val file = File("demo/kotlin/UserRepository.kt")
        assumeFileExists(file)
        val original = file.readText()
        val result   = fullPipeline().run(original)
        assertFalse(result.contains("){"), "Bracket without space should be fixed")
    }

    @Test @Order(14)
    fun `Pipeline processes demo HTML file`() {
        val file = File("demo/html/index.html")
        assumeFileExists(file)
        val original = file.readText()
        val result   = fullPipeline().run(original)
        assertFalse(result.contains("<!--"), "HTML comments should be removed")
        assertTrue(result.contains("<html"),  "HTML structure should remain")
    }

    @Test @Order(15)
    fun `Pipeline processes demo CSS file`() {
        val file = File("demo/css/style.css")
        assumeFileExists(file)
        val original = file.readText()
        val result   = fullPipeline().run(original)
        assertFalse(result.contains("/* "), "CSS comments should be removed")
        assertTrue(result.contains("body"), "CSS rules should remain")
    }

    @Test @Order(16)
    fun `Pipeline processes demo JSON file without breaking structure`() {
        val file = File("demo/json/package.json")
        assumeFileExists(file)
        val original = file.readText()
        val result   = fullPipeline().run(original)
        assertTrue(result.contains("\"name\""),    "JSON keys should remain")
        assertTrue(result.contains("\"version\""), "JSON keys should remain")
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun fullPipeline() = ProcessingPipeline(listOf(
        CommentRemover(), ImportCleaner(), Formatter(), BracketFixer()
    ))

    private fun assumeFileExists(file: File) {
        Assumptions.assumeTrue(file.exists(), "Demo file not found: ${file.path}")
    }
}
