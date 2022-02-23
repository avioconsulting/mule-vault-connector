#!/bin/sh

export VAULT_ADDR=https://127.0.0.1:8205
export VAULT_TOKEN=test_token

ln -s /vault/ca/vault-example.pem /usr/local/share/ca-certificates/
update-ca-certificates

vault secrets enable pki
vault secrets tune -max-lease-ttl=87600h pki

vault secrets enable -path=pki_int pki
vault secrets tune -max-lease-ttl=43800h pki_int

vault write pki_int/roles/vault allow_any_name=true max_ttl="720h" generate_lease=true

# save vault-tls.hcl file
vault policy write vault-tls /vault/prepare/vault-tls.hcl

vault write pki_int/roles/vault-cert allow_any_name=true max_ttl="720h" generate_lease=true
# save vault-cert.hcl file
vault policy write vault-cert /vault/prepare/vault-cert.hcl

vault auth enable cert

vault write auth/cert/certs/vault-cert display_name=vault-cert policies=vault-cert certificate=@/vault/ca/vault-example.pem