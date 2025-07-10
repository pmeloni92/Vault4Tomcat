package org.apache.vault4tomcat.auth;

import org.apache.vault4tomcat.config.VaultConfig;
import org.apache.vault4tomcat.core.VaultClient;
import org.apache.vault4tomcat.vault.Vault;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class VaultAuthenticatorsTest {
    @Test
    public void testAppRoleAuthenticatorSuccess() throws Exception {

        Properties props = new Properties();
        props.setProperty(VaultConfig.VAULT_ADDR, "http://127.0.0.1:8200");
        props.setProperty(VaultConfig.AUTH_METHOD, "approle");
        //TODO: Those should be changed at each run. We should create something to mock those.
        props.setProperty(VaultConfig.APPROLE_ROLE_ID, "7b646921-d109-ade8-3980-a3bde1be4572");
        props.setProperty(VaultConfig.APPROLE_SECRET_ID, "1d4daf9f-bf63-b146-57ce-322a8ff4c025");
        Path tempFile = Files.createTempFile("vault", ".properties");
        try (OutputStream os = Files.newOutputStream(tempFile)) {
            props.store(os, null);
        }

        VaultConfig vaultConfig = new VaultConfig(tempFile.toString());
        String token = new AppRoleAuthentication().authenticate(Vault.create(vaultConfig));
        //TODO: If failed to login, secretId has expired
        assertFalse(token.isEmpty());

        VaultClient client = new VaultClient(vaultConfig);
        Map<String, String> secretData = client.getSecret("myapp/config");
        assertNotNull(secretData, "Secret data should not be null");
//        assertTrue(token.isRenewable());
//        assertEquals(120, token.getLeaseDuration());
    }

    @Test
    public void testAwsIamAuthenticatorWithoutKeys() throws Exception {

        Properties props = new Properties();
        props.setProperty(VaultConfig.VAULT_ADDR, "http://127.0.0.1:8200");
        props.setProperty(VaultConfig.AUTH_METHOD, "awsiam");
        props.setProperty(VaultConfig.AWS_ROLE, "dev-role-iam");
        props.setProperty(VaultConfig.AWS_ACCESS_KEY, "");
        props.setProperty(VaultConfig.AWS_SECRET_KEY, "");
        props.setProperty(VaultConfig.AWS_SESSION_TOKEN, "");
        props.setProperty(VaultConfig.AWS_HEADER_VALUE, "vault.example.com");
        Path tempFile = Files.createTempFile("vault", ".properties");
        try (OutputStream os = Files.newOutputStream(tempFile)) {
            props.store(os, null);
        }

        VaultConfig vaultConfig = new VaultConfig(tempFile.toString());
        String token = new AwsIamAuthentication().authenticate(Vault.create(vaultConfig));
        assertFalse(token.isEmpty());

        VaultClient client = new VaultClient(vaultConfig);
        Map<String, String> secretData = client.getSecret("myapp/config");
        assertNotNull(secretData, "Secret data should not be null");
    }

}
