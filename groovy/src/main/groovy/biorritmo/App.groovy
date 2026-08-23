package biorritmo

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
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.GridLayout
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class BiorhythmWindow extends JFrame {
    private ThemeMode themeMode = ThemeMode.SYSTEM
    private LocalDate birthDate
    private LocalDate selectedDate = LocalDate.now()
    private final JPanel root = new JPanel(new BorderLayout())
    private final JPanel top = new JPanel(new BorderLayout())
    private final JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8))
    private final JSpinner birthSpinner
    private final JSpinner selectedSpinner
    private final JButton previousButton = new JButton('< Anterior')
    private final JButton todayButton = new JButton('Hoy')
    private final JButton nextButton = new JButton('Siguiente >')
    private final JComboBox<String> themeCombo = new JComboBox<>(ThemeMode.values()*.displayName as String[])
    private final BiorhythmCanvas canvas = new BiorhythmCanvas({ Themes.colors(themeMode) })

    BiorhythmWindow() {
        super('Calculadora de biorritmo')
        LocalDate today = LocalDate.now()
        birthDate = LocalDate.of(today.year - 25, today.month, Math.min(today.dayOfMonth, 28))
        birthSpinner = dateSpinner(birthDate)
        selectedSpinner = dateSpinner(selectedDate)

        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        minimumSize = new Dimension(790, 720)
        size = new Dimension(900, 840)
        setLocationRelativeTo(null)
        root.border = new EmptyBorder(8, 8, 8, 8)
        contentPane = root
        buildTop()
        root.add(top, BorderLayout.NORTH)
        root.add(canvas, BorderLayout.CENTER)
        wireEvents()
        applyTheme()
        recalculate()
    }

    private void buildTop() {
        JPanel themeBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2))
        themeBar.add(new JLabel('Tema:'))
        themeCombo.preferredSize = new Dimension(100, 26)
        themeBar.add(themeCombo)
        top.add(themeBar, BorderLayout.NORTH)

        controls.add(labeled('Fecha de nacimiento', birthSpinner))
        controls.add(labeled('Fecha a analizar', selectedSpinner))
        JPanel navigation = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0))
        navigation.add(previousButton)
        navigation.add(todayButton)
        navigation.add(nextButton)
        controls.add(labeled(' ', navigation))
        top.add(controls, BorderLayout.CENTER)
    }

    private static JPanel labeled(String text, Component component) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 4))
        panel.add(new JLabel(text))
        panel.add(component)
        panel
    }

    private void wireEvents() {
        birthSpinner.addChangeListener {
            birthDate = localDate(birthSpinner)
            recalculate()
        }
        selectedSpinner.addChangeListener {
            selectedDate = localDate(selectedSpinner)
            recalculate()
        }
        previousButton.addActionListener { setSelectedDate(selectedDate.minusDays(1)) }
        nextButton.addActionListener { setSelectedDate(selectedDate.plusDays(1)) }
        todayButton.addActionListener { setSelectedDate(LocalDate.now()) }
        themeCombo.addActionListener {
            themeMode = ThemeMode.values()[themeCombo.selectedIndex]
            applyTheme()
        }
    }

    private void setSelectedDate(LocalDate date) {
        selectedDate = date
        selectedSpinner.value = toDate(date)
        recalculate()
    }

    private void recalculate() {
        todayButton.enabled = selectedDate != LocalDate.now()
        canvas.updateData(birthDate, selectedDate)
    }

    private void applyTheme() {
        ThemeColors theme = Themes.colors(themeMode)
        applyColors(root, theme)
        canvas.background = theme.background
        canvas.repaint()
    }

    private static void applyColors(Component component, ThemeColors theme) {
        if (component instanceof JPanel) component.background = theme.background
        if (component instanceof JLabel) component.foreground = theme.label
        if (component instanceof Container) {
            (component as Container).components.each { Component child -> applyColors(child, theme) }
        }
    }

    private static JSpinner dateSpinner(LocalDate date) {
        JSpinner spinner = new JSpinner(new SpinnerDateModel())
        spinner.value = toDate(date)
        spinner.editor = new JSpinner.DateEditor(spinner, 'dd/MM/yyyy')
        spinner.preferredSize = new Dimension(135, 28)
        spinner
    }

    private static Date toDate(LocalDate date) {
        Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }

    private static LocalDate localDate(JSpinner spinner) {
        (spinner.value as Date).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }
}

class App {
    static void main(String[] args) {
        System.setProperty('awt.useSystemAAFontSettings', 'on')
        SwingUtilities.invokeLater {
            try {
                UIManager.setLookAndFeel(UIManager.systemLookAndFeelClassName)
            } catch (Exception ignored) {
                // Swing conserva su apariencia multiplataforma si el tema nativo no está disponible.
            }
            UIManager.put('defaultFont', new Font('Segoe UI', Font.PLAIN, 12))
            new BiorhythmWindow().visible = true
        }
    }
}
