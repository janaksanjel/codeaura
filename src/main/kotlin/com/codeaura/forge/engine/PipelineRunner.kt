package com.codeaura.forge.engine

import com.codeaura.forge.settings.FileConfig
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

object PipelineRunner {

    fun runOnEditor(project: Project, editor: Editor, config: FileConfig, file: VirtualFile? = null) {
        val document = editor.document
        val ext      = file?.extension?.lowercase() ?: ""

        val processors = mutableListOf<CodeProcessor>()
        if (config.removeComments)  processors += CommentRemover()
        if (config.cleanImports)    processors += ImportCleaner()
        if (config.autoFormat)      processors += Formatter()
        if (config.fixBrackets)     processors += BracketFixer()
        if (config.sortLines)       processors += SortLines()
        if (config.trimWhitespace)  processors += TrimWhitespace()
        if (processors.isEmpty()) return

        val pipeline = ProcessingPipeline(processors)
        val selection = editor.selectionModel

        WriteCommandAction.runWriteCommandAction(project) {
            if (selection.hasSelection()) {
                // process only selected text
                val start    = selection.selectionStart
                val end      = selection.selectionEnd
                val selected = document.getText(com.intellij.openapi.util.TextRange(start, end))
                val result   = pipeline.run(selected, ext)
                if (result != selected) document.replaceString(start, end, result)
            } else {
                // process full file
                val original = document.text
                val result   = pipeline.run(original, ext)
                if (result != original) document.setText(result)
            }
        }
    }
}
