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
    // AWS
    public static final String AWS_ROLE = "vault.auth.aws.role";
    public static final String AWS_ACCESS_KEY = "vault.auth.aws.access_key";
    public static final String AWS_SECRET_KEY = "vault.auth.aws.secret_key";
    public static final String AWS_SESSION_TOKEN = "vault.auth.aws.session_token";
    public static final String AWS_HEADER_VALUE = "vault.auth.aws.header_value";
    public static final String AWS_REGION = "vault.auth.aws.region";

    private final String address;
    private String authMethod;
    private String token;

    private String appRoleId;
    private String appRoleSecretId;

    private String awsRole;
    private String awsAccessKey;
    private String awsSecretKey;
    private String awsSessionToken;
    private String awsHeaderValue;
    private String awsRegion;

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
        if ((env = System.getenv("VAULT_AUTH_AWS_ROLE")) != null) props.setProperty(AWS_ROLE, env);
        if ((env = System.getenv("VAULT_AUTH_AWS_ACCESS_KEY")) != null) props.setProperty(AWS_ACCESS_KEY, env);
        if ((env = System.getenv("VAULT_AUTH_AWS_SECRET_KEY")) != null) props.setProperty(AWS_SECRET_KEY, env);
        if ((env = System.getenv("VAULT_AUTH_AWS_SESSION_TOKEN")) != null) props.setProperty(AWS_SESSION_TOKEN, env);
        if ((env = System.getenv("VAULT_AUTH_AWS_HEADER_VALUE")) != null) props.setProperty(AWS_HEADER_VALUE, env);
        if ((env = System.getenv("VAULT_AUTH_AWS_REGION")) != null) props.setProperty(AWS_REGION, env);

        this.address = props.getProperty(VAULT_ADDR, "http://127.0.0.1:8200");
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

        this.awsRole = props.getProperty(AWS_ROLE);
        this.awsAccessKey = props.getProperty(AWS_ACCESS_KEY);
        this.awsSecretKey = props.getProperty(AWS_SECRET_KEY);
        this.awsSessionToken = props.getProperty(AWS_SESSION_TOKEN);
        this.awsHeaderValue = props.getProperty(AWS_HEADER_VALUE);
        this.awsRegion = props.getProperty(AWS_REGION, "us-east-1");

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
     */
    public void setToken(final String token) { this.token = token; }

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

    public String getAppRoleId() { return appRoleId; }

    public String getAppRoleSecretId() { return appRoleSecretId; }

    public String getAuthMethod() { return authMethod; }

    public String getAwsRole() { return awsRole; }

    public String getAwsAccessKey() { return awsAccessKey; }

    public String getAwsSecretKey() { return awsSecretKey; }

    public String getAwsSessionToken() { return awsSessionToken; }

    public String getAwsHeaderValue() { return awsHeaderValue; }

    public String getAwsRegion() { return awsRegion; }
}

