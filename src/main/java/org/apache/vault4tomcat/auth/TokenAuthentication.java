package org.apache.vault4tomcat.auth;

import org.apache.vault4tomcat.config.VaultConfig;

/**
 * VaultAuthenticator implementation for static token authentication.
 * Uses the token provided in the VaultConfig directly (no API call needed).
 */
public class TokenAuthentication implements VaultAuthenticator {

    /**
     * Returns the static Vault token defined in the VaultConfig.
     *
     * @param config VaultConfig containing the token
     * @return the Vault token
     * @throws IllegalArgumentException if the token is missing or empty
     */
    @Override
    public String authenticate(VaultConfig config) throws IllegalArgumentException {
        String token = config.getToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Vault token not provided in configuration");
        }
        return token;
    }

}
