package biorritmo

import javax.swing.JPanel
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.Ellipse2D
import java.awt.geom.Path2D
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BiorhythmCanvas extends JPanel {
    private static final double CHART_WIDTH = 760d
    private static final double CHART_HEIGHT = 340d
    private static final double MARGIN_TOP = 16d
    private static final double MARGIN_RIGHT = 16d
    private static final double MARGIN_BOTTOM = 34d
    private static final double MARGIN_LEFT = 44d
    private static final double CHART_Y = 108d
    private static final double LEGEND_Y = 464d
    private static final double LEGEND_COL_WIDTH = 340d
    private static final double LEGEND_COL_GAP = 40d
    private static final double LEGEND_LEFT = 20d
    private static final double LEGEND_TOP = 12d
    private static final double LEGEND_HEADER_H = 22d
    private static final double LEGEND_ROW_H = 26d

    private final Closure<ThemeColors> colorProvider
    private final Map<String, Boolean> visible = Aspects.ALL.collectEntries { Aspect aspect -> [aspect.key, true] }
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern('dd MMM', new Locale('es', 'MX'))
    private BiorhythmResult result
    private LocalDate selectedDate = LocalDate.now()
    private double offsetX = 0d

    BiorhythmCanvas(Closure<ThemeColors> colorProvider) {
        this.colorProvider = colorProvider
        preferredSize = new Dimension(800, 614)
        opaque = true
        addMouseListener(new MouseAdapter() {
            @Override
            void mousePressed(MouseEvent event) {
                toggleLegendAt(event.x as double, event.y as double)
            }
        })
    }

    void updateData(LocalDate birthDate, LocalDate date) {
        selectedDate = date
        result = Biorhythm.calculate(birthDate, date)
        repaint()
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics)
        Graphics2D g = graphics.create() as Graphics2D
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            ThemeColors theme = colorProvider.call()
            g.color = theme.background
            g.fillRect(0, 0, width, height)
            offsetX = Math.max((width - CHART_WIDTH) / 2d, 0d)
            drawHeader(g, theme)
            drawChart(g, theme)
            drawLegend(g, theme)
        } finally {
            g.dispose()
        }
    }

    private void drawHeader(Graphics2D g, ThemeColors theme) {
        double center = offsetX + CHART_WIDTH / 2d
        drawCentered(g, 'Calculadora de biorritmo', center, 29d, 22f, Font.PLAIN, theme.heading)
        drawCentered(g, 'Ingresa tu fecha de nacimiento para graficar tus ciclos físico, emocional e intelectual,', center, 58d, 12f, Font.PLAIN, theme.muted)
        drawCentered(g, 'junto con los aspectos complementarios: espiritual, conciencia, intuición y estética.', center, 76d, 12f, Font.PLAIN, theme.muted)
    }

    private void drawChart(Graphics2D g, ThemeColors theme) {
        if (result == null) return
        double plotWidth = CHART_WIDTH - MARGIN_LEFT - MARGIN_RIGHT
        double plotHeight = CHART_HEIGHT - MARGIN_TOP - MARGIN_BOTTOM
        double left = offsetX + MARGIN_LEFT
        double right = offsetX + CHART_WIDTH - MARGIN_RIGHT
        double top = CHART_Y + MARGIN_TOP
        double bottom = CHART_Y + CHART_HEIGHT - MARGIN_BOTTOM
        Closure<Double> x = { int index -> left + index / (result.dates.size() - 1d) * plotWidth }
        Closure<Double> y = { double value -> top + (1d - (value + 100d) / 200d) * plotHeight }

        [100, 50, 0, -50, -100].each { int value ->
            double yy = y(value)
            g.color = value == 0 ? theme.zeroGridline : theme.gridline
            g.stroke = new BasicStroke(1f)
            g.drawLine(left as int, yy as int, right as int, yy as int)
            drawRight(g, value.toString(), left - 8d, yy + 4d, 11f, Font.PLAIN, theme.axisLabel)
        }

        double centerX = x(Biorhythm.RANGE_DAYS)
        g.color = theme.markerLine
        g.stroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, [3f, 3f] as float[], 0f)
        g.drawLine(centerX as int, top as int, centerX as int, bottom as int)
        String marker = selectedDate == LocalDate.now() ? 'Hoy' : formatDate(selectedDate)
        drawCentered(g, marker, centerX, top - 6d, 12f, Font.BOLD, theme.label)

        result.dates.eachWithIndex { LocalDate date, int index ->
            if ((index - Biorhythm.RANGE_DAYS) % 5 == 0) {
                drawCentered(g, formatDate(date), x(index), bottom + 18d, 11f, Font.PLAIN, theme.axisLabel)
            }
        }

        result.cycles.findAll { CycleResult cycle -> visible[cycle.aspect.key] }.each { CycleResult cycle ->
            Path2D.Double path = new Path2D.Double()
            cycle.values.eachWithIndex { double value, int index ->
                if (index == 0) path.moveTo(x(index), y(value)) else path.lineTo(x(index), y(value))
            }
            g.color = cycle.aspect.color
            g.stroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, cycle.aspect.dash, 0f)
            g.draw(path)
            double markerY = y(cycle.values[Biorhythm.RANGE_DAYS])
            g.fill(new Ellipse2D.Double(centerX - 5d, markerY - 5d, 10d, 10d))
            g.color = Color.WHITE
            g.stroke = new BasicStroke(2f)
            g.draw(new Ellipse2D.Double(centerX - 5d, markerY - 5d, 10d, 10d))
        }
    }

    private void drawLegend(Graphics2D g, ThemeColors theme) {
        if (result == null) return
        List<Map> groups = [
            [x: LEGEND_LEFT, title: 'Aspectos básicos', aspects: Aspects.ALL.findAll { it.group == AspectGroup.BASIC }],
            [x: LEGEND_LEFT + LEGEND_COL_WIDTH + LEGEND_COL_GAP, title: 'Aspectos complementarios', aspects: Aspects.ALL.findAll { it.group == AspectGroup.COMPLEMENTARY }]
        ]
        Map<String, CycleResult> values = result.cycles.collectEntries { CycleResult cycle -> [cycle.aspect.key, cycle] }
        groups.each { Map group ->
            double columnX = group.x as double
            drawLeft(g, group.title as String, offsetX + columnX, LEGEND_Y + LEGEND_TOP + 14d, 14f, Font.BOLD, theme.label)
            (group.aspects as List<Aspect>).eachWithIndex { Aspect aspect, int index ->
                double rowTop = LEGEND_Y + LEGEND_TOP + LEGEND_HEADER_H + index * LEGEND_ROW_H
                double centerY = rowTop + LEGEND_ROW_H / 2d
                boolean enabled = visible[aspect.key]
                g.color = aspect.color
                Ellipse2D dot = new Ellipse2D.Double(offsetX + columnX + 2d, centerY - 6d, 12d, 12d)
                if (enabled) g.fill(dot)
                else {
                    g.stroke = new BasicStroke(2f)
                    g.draw(dot)
                }
                CycleResult line = values[aspect.key]
                Color textColor = enabled ? theme.heading : theme.legendDim
                drawLeft(g, aspect.label, offsetX + columnX + 24d, rowTop + 17d, 13f, Font.PLAIN, textColor)
                drawRight(g, "${line.currentValue}%", offsetX + columnX + LEGEND_COL_WIDTH - 90d, rowTop + 17d, 13f, Font.PLAIN, textColor)
                drawRight(g, line.status, offsetX + columnX + LEGEND_COL_WIDTH, rowTop + 17d, 12f, Font.PLAIN, theme.axisLabel)
            }
        }
    }

    private void toggleLegendAt(double x, double y) {
        double localX = x - offsetX
        double localY = y - LEGEND_Y
        List<Map> columns = [
            [x: LEGEND_LEFT, aspects: Aspects.ALL.findAll { it.group == AspectGroup.BASIC }],
            [x: LEGEND_LEFT + LEGEND_COL_WIDTH + LEGEND_COL_GAP, aspects: Aspects.ALL.findAll { it.group == AspectGroup.COMPLEMENTARY }]
        ]
        for (Map column : columns) {
            double columnX = column.x as double
            if (localX < columnX || localX > columnX + LEGEND_COL_WIDTH) continue
            List<Aspect> aspects = column.aspects as List<Aspect>
            for (int index = 0; index < aspects.size(); index++) {
                double rowY = LEGEND_TOP + LEGEND_HEADER_H + index * LEGEND_ROW_H
                if (localY >= rowY && localY <= rowY + LEGEND_ROW_H) {
                    Aspect aspect = aspects[index]
                    visible[aspect.key] = !visible[aspect.key]
                    repaint()
                    return
                }
            }
        }
    }

    private String formatDate(LocalDate date) {
        date.format(dateFormat).toLowerCase(new Locale('es', 'MX'))
    }

    private static Font font(float size, int style) { new Font('Segoe UI', style, size as int) }

    private static void drawLeft(Graphics2D g, String text, double x, double baseline, float size, int style, Color color) {
        g.font = font(size, style); g.color = color
        g.drawString(text, x as float, baseline as float)
    }

    private static void drawCentered(Graphics2D g, String text, double centerX, double baseline, float size, int style, Color color) {
        g.font = font(size, style); g.color = color
        g.drawString(text, (centerX - g.fontMetrics.stringWidth(text) / 2d) as float, baseline as float)
    }

    private static void drawRight(Graphics2D g, String text, double right, double baseline, float size, int style, Color color) {
        g.font = font(size, style); g.color = color
        g.drawString(text, (right - g.fontMetrics.stringWidth(text)) as float, baseline as float)
    }
}
