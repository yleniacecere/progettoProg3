package it.ylenia.telefonia;

import java.util.Arrays;

/** Accesso dell'amministratore predisposto dal sistema per la simulazione. */
public final class AdminAccess {
    public void login(String username, char[] password) {
        if (!"admin".equals(username) || !Arrays.equals("admin".toCharArray(), password))
            throw new IllegalArgumentException("Username o password non corretti.");
    }
}
