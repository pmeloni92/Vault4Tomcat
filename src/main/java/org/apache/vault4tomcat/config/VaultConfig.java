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

    // Config property keys
    public static final String VAULT_ADDR = "vault.address";
    public static final String VAULT_TOKEN = "vault.token";
    public static final String AUTH_METHOD = "vault.auth.method";
    // AppRole
    public static final String APPROLE_ROLE_ID = "vault.auth.approle.role_id";
    public static final String APPROLE_SECRET_ID = "vault.auth.approle.secret_id";

    private String address;
    private String authMethod;
    private String token;
    private String appRoleId;
    private String appRoleSecretId;

    private String nameSpace;

    private int openTimeout = 5;
    private int readTimeout = 30;

    private boolean sslVerify = true;

    public VaultConfig(final String filePath) throws Exception {
        Properties props = new Properties();

        if (filePath != null && !filePath.trim().isEmpty()) {
            try (InputStream is = new FileInputStream(filePath)) {
                props.load(is);
            }
        }

        String env;
        if ((env = System.getenv("VAULT_ADDR")) != null) props.setProperty(VAULT_ADDR, env);
        if ((env = System.getenv("VAULT_TOKEN")) != null) props.setProperty(VAULT_TOKEN, env);
        if ((env = System.getenv("VAULT_AUTH_METHOD")) != null) props.setProperty(AUTH_METHOD, env);
        if ((env = System.getenv("VAULT_AUTH_APPROLE_ROLE_ID")) != null) props.setProperty(APPROLE_ROLE_ID, env);
        if ((env = System.getenv("VAULT_AUTH_APPROLE_SECRET_ID")) != null) props.setProperty(APPROLE_SECRET_ID, env);

        this.address = props.getProperty(VAULT_ADDR, "http://127.0.0.1:8200");
        if (this.address.isEmpty()) {
            throw new IllegalArgumentException("Vault address must be specified.");
        }

        this.token = props.getProperty(VAULT_TOKEN);
        String method = props.getProperty(AUTH_METHOD);
        if (method == null || method.isEmpty()) {
            method = !this.token.isEmpty() ? "token" : null;
        }
        this.authMethod = method;
        if (this.authMethod == null || this.authMethod.isEmpty()) {
            throw new IllegalArgumentException("Vault auth method must be specified (vault.auth.method or VAULT_AUTH_METHOD)");
        }

        this.appRoleId = props.getProperty(APPROLE_ROLE_ID);
        this.appRoleSecretId = props.getProperty(APPROLE_SECRET_ID);

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

    public String getNameSpace() {
        return nameSpace;
    }

    /**
     * <p>Sets the address (URL) of the Vault server instance to which API calls should be sent.
     * E.g. <code><a href="http://127.0.0.1:8200">http://127.0.0.1:8200</a></code>.</p>
     *
     * <p><code>address</code> is required for the Vault driver to function.
     *
     * @param address The Vault server base URL
     */
    public void setAddress(final String address) {
        this.address = address.trim();
        if (this.address.endsWith("/")) {
            this.address = this.address.substring(0, this.address.length() - 1);
        }
    }

    public String getAddress() {
        return address;
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

    public String getToken() {
        return token;
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
     */
    public void setOpenTimeout(final Integer openTimeout) { this.openTimeout = openTimeout; }

    public Integer getOpenTimeout() {
        return openTimeout;
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
     */
    public void setReadTimeout(final Integer readTimeout) { this.readTimeout = readTimeout; }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public void setSslVerify(boolean sslVerify) {
        this.sslVerify = sslVerify;
    }
    public boolean isSslVerify() { return sslVerify; }

    public void setAppRoleId(String appRoleId) { this.appRoleId = appRoleId; }
    public String getAppRoleId() { return appRoleId; }

    public void setAppRoleSecretId(String appRoleSecretId) { this.appRoleSecretId = appRoleSecretId; }
    public String getAppRoleSecretId() { return appRoleSecretId; }

    public void setAuthMethod(String authMethod) { this.authMethod = authMethod; }
    public String getAuthMethod() { return authMethod; }

}

