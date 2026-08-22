package biorritmo;

@kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\b\u0010\u001a\u001a\u00020\u001bH\u0002J\u0018\u0010\u001c\u001a\u00020\u000b2\u0006\u0010\u001d\u001a\u00020\u00172\u0006\u0010\u001e\u001a\u00020\u001fH\u0002J\b\u0010 \u001a\u00020\u001bH\u0002J\u0010\u0010!\u001a\u00020\u001b2\u0006\u0010\"\u001a\u00020\u0007H\u0002J\b\u0010#\u001a\u00020\u001bH\u0002J\b\u0010$\u001a\u00020\u001bH\u0002J\u0018\u0010%\u001a\u00020\u001b2\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010&\u001a\u00020\'H\u0002J\u0010\u0010(\u001a\u00020\u000f2\u0006\u0010\"\u001a\u00020\u0007H\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0006\u001a\n \b*\u0004\u0018\u00010\u00070\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0016\u0010\t\u001a\n \b*\u0004\u0018\u00010\u00070\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0015\u001a\u0010\u0012\f\u0012\n \b*\u0004\u0018\u00010\u00170\u00170\u0016X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0019X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006)"}, d2 = {"Lbiorritmo/BiorhythmWindow;", "Ljavax/swing/JFrame;", "<init>", "()V", "themeMode", "Lbiorritmo/ThemeMode;", "birthDate", "Ljava/time/LocalDate;", "kotlin.jvm.PlatformType", "selectedDate", "root", "Ljavax/swing/JPanel;", "top", "controls", "birthSpinner", "Ljavax/swing/JSpinner;", "selectedSpinner", "previousButton", "Ljavax/swing/JButton;", "todayButton", "nextButton", "themeCombo", "Ljavax/swing/JComboBox;", "", "canvas", "Lbiorritmo/BiorhythmCanvas;", "buildTop", "", "labeled", "text", "component", "Ljava/awt/Component;", "wireEvents", "setSelectedDate", "date", "recalculate", "applyTheme", "applyColors", "theme", "Lbiorritmo/ThemeColors;", "dateSpinner", "biorritmo-kotlin"})
public final class BiorhythmWindow extends javax.swing.JFrame {
    @org.jetbrains.annotations.NotNull()
    private biorritmo.ThemeMode themeMode = biorritmo.ThemeMode.SYSTEM;
    private java.time.LocalDate birthDate;
    private java.time.LocalDate selectedDate;
    @org.jetbrains.annotations.NotNull()
    private final javax.swing.JPanel root = null;
    @org.jetbrains.annotations.NotNull()
    private final javax.swing.JPanel top = null;
    @org.jetbrains.annotations.NotNull()
    private final javax.swing.JPanel controls = null;
    @org.jetbrains.annotations.NotNull()
    private final javax.swing.JSpinner birthSpinner = null;
    @org.jetbrains.annotations.NotNull()
    private final javax.swing.JSpinner selectedSpinner = null;
    @org.jetbrains.annotations.NotNull()
    private final javax.swing.JButton previousButton = null;
    @org.jetbrains.annotations.NotNull()
    private final javax.swing.JButton todayButton = null;
    @org.jetbrains.annotations.NotNull()
    private final javax.swing.JButton nextButton = null;
    @org.jetbrains.annotations.NotNull()
    private final javax.swing.JComboBox<java.lang.String> themeCombo = null;
    @org.jetbrains.annotations.NotNull()
    private final biorritmo.BiorhythmCanvas canvas = null;
    
    public BiorhythmWindow() {
        super();
    }
    
    private final void buildTop() {
    }
    
    private final javax.swing.JPanel labeled(java.lang.String text, java.awt.Component component) {
        return null;
    }
    
    private final void wireEvents() {
    }
    
    private final void setSelectedDate(java.time.LocalDate date) {
    }
    
    private final void recalculate() {
    }
    
    private final void applyTheme() {
    }
    
    private final void applyColors(java.awt.Component component, biorritmo.ThemeColors theme) {
    }
    
    private final javax.swing.JSpinner dateSpinner(java.time.LocalDate date) {
        return null;
    }
}