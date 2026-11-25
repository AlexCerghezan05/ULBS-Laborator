package ro.ulbs.p21.auth;

import java.io.Serializable;

/** Utilizatorul logat, ținut în sesiune (D-039). Rolul e unul singur (D-029). */
public record AppPrincipal(long id, String username, String fullName, String role) implements Serializable {
}
