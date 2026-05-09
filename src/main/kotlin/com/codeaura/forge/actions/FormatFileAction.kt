package com.codeaura.forge.actions

import com.codeaura.forge.engine.PipelineRunner
import com.codeaura.forge.settings.FileConfigRegistry
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager

class FormatFileAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project  = e.project ?: return
        val editor   = e.getData(CommonDataKeys.EDITOR) ?: return
        val file     = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val registry = ApplicationManager.getApplication().getService(FileConfigRegistry::class.java)
        PipelineRunner.runOnEditor(project, editor, registry.getOrCreate(file), file)
    }
}
