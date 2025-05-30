package org.apache.vault4tomcat.vault;

import org.apache.vault4tomcat.config.VaultConfig;
import org.apache.vault4tomcat.vault.api.Logical;

public interface Vault {

    static Vault create(VaultConfig vaultConfig) {
        return new VaultImpl(vaultConfig);
    }

    /**
     * Returns the implementing class for Vault's core/logical operations (e.g. read, write).
     *
     * @return The implementing class for Vault's core/logical operations (e.g. read, write)
     */
    Logical logical();

    VaultConfig getConfig();

}