package com.codeaura.forge

import com.codeaura.forge.engine.*
import com.codeaura.forge.settings.FileConfig
import java.io.File

/**
 * Standalone sandbox runner — no IntelliJ needed.
 * Reads every file in demo/, runs the full pipeline, prints before/after diff stats.
 *
 * Run with:  kotlinc -script SandboxRunner.kts   (or via Gradle test task)
 */
object SandboxRunner {
    @JvmStatic
    fun main(args: Array<String>) {
    val demoRoot = File("demo")
    val allFiles = demoRoot.walkTopDown().filter { it.isFile }.toList()

    println("╔══════════════════════════════════════════════════════╗")
    println("║         ◈ CodeAura Forge — Sandbox Test Runner       ║")
    println("╚══════════════════════════════════════════════════════╝")
    println("Found ${allFiles.size} demo files\n")

    allFiles.forEach { file ->
        println("─".repeat(56))
        println("📄 File : ${file.relativeTo(demoRoot)}")

        val original = file.readText()
        val config   = FileConfig(
            removeComments = true,
            cleanImports   = true,
            autoFormat     = true,
            fixBrackets    = true
        )

        val result = buildPipeline(config).run(original)

        val linesBefore = original.lines().size
        val linesAfter  = result.lines().size
        val removed     = linesBefore - linesAfter

        println("  Lines before : $linesBefore")
        println("  Lines after  : $linesAfter")
        println("  Lines removed: $removed")
        println("  Status       : ${if (result != original) "✅ CHANGED" else "⬜ NO CHANGE"}")

        // Write formatted output next to original with .formatted extension
        val out = File(file.parent, "${file.nameWithoutExtension}.formatted.${file.extension}")
        out.writeText(result)
        println("  Output saved : ${out.relativeTo(demoRoot)}")
    }

    println("\n" + "═".repeat(56))
    println("✅ All ${allFiles.size} files processed.")
    println("═".repeat(56))
    }

    private fun buildPipeline(config: FileConfig): ProcessingPipeline {
        val processors = mutableListOf<CodeProcessor>()
        if (config.removeComments) processors += CommentRemover()
        if (config.cleanImports)   processors += ImportCleaner()
        processors += Formatter()
        if (config.fixBrackets)    processors += BracketFixer()
        return ProcessingPipeline(processors)
    }
}
