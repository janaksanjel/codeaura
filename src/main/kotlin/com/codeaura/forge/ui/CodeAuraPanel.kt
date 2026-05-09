package com.codeaura.forge.ui

import com.codeaura.forge.engine.PipelineRunner
import com.codeaura.forge.settings.FileConfig
import com.codeaura.forge.settings.FileConfigRegistry
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder

private val BG       = Color(0x0F0F1A)
private val SURFACE  = Color(0x1A1A2E)
private val BORDER   = Color(0x2A2A45)
private val ACCENT   = Color(0x9B6DFF)
private val ACCENT2  = Color(0x00D4FF)
private val SUCCESS  = Color(0x00C896)
private val FG       = Color(0xE8E8F0)
private val FG_DIM   = Color(0x8888AA)
private val FG_HINT  = Color(0x555577)
private val BTN_BLUE = Color(0x1A73E8)
private val BTN_RED  = Color(0xD32F2F)
private val CB_BLUE  = Color(0x2979FF)
private val MONO_SM  = Font("JetBrains Mono", Font.PLAIN, 11)
private val MONO_B   = Font("JetBrains Mono", Font.BOLD, 12)

class CodeAuraPanel(private val project: Project) : JPanel(BorderLayout()) {

    private val registry = ApplicationManager.getApplication().getService(FileConfigRegistry::class.java)

    private val fileLabel = JLabel("No file selected").apply { font = MONO_SM; foreground = FG_DIM }
    private val extBadge  = JLabel("").apply { font = Font("JetBrains Mono", Font.BOLD, 9); foreground = ACCENT2; isVisible = false }

    private val cbRemoveComments = blueCheck("Remove Comments",   "Strip // # <!-- --> /* */")
    private val cbCleanImports   = blueCheck("Clean Imports",     "Deduplicate & sort imports")
    private val cbAutoFormat     = blueCheck("Auto Format",       "Collapse blank lines & trim")
    private val cbFixBrackets    = blueCheck("Fix Brackets",      "Ensure space before {")
    private val cbSortLines      = blueCheck("Sort Lines",        "Sort selected/all lines A→Z")
    private val cbTrimWhitespace = blueCheck("Trim Whitespace",   "Remove trailing spaces")
    private val allChecks = listOf(cbRemoveComments, cbCleanImports, cbAutoFormat, cbFixBrackets, cbSortLines, cbTrimWhitespace)

    private val btnFormat = makeButton(">> Format File",       BTN_BLUE, Color.WHITE, 40, true)
    private val btnReset  = makeButton("x  Reset to Defaults",  BTN_RED,  Color.WHITE, 34, false)

    private val statusLabel = JLabel("Ready").apply { font = MONO_SM; foreground = FG_DIM }

    private var currentFile: VirtualFile? = null

    init {
        background = BG
        add(buildHeader(), BorderLayout.NORTH)
        add(buildBody(),   BorderLayout.CENTER)
        add(buildFooter(), BorderLayout.SOUTH)
        wireListeners()
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private fun buildHeader(): JPanel {
        val header = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            background = SURFACE
            border = EmptyBorder(14, 14, 12, 14)
        }
        val logoRow = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
            isOpaque = false
            add(JLabel("◈ ").apply { font = Font("JetBrains Mono", Font.BOLD, 16); foreground = ACCENT })
            add(JLabel("CodeAura").apply { font = Font("JetBrains Mono", Font.BOLD, 16); foreground = FG })
        }
        val tagLine = JLabel("Smart Code Formatter").apply {
            font = Font("JetBrains Mono", Font.PLAIN, 10); foreground = ACCENT2
            border = EmptyBorder(1, 2, 8, 0)
        }
        val fileRow = JPanel(FlowLayout(FlowLayout.LEFT, 4, 0)).apply {
            isOpaque = false; add(fileLabel); add(extBadge)
        }
        header.add(logoRow); header.add(tagLine); header.add(fileRow)
        return header
    }

    // ── Body ──────────────────────────────────────────────────────────────────

    private fun buildBody(): JScrollPane {
        val body = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            background = BG
            border = EmptyBorder(12, 10, 12, 10)
        }

        body.add(sectionLabel("PROCESSING"))
        body.add(Box.createVerticalStrut(4))

        val card = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            background = SURFACE
            border = LineBorder(BORDER, 1, true)
            alignmentX = LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
        }
        allChecks.forEachIndexed { i, cb ->
            card.add(cb)
            if (i < allChecks.size - 1) card.add(JSeparator().apply {
                foreground = BORDER; maximumSize = Dimension(Int.MAX_VALUE, 1)
            })
        }
        body.add(card)

        body.add(Box.createVerticalStrut(14))
        body.add(sectionLabel("ACTIONS"))
        body.add(Box.createVerticalStrut(6))
        body.add(btnFormat)
        body.add(Box.createVerticalStrut(6))
        body.add(btnReset)

        body.add(Box.createVerticalStrut(12))
        body.add(sectionLabel("SHORTCUTS"))
        body.add(Box.createVerticalStrut(4))
        val shortcutCard = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            background = SURFACE
            border = LineBorder(BORDER, 1, true)
            alignmentX = LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
        }
        listOf(
            "Ctrl+Alt+F" to "Format File",
            "Ctrl+Alt+C" to "Remove Comments",
            "Ctrl+Alt+I" to "Clean Imports",
            "Ctrl+Alt+S" to "Sort Lines",
            "Ctrl+Alt+T" to "Trim Whitespace",
            "Ctrl+Alt+B" to "Toggle Sidebar"
        ).forEachIndexed { i, (key, desc) ->
            shortcutCard.add(shortcutRow(key, desc))
            if (i < 5) shortcutCard.add(JSeparator().apply {
                foreground = BORDER; maximumSize = Dimension(Int.MAX_VALUE, 1)
            })
        }
        body.add(shortcutCard)

        body.add(Box.createVerticalStrut(10))
        body.add(JLabel("* Select code to format selection only").apply {
            font = Font("JetBrains Mono", Font.PLAIN, 10)
            foreground = Color(0x6B6B8A)
            alignmentX = LEFT_ALIGNMENT
        })
        body.add(Box.createVerticalGlue())

        return JScrollPane(body).apply {
            border = null; background = BG; viewport.background = BG
            verticalScrollBar.unitIncrement = 10
            verticalScrollBarPolicy   = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        }
    }

    // ── Footer ────────────────────────────────────────────────────────────────

    private fun buildFooter(): JPanel {
        return JPanel(BorderLayout()).apply {
            background = Color(0x16213E)
            border = EmptyBorder(6, 12, 6, 12)
            add(JSeparator().apply { foreground = BORDER }, BorderLayout.NORTH)
            add(statusLabel, BorderLayout.WEST)
            add(JLabel("v1.0.0").apply { font = MONO_SM; foreground = Color(0x44445A) }, BorderLayout.EAST)
        }
    }

    // ── Listeners ─────────────────────────────────────────────────────────────

    private fun wireListeners() {
        allChecks.forEach { it.addActionListener { syncToConfig() } }
        btnFormat.addActionListener { runPipeline(); setStatus("✓  Done", SUCCESS) }
        btnReset.addActionListener  { resetConfig(); setStatus("↺  Reset", FG_DIM) }
    }

    private fun syncToConfig() {
        val cfg = registry.getOrCreate(currentFile ?: return)
        cfg.removeComments  = cbRemoveComments.isSelected
        cfg.cleanImports    = cbCleanImports.isSelected
        cfg.autoFormat      = cbAutoFormat.isSelected
        cfg.fixBrackets     = cbFixBrackets.isSelected
        cfg.sortLines       = cbSortLines.isSelected
        cfg.trimWhitespace  = cbTrimWhitespace.isSelected
    }

    private fun runPipeline() {
        val file   = currentFile ?: return
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        PipelineRunner.runOnEditor(project, editor, registry.getOrCreate(file), file)
    }

    private fun resetConfig() {
        registry.remove(currentFile ?: return)
        loadFromConfig(FileConfig())
    }

    private fun setStatus(msg: String, color: Color) {
        statusLabel.text = msg; statusLabel.foreground = color
        Timer(2500) { statusLabel.text = "Ready"; statusLabel.foreground = FG_DIM }.apply { isRepeats = false; start() }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    fun refreshForFile(file: VirtualFile) {
        currentFile = file
        fileLabel.text = file.name
        extBadge.text  = " .${file.extension?.uppercase() ?: "?"}"
        extBadge.isVisible = true
        loadFromConfig(registry.getOrCreate(file))
    }

    private fun loadFromConfig(cfg: FileConfig) {
        cbRemoveComments.isSelected = cfg.removeComments
        cbCleanImports.isSelected   = cfg.cleanImports
        cbAutoFormat.isSelected     = cfg.autoFormat
        cbFixBrackets.isSelected    = cfg.fixBrackets
        cbSortLines.isSelected      = cfg.sortLines
        cbTrimWhitespace.isSelected = cfg.trimWhitespace
        repaint()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun shortcutRow(key: String, desc: String) = JPanel(BorderLayout()).apply {
        background = SURFACE
        border = EmptyBorder(5, 10, 5, 10)
        maximumSize = Dimension(Int.MAX_VALUE, 28)
        add(JLabel(desc).apply { font = MONO_SM; foreground = Color(0xAAAAAA) }, BorderLayout.WEST)
        add(JLabel(key).apply {
            font = Font("JetBrains Mono", Font.BOLD, 10)
            foreground = ACCENT
            border = EmptyBorder(2, 6, 2, 6)
            background = Color(0x0F0F1A)
            isOpaque = true
        }, BorderLayout.EAST)
    }

    private fun sectionLabel(text: String) = JLabel(text).apply {
        font = Font("JetBrains Mono", Font.BOLD, 10)
        foreground = FG_DIM; alignmentX = LEFT_ALIGNMENT
    }

    private fun makeButton(text: String, bg: Color, fg: Color, h: Int, bold: Boolean) = JButton(text).apply {
        background = bg; foreground = fg
        font = if (bold) MONO_B else MONO_SM
        isFocusPainted = false; isOpaque = true; isBorderPainted = false
        cursor = Cursor(Cursor.HAND_CURSOR)
        alignmentX = LEFT_ALIGNMENT
        maximumSize = Dimension(Int.MAX_VALUE, h)
        preferredSize = Dimension(200, h)
    }

    private fun blueCheck(label: String, hint: String): JCheckBox {
        return object : JCheckBox() {
            override fun paintComponent(g: Graphics) {
                val g2 = g as Graphics2D
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g2.color = SURFACE
                g2.fillRect(0, 0, width, height)

                val bx = 10; val by = (height - 16) / 2
                if (isSelected) {
                    g2.color = CB_BLUE
                    g2.fillRoundRect(bx, by, 16, 16, 4, 4)
                    g2.color = Color.WHITE
                    g2.stroke = BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
                    g2.drawLine(bx + 3, by + 8, bx + 6, by + 11)
                    g2.drawLine(bx + 6, by + 11, bx + 13, by + 4)
                } else {
                    g2.color = BORDER
                    g2.drawRoundRect(bx, by, 16, 16, 4, 4)
                }

                g2.font  = MONO_B
                g2.color = if (isSelected) FG else Color(0xAAAAAA)
                g2.drawString(label, bx + 24, height / 2 - 2)

                g2.font  = Font("JetBrains Mono", Font.PLAIN, 10)
                g2.color = FG_HINT
                g2.drawString(hint, bx + 24, height / 2 + 11)
            }
        }.apply {
            isOpaque = true; background = SURFACE
            isFocusPainted = false; isBorderPainted = false; isContentAreaFilled = false
            cursor = Cursor(Cursor.HAND_CURSOR)
            preferredSize = Dimension(200, 50); minimumSize = Dimension(100, 50)
            maximumSize = Dimension(Int.MAX_VALUE, 50); alignmentX = LEFT_ALIGNMENT
        }
    }
}
