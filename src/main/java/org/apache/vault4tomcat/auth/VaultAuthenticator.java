package org.apache.vault4tomcat.auth;


import org.apache.vault4tomcat.config.VaultConfig;

/**
 * Interface for authentication strategies used to obtain a Vault token.
 * Each authentication method should implement this interface.
 */
public interface VaultAuthenticator {
    /**
     * Authenticate to Vault using the given config and return a client token.
     * @param config Vault configuration (with necessary credentials for auth).
     * @return A Vault client token string that can be used to access secrets.
     * @throws Exception if authentication fails (e.g., network or credentials error).
     */
    String authenticate(VaultConfig config) throws Exception;
}
