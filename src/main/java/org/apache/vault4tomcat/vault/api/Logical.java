package org.apache.vault4tomcat.vault.api;

import org.apache.vault4tomcat.config.VaultConfig;
import org.apache.vault4tomcat.vault.VaultException;
import org.apache.vault4tomcat.vault.response.LogicalResponse;

/**
 * Provides access to the Vault logical secret engine (read-only for KV v2).
 * This class is used internally by VaultClient to retrieve secrets.
 */
public class Logical {

    private String nameSpace;
    private final VaultConfig config;

    /**
     * Constructs a Logical instance using the provided Vault configuration.
     *
     * @param config VaultConfig containing the Vault address and token
     */
    public Logical(final VaultConfig config) {
        this.config = config;
        if (this.config.getNameSpace() != null && !this.config.getNameSpace().isEmpty()) {
            this.nameSpace = this.config.getNameSpace();
        }
    }

    /**
     * Optional namespace support (not typically used unless Vault Enterprise is configured with namespaces).
     *
     * @param nameSpace the Vault namespace to use for requests.
     * @return this Logical instance for chaining.
     */
    public Logical withNameSpace(final String nameSpace) {
        this.nameSpace = nameSpace;
        return this;
    }

    /**
     * Reads a secret from Vault using the KV v2 engine. This is the default mode for Vault4Tomcat.
     * It calls the appropriate path (/v1/<mount>/data/<path>) and parses nested fields.
     *@param path the Vault secret path (e.g. "secret/myapp")
     *@return the parsed response from Vault
     *@throws VaultException if the request fails or Vault returns a non-200 response
     */
    public LogicalResponse read(final String path) throws VaultException {
        return read(path, "readV2");
    }

    /**
     * Internal method to perform the actual read operation.
     * This will call the underlying REST client and parse the logical response.
     * @param path the secret path
     * @param operation a tag to describe the operation, here fixed as "readV2"
     * @return the logical response containing secret data
     * @throws VaultException if any errors occur during the REST call
     */
    private LogicalResponse read(final String path, final String operation) throws VaultException {
        return LogicalUtilities.readV2(path, config, nameSpace);
    }

}
