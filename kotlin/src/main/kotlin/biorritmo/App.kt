package biorritmo

import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.GridLayout
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerDateModel
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.WindowConstants
import javax.swing.border.EmptyBorder

class BiorhythmWindow : JFrame("Calculadora de biorritmo") {
    private var themeMode = ThemeMode.SYSTEM
    private var birthDate = LocalDate.now().let { LocalDate.of(it.year - 25, it.month, minOf(it.dayOfMonth, 28)) }
    private var selectedDate = LocalDate.now()
    private val root = JPanel(BorderLayout())
    private val top = JPanel(BorderLayout())
    private val controls = JPanel(FlowLayout(FlowLayout.CENTER, 10, 8))
    private val birthSpinner = dateSpinner(birthDate)
    private val selectedSpinner = dateSpinner(selectedDate)
    private val previousButton = JButton("< Anterior")
    private val todayButton = JButton("Hoy")
    private val nextButton = JButton("Siguiente >")
    private val themeCombo = JComboBox(ThemeMode.entries.map { it.displayName }.toTypedArray())
    private val canvas = BiorhythmCanvas { Themes.colors(themeMode) }

    init {
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        minimumSize = Dimension(790, 720)
        size = Dimension(900, 840)
        setLocationRelativeTo(null)
        root.border = EmptyBorder(8, 8, 8, 8)
        contentPane = root
        buildTop()
        root.add(top, BorderLayout.NORTH)
        root.add(canvas, BorderLayout.CENTER)
        wireEvents()
        applyTheme()
        recalculate()
    }

    private fun buildTop() {
        val themeBar = JPanel(FlowLayout(FlowLayout.RIGHT, 8, 2))
        themeBar.add(JLabel("Tema:"))
        themeCombo.preferredSize = Dimension(100, 26)
        themeBar.add(themeCombo)
        top.add(themeBar, BorderLayout.NORTH)
        controls.add(labeled("Fecha de nacimiento", birthSpinner))
        controls.add(labeled("Fecha a analizar", selectedSpinner))
        val navigation = JPanel(FlowLayout(FlowLayout.CENTER, 8, 0))
        navigation.add(previousButton)
        navigation.add(todayButton)
        navigation.add(nextButton)
        controls.add(labeled(" ", navigation))
        top.add(controls, BorderLayout.CENTER)
    }

    private fun labeled(text: String, component: Component) = JPanel(GridLayout(2, 1, 0, 4)).apply {
        add(JLabel(text)); add(component)
    }

    private fun wireEvents() {
        birthSpinner.addChangeListener { birthDate = birthSpinner.localDate(); recalculate() }
        selectedSpinner.addChangeListener { selectedDate = selectedSpinner.localDate(); recalculate() }
        previousButton.addActionListener { setSelectedDate(selectedDate.minusDays(1)) }
        nextButton.addActionListener { setSelectedDate(selectedDate.plusDays(1)) }
        todayButton.addActionListener { setSelectedDate(LocalDate.now()) }
        themeCombo.addActionListener {
            themeMode = ThemeMode.entries[themeCombo.selectedIndex]
            applyTheme()
        }
    }

    private fun setSelectedDate(date: LocalDate) {
        selectedDate = date
        selectedSpinner.value = date.toDate()
        recalculate()
    }

    private fun recalculate() {
        todayButton.isEnabled = selectedDate != LocalDate.now()
        canvas.updateData(birthDate, selectedDate)
    }

    private fun applyTheme() {
        val theme = Themes.colors(themeMode)
        applyColors(root, theme)
        canvas.background = theme.background
        canvas.repaint()
    }

    private fun applyColors(component: Component, theme: ThemeColors) {
        if (component is JPanel) component.background = theme.background
        if (component is JLabel) component.foreground = theme.label
        if (component is java.awt.Container) component.components.forEach { applyColors(it, theme) }
    }

    private fun dateSpinner(date: LocalDate) = JSpinner(SpinnerDateModel()).apply {
        value = date.toDate()
        editor = JSpinner.DateEditor(this, "dd/MM/yyyy")
        preferredSize = Dimension(135, 28)
    }
}

private fun LocalDate.toDate(): Date = Date.from(atStartOfDay(ZoneId.systemDefault()).toInstant())
private fun JSpinner.localDate(): LocalDate = (value as Date).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

fun main() {
    System.setProperty("awt.useSystemAAFontSettings", "on")
    SwingUtilities.invokeLater {
        runCatching { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()) }
        UIManager.put("defaultFont", Font("Segoe UI", Font.PLAIN, 12))
        BiorhythmWindow().isVisible = true
    }
}
