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
        props.setProperty(VaultConfig.VAULT_ADDR, "http://127.0.0.1:8200");
        props.setProperty(VaultConfig.VAULT_TOKEN, "s.123456");
        Path tempFile = Files.createTempFile("vaultcfg", ".properties");
        try (OutputStream os = Files.newOutputStream(tempFile)) {
            props.store(os, null);
        }

        VaultConfig cfg = new VaultConfig(tempFile.toString());
        assertEquals("http://127.0.0.1:8200", cfg.getAddress());
        assertEquals("s.123456", cfg.getToken());
        assertNull(cfg.getAppRoleId(), "AppRoleId should not be set by this file");
        Files.delete(tempFile);
    }
}