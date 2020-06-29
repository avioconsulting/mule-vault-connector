package com.avioconsulting.mule.vault.api.client;

public final class VaultConstants {

    private VaultConstants() {
        super();
    }

    public static final String VAULT_API_PATH = "/v1";

    // Header Constants
    public static final String VAULT_TOKEN_HEADER = "X-Vault-Token";
    public static final String VAULT_NAMESPACE_HEADER = "X-Vault-Namespace";
    public static final String VAULT_REQUEST_HEADER = "X-Vault-Request";

    // Payload Constants
    public static final String CIPHERTEXT_ATTRIBUTE = "ciphertext";
    public static final String PLAINTEXT_ATTRIBUTE = "plaintext";
    public static final String DATA_ATTRIBUTE = "data";
}
