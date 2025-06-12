package org.apache.vault4tomcat.auth;

import org.apache.vault4tomcat.vault.Vault;

/**
 * VaultAuthenticator implementation for static token authentication.
 * Uses the token provided in the VaultConfig directly (no API call needed).
 */
public class TokenAuthentication implements VaultAuthenticator {

    /**
     * Returns the static Vault token defined in the VaultConfig.
     *
     * @param vault VaultConfig containing the token
     * @return the Vault token
     * @throws IllegalArgumentException if the token is missing or empty
     */
    @Override
    public String authenticate(Vault vault) throws IllegalArgumentException {
        String token = vault.getConfig().getToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Vault token not provided in configuration");
        }
        return token;
    }

}
