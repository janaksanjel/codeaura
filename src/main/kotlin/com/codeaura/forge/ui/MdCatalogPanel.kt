package com.codeaura.forge.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VfsUtil
import java.awt.*
import java.io.File
import javax.swing.*
import javax.swing.border.EmptyBorder

private val MD_BG      = Color(0x0F0F1A)
private val MD_SURFACE = Color(0x1A1A2E)
private val MD_BORDER  = Color(0x2A2A45)
private val MD_ACCENT  = Color(0x9B6DFF)
private val MD_FG      = Color(0xE8E8F0)
private val MD_DIM     = Color(0x8888AA)
private val MD_HINT    = Color(0x555577)
private val MD_MONO    = Font("JetBrains Mono", Font.PLAIN, 11)
private val MD_MONO_B  = Font("JetBrains Mono", Font.BOLD, 12)

class MdCatalogPanel(private val project: Project) : JPanel(BorderLayout()) {

    private val listModel = DefaultListModel<VirtualFile>()
    private val fileList  = JList(listModel)
    private val preview   = JTextArea().apply {
        isEditable = false
        background = MD_BG
        foreground = MD_FG
        font = MD_MONO
        lineWrap = true
        wrapStyleWord = true
        border = EmptyBorder(10, 10, 10, 10)
    }
    private val fileCountLabel = JLabel("0 files").apply {
        font = MD_MONO; foreground = MD_DIM
    }

    init {
        background = MD_BG
        add(buildHeader(),  BorderLayout.NORTH)
        add(buildBody(),    BorderLayout.CENTER)
        scanProject()
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private fun buildHeader(): JPanel {
        val header = JPanel(BorderLayout()).apply {
            background = MD_SURFACE
            border = EmptyBorder(10, 14, 10, 14)
        }
        val title = JLabel("MD Catalog").apply {
            font = MD_MONO_B; foreground = MD_ACCENT
        }
        val refreshBtn = JButton("Refresh").apply {
            font = Font("JetBrains Mono", Font.PLAIN, 10)
            background = MD_BG; foreground = MD_DIM
            isFocusPainted = false; isBorderPainted = false; isOpaque = true
            cursor = Cursor(Cursor.HAND_CURSOR)
            addActionListener { scanProject() }
        }
        header.add(title,        BorderLayout.WEST)
        header.add(fileCountLabel, BorderLayout.CENTER)
        header.add(refreshBtn,   BorderLayout.EAST)
        return header
    }

    // ── Body ──────────────────────────────────────────────────────────────────

    private fun buildBody(): JSplitPane {
        // file list
        fileList.apply {
            background = MD_SURFACE
            foreground = MD_FG
            font = MD_MONO
            selectionBackground = Color(0x2A2A55)
            selectionForeground = MD_FG
            border = EmptyBorder(4, 0, 4, 0)
            cellRenderer = MdFileRenderer()
        }
        fileList.addListSelectionListener {
            val file = fileList.selectedValue ?: return@addListSelectionListener
            loadPreview(file)
        }

        val listScroll = JScrollPane(fileList).apply {
            border = null; background = MD_SURFACE
            viewport.background = MD_SURFACE
        }

        val previewScroll = JScrollPane(preview).apply {
            border = null; background = MD_BG
            viewport.background = MD_BG
        }

        return JSplitPane(JSplitPane.VERTICAL_SPLIT, listScroll, previewScroll).apply {
            background = MD_BG
            dividerSize = 4
            resizeWeight = 0.35
            border = null
        }
    }

    // ── Logic ─────────────────────────────────────────────────────────────────

    private fun scanProject() {
        listModel.clear()
        preview.text = ""
        val basePath = project.basePath ?: return
        File(basePath).walkTopDown()
            .filter { it.isFile && it.extension.equals("md", ignoreCase = true) }
            .sortedBy { it.name }
            .forEach { f ->
                val vf = VfsUtil.findFileByIoFile(f, true)
                if (vf != null) listModel.addElement(vf)
            }
        fileCountLabel.text = "  ${listModel.size()} file${if (listModel.size() != 1) "s" else ""}"
        if (listModel.size() > 0) {
            fileList.selectedIndex = 0
            loadPreview(listModel.getElementAt(0))
        }
    }

    private fun loadPreview(file: VirtualFile) {
        try {
            preview.text = file.contentsToByteArray().toString(Charsets.UTF_8)
            preview.caretPosition = 0
        } catch (e: Exception) {
            preview.text = "Could not read file: ${e.message}"
        }
    }

    // ── Renderer ──────────────────────────────────────────────────────────────

    private inner class MdFileRenderer : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(
            list: JList<*>, value: Any?, index: Int,
            isSelected: Boolean, cellHasFocus: Boolean
        ): Component {
            val file = value as? VirtualFile
            val label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
            label.text = "  ${file?.name ?: ""}"
            label.font = MD_MONO
            label.foreground = if (isSelected) MD_FG else MD_DIM
            label.background = if (isSelected) Color(0x2A2A55) else MD_SURFACE
            label.border = EmptyBorder(5, 6, 5, 6)
            label.isOpaque = true
            return label
        }
    }
}
