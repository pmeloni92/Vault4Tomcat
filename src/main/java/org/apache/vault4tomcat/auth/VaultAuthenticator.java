package org.apache.vault4tomcat.auth;

import org.apache.vault4tomcat.vault.Vault;

/**
 * Interface for authentication strategies used to obtain a Vault token.
 * Each authentication method should implement this interface.
 */
public interface VaultAuthenticator {

    String CLIENT_TOKEN = "client_token";

    /**
     * Authenticate to Vault and return a client token.
     * @param vault Vault configuration (with necessary credentials for auth).
     * @return A Vault client token string that can be used to access secrets.
     * @throws Exception if authentication fails (e.g., network or credentials error).
     */
    String authenticate(Vault vault) throws Exception;
}
