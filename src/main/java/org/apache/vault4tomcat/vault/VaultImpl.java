package org.apache.vault4tomcat.vault;

import org.apache.vault4tomcat.config.VaultConfig;
import org.apache.vault4tomcat.vault.api.Logical;

import java.util.logging.Logger;

/**
 * Main Vault API implementation used to access logical secrets from HashiCorp Vault.
 * <p>
 * VaultImpl connects to Vault using provided VaultConfig and allows access to the logical
 * secret engine (typically used for key-value storage).
 * <p>
 * Example usage:
 * <pre>{@code
 * VaultConfig config = VaultConfig.loadFromEnvironment();
 * Vault vault = new VaultImpl(config);
 * Map<String, String> secret = vault.logical().read("secret/myapp").getData();
 * }</pre>
 */
public class VaultImpl implements Vault {

    private final VaultConfig vaultConfig;
    private final Logger logger = Logger.getLogger(VaultImpl.class.getCanonicalName());

    /**
     * Constructs a new VaultImpl instance using the given Vault configuration.
     *
     * @param vaultConfig the configuration for connecting to Vault
     */
    public VaultImpl(final VaultConfig vaultConfig) {
        this.vaultConfig = vaultConfig;
        if (this.vaultConfig.getNameSpace() != null && !this.vaultConfig.getNameSpace().isEmpty()) {
            logger.info(String.format(
                    "The NameSpace %s has been bound to this Vault instance. Please keep this in mind when running operations.",
                    this.vaultConfig.getNameSpace()));
        }
    }

    /**
     * Provides access to Vault's logical secret backend (KV engine).
     *
     * @return Logical instance to read/write secrets
     */
    @Override
    public Logical logical() {
        return new Logical(vaultConfig);
    }

    /**
     * Returns the active Vault configuration.
     *
     * @return the VaultConfig instance
     */
    @Override
    public VaultConfig getConfig() {
        return vaultConfig;
    }
}
