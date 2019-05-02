# Executing the Vault Connector Demo

## Step 0: Install prerequisites

You will need Vault locally and on your PATH

## Step 1: Start Vault

Execute setupDemo.sh to kill any previously running Vault server instances and start a new instance. 

It will add a new secret at secrets/stamples/sample1, enable the transit secret engine, and create a key called demo-key

## Step 2: Execute the Demo application

Open the demo application (vault-connector-demo) with Anypoint Studio and start the application as a Mule Application

### Executing get-secret-flow

Execute the following to test the Get Secret component

curl -d '{"path":"secret/samples/sample1"}' -X POST http://localhost:8081/getSecret

### Executing write-secret-flow

Execute the following to test the Write Secret component

curl -d '{"path":"secret/samples/sample2","secret":"{\"attr1\":\"data2\"}"}' -X POST http://localhost:8081/writeSecret
curl -d '{"path":"secret/samples/sample2"}' -X POST http://localhost:8081/getSecret

### Executing encrypt-data-flow

Execute the following to test the Encrypt data component

curl -d '{"plaintext":"This is my sample plaintext"}' -X POST http://localhost:8081/encrypt

### Executing decrypt-data-flow

Execute the following to test the Decrypt data component, replacing CIPHERTEXT with the output of the command above

curl -d '{"ciphertext":"CIPHERTEXT"}' -X POST http://localhost:8081/decrypt

### Executing reencrypt-data-flow

Execute the following to test the Reencrypt data component, replacing CIPHERTEXT with the output of the command above

curl -d '{"ciphertext":"CIPHERTEXT"}' -X POST http://localhost:8081/reencrypt

## Step 3: Stopping Vault

Execute killVault.sh to kill all running Vault servers