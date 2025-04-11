package org.apache.vault4tomcat.config;

import org.apache.vault4tomcat.vault.VaultException;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

/**
 * Holds configuration details for connecting to HashiCorp Vault.
 * Supports loading values from a properties file or environment variables.
 * <p>
 * Fields include Vault address, authentication credentials for multiple methods (e.g., token, AppRole).
 * <p>
 * Example usage:
 *   VaultConfig config = VaultConfig.loadFromEnvironment();
 */
public class VaultConfig implements Serializable {

    private String address;
    private String token;
    private String appRoleId;
    private String appRoleSecret;

    private String nameSpace;

    private int openTimeout = 5;
    private int readTimeout = 30;

    private boolean sslVerify = true;

    public VaultConfig() {

    }

    public VaultConfig(String address, String token) {
        this.address = address;
        this.token = token;
    }
    /**
     * <p>Optional. Sets a global namespace to the Vault server instance, if desired. Otherwise,
     * namespace can be applied individually to any read / write / auth call.
     *
     * <p>Namespace support requires Vault Enterprise Pro, please see
     * <a href="https://learn.hashicorp.com/vault/operations/namespaces">https://learn.hashicorp.com/vault/operations/namespaces</a></p>
     *
     * @param nameSpace The namespace to use globally in this VaultConfig instance.
     * @return This object, with the namespace populated, ready for additional builder-pattern
     * method calls or else finalization with the build() method
     * @throws VaultException If any error occurs
     */
    public VaultConfig setNameSpace(final String nameSpace) throws VaultException {
        if (nameSpace != null && !nameSpace.isEmpty()) {
            this.nameSpace = nameSpace;
            return this;
        } else {
            throw new VaultException("A namespace cannot be empty.");
        }
    }

    /**
     * <p>Sets the address (URL) of the Vault server instance to which API calls should be sent.
     * E.g. <code><a href="http://127.0.0.1:8200">http://127.0.0.1:8200</a></code>.</p>
     *
     * <p><code>address</code> is required for the Vault driver to function.
     *
     * @param address The Vault server base URL
     * @return This object, with address populated, ready for additional builder-pattern method
     * calls or else finalization with the build() method
     */
    public VaultConfig setAddress(final String address) {
        this.address = address.trim();
        if (this.address.endsWith("/")) {
            this.address = this.address.substring(0, this.address.length() - 1);
        }
        return this;
    }

    /**
     * <p>Sets the token used to access Vault.</p>
     *
     * <p>There are some cases where you might want to instantiate a <code>VaultConfig</code>
     * object without a token (e.g. you plan to retrieve a token programmatically, with a call to
     * the "userpass" auth backend, and populate it prior to making any other API calls).</p>
     *
     * @param token The token to use for accessing Vault
     * @return This object, with token populated, ready for additional builder-pattern method calls
     * or else finalization with the build() method
     */
    public VaultConfig setToken(final String token) {
        this.token = token;
        return this;
    }

    /**
     * <p>The number of seconds to wait before giving up on establishing an HTTP(S) connection to
     * the Vault server.</p>
     *
     * <p>If no openTimeout is explicitly set, then the object will look to the
     * <code>VAULT_OPEN_TIMEOUT</code>
     * environment variable.</p>
     *
     * @param openTimeout Number of seconds to wait for an HTTP(S) connection to successfully
     * establish
     * @return This object, with openTimeout populated, ready for additional builder-pattern method
     * calls or else finalization with the build() method
     */
    public VaultConfig setOpenTimeout(final Integer openTimeout) {
        this.openTimeout = openTimeout;
        return this;
    }

    /**
     * <p>After an HTTP(S) connection has already been established, this is the number of seconds
     * to wait for all data to finish downloading.</p>
     *
     * <p>If no readTimeout is explicitly set, then the object will look to the
     * <code>VAULT_READ_TIMEOUT</code>
     * environment variable.</p>
     *
     * @param readTimeout Number of seconds to wait for all data to be retrieved from an established
     * HTTP(S) connection
     * @return This object, with readTimeout populated, ready for additional builder-pattern method
     * calls or else finalization with the build() method
     */
    public VaultConfig setReadTimeout(final Integer readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public void setSslVerify(boolean sslVerify) {
        this.sslVerify = sslVerify;
    }

    public String getAddress() {
        return address;
    }

    public String getToken() {
        return token;
    }

    public Integer getOpenTimeout() {
        return openTimeout;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public boolean isSslVerify() {
        return sslVerify;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public String getAppRoleId() { return appRoleId; }
    public void setAppRoleId(String appRoleId) { this.appRoleId = appRoleId; }

    public String getAppRoleSecret() { return appRoleSecret; }
    public void setAppRoleSecret(String appRoleSecret) { this.appRoleSecret = appRoleSecret; }

    /**
     * Load configuration from a .properties file.
     */
    public static org.apache.vault4tomcat.config.VaultConfig loadFromProperties(String filePath) throws Exception {
        Properties props = new Properties();
        try (InputStream is = new FileInputStream(filePath)) {
            props.load(is);
        }
        org.apache.vault4tomcat.config.VaultConfig config = new org.apache.vault4tomcat.config.VaultConfig();
        config.setAddress(props.getProperty("vault.address", "http://127.0.0.1:8200"));
        config.setToken(props.getProperty("vault.token"));
        config.setAppRoleId(props.getProperty("vault.appRoleId"));
        config.setAppRoleSecret(props.getProperty("vault.appRoleSecret"));
        return config;
    }

    /**
     * Load configuration from environment variables.
     * (Uses standard Vault env vars like VAULT_ADDR, VAULT_TOKEN, etc.)
     */
    public static org.apache.vault4tomcat.config.VaultConfig loadFromEnvironment() {
        org.apache.vault4tomcat.config.VaultConfig config = new org.apache.vault4tomcat.config.VaultConfig();
        String addr = System.getenv("VAULT_ADDR");
        String token = System.getenv("VAULT_TOKEN");
        config.setAddress(addr != null ? addr : "http://127.0.0.1:8200");
        if (token != null) config.setToken(token);
        // Similarly load other possible env vars for different auth methods
        if (System.getenv("VAULT_ROLE_ID") != null) {
            config.setAppRoleId(System.getenv("VAULT_ROLE_ID"));
        }
        if (System.getenv("VAULT_SECRET_ID") != null) {
            config.setAppRoleSecret(System.getenv("VAULT_SECRET_ID"));
        }
        return config;
    }
}

