package org.apache.vault4tomcat.config;

import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class VaultConfigTest {
    @Test
    void testLoadFromPropertiesFile() throws Exception {
        Properties props = new Properties();
        props.setProperty("vault.address", "http://127.0.0.1:8200");
        props.setProperty("vault.token", "s.123456");  // sample token
        Path tempFile = Files.createTempFile("vaultcfg", ".properties");
        try (OutputStream os = Files.newOutputStream(tempFile)) {
            props.store(os, null);
        }

        VaultConfig cfg = VaultConfig.loadFromProperties(tempFile.toString());
        assertEquals("http://127.0.0.1:8200", cfg.getAddress());
        assertEquals("s.123456", cfg.getToken());
        assertNull(cfg.getAppRoleId(), "AppRoleId should not be set by this file");
        Files.delete(tempFile);
    }

    @Test
    void testLoadFromEnvironmentDefaults() {
        // Ensure no relevant env vars are set (or simulate a clean environment).
        VaultConfig cfg = VaultConfig.loadFromEnvironment();
        assertEquals("http://127.0.0.1:8200", cfg.getAddress(), "Default address should be used if VAULT_ADDR not set");
        assertNull(cfg.getToken(), "Token should be null if not set in env");
    }

    @Test
    void testFluentSetters() {
        VaultConfig cfg = new VaultConfig()
                .setAddress("http://localhost:8200/")
                .setToken("test-token")
                .setOpenTimeout(2)
                .setReadTimeout(5);
        assertEquals("http://localhost:8200", cfg.getAddress(), "Trailing slash should be removed from address");
        assertEquals("test-token", cfg.getToken());
        assertEquals(2, cfg.getOpenTimeout());
        assertEquals(5, cfg.getReadTimeout());
    }
}
