#!/usr/bin/env bash

# Assumes vault is in $PATH.
# Kills any running vault server.

# Kill any running vault servers
kill $(ps ax |grep 'vault server' |awk '{print $1}') 2>/dev/null
set -e

# Start the Vault server
sleep 1
vault server -dev -dev-root-token-id=devroot -log-level=debug > /tmp/vault.log 2>&1 &
sleep 1
export VAULT_TOKEN=devroot
export VAULT_ADDR=http://127.0.0.1:8200

# Write a sample secret to the KV secrets engine
vault kv put secret/samples/sample1 attribute1="This value came from the Vault Properties Provider"

# Enable Transit Secrets Engine and create a key to use
vault secrets enable transit
vault write -f transit/keys/demo-key