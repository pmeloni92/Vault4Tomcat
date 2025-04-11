package org.apache.vault4tomcat.vault.api;

import org.apache.vault4tomcat.config.VaultConfig;
import org.apache.vault4tomcat.vault.VaultException;
import org.apache.vault4tomcat.vault.response.LogicalResponse;
import org.apache.vault4tomcat.vault.rest.Rest;
import org.apache.vault4tomcat.vault.rest.RestException;
import org.apache.vault4tomcat.vault.rest.RestResponse;

import java.util.Map;


public class LogicalUtilities {

    /**
     * Utility class – not meant to be instantiated.
     */
    private LogicalUtilities() {
        // No-op.
    }

    /**
     * Reads a secret using the KV v2 engine.
     * @param path       The logical path to the secret (e.g., "secret/myapp").
     * @param config     Vault configuration (includes token, address, etc.).
     * @param nameSpace  Optional Vault namespace (Vault Enterprise).
     * @return LogicalResponse containing the secret data and metadata.
     * @throws VaultException if the secret cannot be retrieved.
     */
    public static LogicalResponse readV2(final String path, final VaultConfig config, final String nameSpace)
            throws VaultException {

        final String endpoint = "/v1/" + pathPrefix(path) + "/data/" + cleanPath(path);

        try {
            final Rest rest = new Rest()
                    .url(config.getAddress() + endpoint)
                    .header("X-Vault-Token", config.getToken())
                    .header("X-Vault-Request", "true")
                    .connectTimeoutSeconds(config.getOpenTimeout())
                    .readTimeoutSeconds(config.getReadTimeout());

            if (nameSpace != null && !nameSpace.isEmpty()) {
                rest.header("X-Vault-Namespace", nameSpace);
            }

            final RestResponse response = rest.get();
            return new LogicalResponse(response);
        } catch (RestException e) {
            throw new VaultException("Failed to read secret at path: " + path + " " + e);
        }
    }

    /**
     * Prepends the Vault mount path for the KV v2 engine. This method assumes
     * KV v2 is mounted at "secret/" unless specified differently in future logic.
     * @param path The user-provided path.
     * @return The mount path prefix.
     */
    private static String pathPrefix(String path) {
        // TODO: assumes default mount is "secret"
        return "secret";
    }

    /**
     * Removes leading slashes to sanitize the Vault path.
     * @param path The input path.
     * @return A cleaned path suitable for URL construction.
     */
    private static String cleanPath(String path) {
        return path.replaceFirst("^/", "");
    }
}
