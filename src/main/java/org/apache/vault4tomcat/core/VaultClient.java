package org.apache.vault4tomcat.core;

import org.apache.vault4tomcat.auth.VaultAuthenticator;
import org.apache.vault4tomcat.vault.Vault;
import org.apache.vault4tomcat.config.VaultConfig;
import org.apache.vault4tomcat.vault.VaultException;
import org.apache.vault4tomcat.vault.response.LogicalResponse;

import java.util.Map;

/**
 * Client that interacts with HashiCorp Vault to fetch secrets using the authenticated token.
 * Initializes via VaultAuthenticator and maintains a Vault driver instance.
 */
public class VaultClient {
    private final VaultConfig config;
    private final VaultAuthenticator authenticator;
    private Vault vault;              // Vault driver client for making API calls
    private String clientToken;       // Vault token obtained after authentication

    public VaultClient(VaultConfig config, VaultAuthenticator authenticator) throws Exception {
        this.config = config;
        this.authenticator = authenticator;
        this.clientToken = authenticator.authenticate(config);
        if (clientToken == null || clientToken.isEmpty()) {
            throw new Exception("Failed to obtain Vault token via authentication.");
        }
        this.vault = Vault.create(config);
    }

    /**
     * Retrieve a secret from Vault at the given path.
     * @param path The Vault logical path of the secret (e.g., "secret/myapp/config").
     * @return A map of key-value pairs stored at that secret path.
     * @throws Exception if the secret cannot be retrieved (authentication or connectivity issues).
     */
    public Map<String, String> getSecret(String path) throws Exception {
        try {
            LogicalResponse response = vault.logical().read(path);
            return response.getData();
        } catch (VaultException e) {
            // TODO: Add custom exception
            throw new Exception("Error retrieving secret from Vault: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves a specific value from a Vault secret at the given path.
     * @param path the path in Vault (e.g., "secret/myapp/config").
     * @param key the key to extract from the secret data.
     * @return the secret value associated with the given key.
     * @throws Exception if the secret or key cannot be found (authentication or connectivity issues).
     */
    public String getSecretValue(String path, String key) throws Exception {
        Map<String, String> data = getSecret(path);
        return (data != null) ? data.get(key) : null;
    }
}
