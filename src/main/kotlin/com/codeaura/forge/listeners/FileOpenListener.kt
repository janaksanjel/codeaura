package com.codeaura.forge.listeners

import com.codeaura.forge.settings.FileConfigRegistry
import com.codeaura.forge.ui.CodeAuraToolWindowFactory
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager

class FileOpenListener : FileEditorManagerListener {

    override fun selectionChanged(event: FileEditorManagerEvent) {
        val project = event.manager.project
        val newFile = event.newFile ?: return

        // Ensure config exists for this file
        val registry = ApplicationManager.getApplication().getService(FileConfigRegistry::class.java)
        registry.getOrCreate(newFile)

        // Auto-open the tool window
        ApplicationManager.getApplication().invokeLater {
            val tw = ToolWindowManager.getInstance(project).getToolWindow("CodeAura")
            tw?.show {
                CodeAuraToolWindowFactory.refreshForFile(project, newFile)
            }
        }
    }
}
