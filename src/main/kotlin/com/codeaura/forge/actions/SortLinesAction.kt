package com.codeaura.forge.actions

import com.codeaura.forge.engine.PipelineRunner
import com.codeaura.forge.settings.FileConfig
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

class SortLinesAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor  = e.getData(CommonDataKeys.EDITOR) ?: return
        val file    = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        PipelineRunner.runOnEditor(project, editor, FileConfig(sortLines = true), file)
    }
}
