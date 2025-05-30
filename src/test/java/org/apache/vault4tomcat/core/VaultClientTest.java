package org.apache.vault4tomcat.core;

import org.apache.vault4tomcat.config.VaultConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VaultClientTest {

    private static VaultConfig testConfig;
    // Set up configuration for tests (could also use VaultConfig.loadFromEnvironment())
    //VaultConfig testConfig = new VaultConfig("http://127.0.0.1:8200", "my-root-token");

    @BeforeAll
    static void setupConfig() {
        // Use environment variables or hard-coded values for Vault address and token
        String addr = System.getenv("VAULT_ADDR");
        String token = System.getenv("VAULT_TOKEN");
        if (addr == null) addr = "http://127.0.0.1:8200";
        if (token == null) token = "my-root-token";  // dev server root token
        testConfig = new VaultConfig(addr, token);
        // TODO:  namespace needed for enterprise tests, set config.setNameSpace() here
    }

    @Test
    void testSecretRetrievalSuccess() throws Exception {
        VaultClient client = new VaultClient(testConfig);
        // Attempt to retrieve a known secret (assumes "secret/myapp/config" exists in Vault)
        Map<String, String> secretData = client.getSecret("myapp/config");
        assertNotNull(secretData, "Secret data should not be null");
        // For example, if "secret/myapp/config" contains a key "username" = "admin", test it:
        assertEquals("admin", secretData.get("username"), "Secret 'username' should match expected value");
        assertEquals("s3cr3t", secretData.get("password"), "Secret 'username' should match expected value");
    }

    @Test
    void testInvalidTokenThrowsException() {
        // Use an invalid token to ensure authentication fails
        VaultConfig badConfig = new VaultConfig("http://127.0.0.1:8200", "invalid-token");
        // VaultClient construction or secret retrieval should throw an exception due to bad token
        assertThrows(Exception.class, () -> {
            VaultClient badClient = new VaultClient(badConfig);
            badClient.getSecret("myapp/config");
        }, "Expected an exception for invalid Vault token or unreachable Vault");
    }
}
