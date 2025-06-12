package org.apache.vault4tomcat.vault.api;

import org.apache.vault4tomcat.config.VaultConfig;
import org.apache.vault4tomcat.vault.VaultException;
import org.apache.vault4tomcat.vault.response.LogicalResponse;
import org.apache.vault4tomcat.vault.rest.VaultHttpClient;
import org.apache.vault4tomcat.vault.rest.RestException;
import org.apache.vault4tomcat.vault.rest.RestResponse;

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
            final VaultHttpClient vaultHttpClient = new VaultHttpClient()
                    .url(config.getAddress() + endpoint)
                    .header("X-Vault-Token", config.getToken())
                    .header("X-Vault-Request", "true")
                    .connectTimeoutSeconds(config.getOpenTimeout())
                    .readTimeoutSeconds(config.getReadTimeout());

            if (nameSpace != null && !nameSpace.isEmpty()) {
                vaultHttpClient.header("X-Vault-Namespace", nameSpace);
            }

            final RestResponse response = vaultHttpClient.get();
            return new LogicalResponse(response, "readV2");
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

    public static LogicalResponse appRoleLogin(final VaultConfig config, final String roleId, final String secretId)
            throws VaultException {
        final String endpoint = "/v1/" + "auth/approle/login";

        try {
            final VaultHttpClient vaultHttpClient = new VaultHttpClient()
                    .url(config.getAddress() + endpoint)
                    .header("Content-Type", "application/json")
                    .connectTimeoutSeconds(config.getOpenTimeout())
                    .readTimeoutSeconds(config.getReadTimeout());

            StringBuilder body = new StringBuilder();
            body.append("{\"role_id\":\"").append(escapeJson(roleId)).append("\"");
            if (secretId != null && !secretId.isEmpty()) {
                body.append(",\"secret_id\":\"").append(escapeJson(secretId)).append("\"");
            }
            body.append("}");
            final RestResponse response = vaultHttpClient.post(body.toString());
            return new LogicalResponse(response, "login");
        } catch (RestException e) {
            throw new VaultException("Failed to login with the provided approle id and secret " + e);
        }
    }

    protected static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
