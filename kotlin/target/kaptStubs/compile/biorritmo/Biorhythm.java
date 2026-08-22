package biorritmo;

@kotlin.Metadata(mv = {2, 1, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0016\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\tJ\u0016\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u00072\u0006\u0010\u000e\u001a\u00020\u0005J\u0016\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\f2\u0006\u0010\u0012\u001a\u00020\fJ\u0016\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\t2\u0006\u0010\u0016\u001a\u00020\tR\u000e\u0010\u0004\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lbiorritmo/Biorhythm;", "", "<init>", "()V", "RANGE_DAYS", "", "daysBetween", "", "from", "Ljava/time/LocalDate;", "to", "valueAt", "", "daysSinceBirth", "period", "phaseLabel", "", "value", "nextValue", "calculate", "Lbiorritmo/BiorhythmResult;", "birthDate", "selectedDate", "biorritmo-kotlin"})
public final class Biorhythm {
    public static final int RANGE_DAYS = 15;
    @org.jetbrains.annotations.NotNull()
    public static final biorritmo.Biorhythm INSTANCE = null;
    
    private Biorhythm() {
        super();
    }
    
    public final long daysBetween(@org.jetbrains.annotations.NotNull()
    java.time.LocalDate from, @org.jetbrains.annotations.NotNull()
    java.time.LocalDate to) {
        return 0L;
    }
    
    public final double valueAt(long daysSinceBirth, int period) {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String phaseLabel(double value, double nextValue) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final biorritmo.BiorhythmResult calculate(@org.jetbrains.annotations.NotNull()
    java.time.LocalDate birthDate, @org.jetbrains.annotations.NotNull()
    java.time.LocalDate selectedDate) {
        return null;
    }
}