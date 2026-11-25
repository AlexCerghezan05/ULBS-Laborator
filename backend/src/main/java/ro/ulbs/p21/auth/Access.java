package ro.ulbs.p21.auth;

/**
 * Nivelurile de acces din D-012, pentru {@code @PreAuthorize} pe fiecare rută (PATTERN-004).
 * Admin poate tot; Operator scrie, fără rapoarte și setări; Contabil citește, cu rapoarte și export.
 */
public final class Access {

    /** Setări firmă, serii, utilizatori, reminder-e. */
    public static final String ADMIN = "hasRole('ADMIN')";
    /** Emitere și modificare: facturi, clienți, produse, încasări. */
    public static final String WRITE = "hasAnyRole('ADMIN', 'OPERATOR')";
    /** Rapoartele. */
    public static final String REPORTS = "hasAnyRole('ADMIN', 'CONTABIL')";
    /** Citire, pentru oricare din cele trei roluri. */
    public static final String READ = "hasAnyRole('ADMIN', 'OPERATOR', 'CONTABIL')";

    private Access() {
    }
}
