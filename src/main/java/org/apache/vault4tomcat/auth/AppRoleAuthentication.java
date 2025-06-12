package org.apache.vault4tomcat.auth;

import org.apache.vault4tomcat.vault.Vault;
import org.apache.vault4tomcat.vault.response.LogicalResponse;

/**
 * VaultAuthenticator implementation for <a href="https://developer.hashicorp.com/vault/docs/auth/approle">app role authentication</a>.
 *
 */
public class AppRoleAuthentication implements VaultAuthenticator {
    /**
     * Makes an API call to fetch client token using app role id and secret id defined in the VaultConfig.
     *
     * @param vault VaultConfig containing the app role id and the secret id
     * @return the Vault token
     * @throws IllegalArgumentException if the app role id is missing or empty
     */
    @Override
    public String authenticate(Vault vault) throws Exception {
        String roleId = vault.getConfig().getAppRoleId();
        if (roleId == null || roleId.isEmpty()) {
            throw new IllegalArgumentException("AppRole authentication requires role_id");
        }
        String secretId = vault.getConfig().getAppRoleSecretId();

        LogicalResponse logicalResponse = vault.logical().login(roleId, secretId);

        String token = logicalResponse.getData().get(CLIENT_TOKEN);
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Vault token not provided in configuration");
        }
        return token;
    }


}
