package com.codeaura.forge.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class CodeAuraToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val factory = ContentFactory.getInstance()

        val formatPanel = CodeAuraPanel(project)
        val mdPanel     = MdCatalogPanel(project)

        toolWindow.contentManager.addContent(
            factory.createContent(formatPanel, "Formatter", false)
        )
        toolWindow.contentManager.addContent(
            factory.createContent(mdPanel, "MD Catalog", false)
        )

        panels[project] = formatPanel
    }

    companion object {
        private val panels = mutableMapOf<Project, CodeAuraPanel>()

        fun refreshForFile(project: Project, file: VirtualFile) {
            panels[project]?.refreshForFile(file)
        }
    }
}
