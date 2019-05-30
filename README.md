# Vault Connector

### Global Element Configurations

##### Basic Connection
Use this connection type to use Vault's Token Authentication backend.

Attributes:

*	vaultUrl - Vault Base URL (i.e. https://localhost:8200)
*	vaultToken - Token to use to authenticate to Vault
*	engineVersion - (Optional) the version of the secrets engine to use
*	pemFile - (Optional) An X.509 certificate, to use when communicating with Vault over HTTPS
*	trustStoreFile - (Optional) JKS Trust Store containing Vault Server certificate

```xml
<vault:config name="config" configId="configId">
	<vault:basic-connection vaultUrl="${vaultUrl}" vaultToken="${vaultToken}" engineVersion="v1">
		<vault:ssl-properties pemFile="${pemFile}"/>
	</vault:basic-connection>
</vault:config>
```

##### TLS Connection
Use this connection type to authenticate using Vault's TLS Certificate Authentication backend.

###### Using Java Key Store
Attributes:

*	vaultUrl - Vault Base URL (i.e. https://localhost:8200)
*	keyStoreFile - Java keystore, containing a client certificate that's registered with Vault's TLS Certificate auth backend
*	keyStorePassword - Password for the Key Store
*	trustStoreFile - A Java keystore, containing the X509 certificate used by Vault

```xml
<vault:config name="jksConfig" configId="jksConfigId" >
    <vault:tls-connection vaultUrl="${vaultUrl}">
		<vault:ssl-properties trustStoreFile="${trustStoreFile}"/>
        <vault:jks-properties keyStoreFile="${keyStoreFile}" keyStorePassword="${keyStorePassword}"/>
    </vault:tls-connection>
</vault:config>
```

###### Using PEM Files
Attributes:

*	vaultUrl - Vault Base URL (i.e. https://localhost:8200)
*	pemFile - An X.509 certificate, to use when communicating with Vault over HTTPS
*	clientPemFile - An X.509 client certificate, for use with Vault's TLS Certificate auth backend
*	clientKeyPemFile - An RSA private key, for use with Vault's TLS Certificate auth backend

```xml
<vault:config name="pemConfig" configId="pemConfigId" >
    <vault:tls-connection vaultUrl="${vaultUrl}" >
		<vault:ssl-properties pemFile="${pemFile}" />
        <vault:pem-properties clientPemFile="${clientPemFile}" clientKeyPemFile="${clientKeyPemFile}" />
    </vault:tls-connection>
</vault:config>
```

##### EC2 Connection
Use this connection type to authenticate using Vault's AWS Authentication Backend with instance identity documents.

###### Using Instance Metadata Service
Attributes:

*	vaultUrl - Vault Base URL (i.e. https://localhost:8200)
*	awsAuthMount - Vault mount point for AWS Authentication backend
*	vaultRole - Name of the role against which the login is being attempted. If role is not specified, then the login endpoint looks for a role bearing the name of the AMI ID of the EC2 instance that is trying to login if using the ec2 auth method, or the "friendly name" (i.e., role name or username) of the IAM principal authenticated. If a matching role is not found, login fails.
*	useInstanceMetadata - true to use the AWS metadata service to retrieve details

```xml
<vault:config name="config" configId="configId" >
    <vault:ec2-connection vaultUrl="${vaultUrl}" awsAuthMount="aws" vaultRole="ec2" useInstanceMetadata="true" />
</vault:config>
```

###### Using Instance Identity Document
Attributes:

*	vaultUrl - Vault Base URL (i.e. https://localhost:8200)
*	awsAuthMount - Vault mount point for AWS Authentication backend
*	vaultRole - Name of the role against which the login is being attempted. If role is not specified, then the login endpoint looks for a role bearing the name of the AMI ID of the EC2 instance that is trying to login if using the ec2 auth method, or the "friendly name" (i.e., role name or username) of the IAM principal authenticated. If a matching role is not found, login fails.
*	useInstanceMetadata - false to use the Instance Identity Document
*	identity - Base64 encoded EC2 instance identity document
*	signature - Base64 encoded SHA256 RSA signature of the instance identity document

```xml
<vault:config name="config" configId="configId" >
    <vault:ec2-connection vaultUrl="${vaultUrl}" awsAuthMount="aws" vaultRole="ec2" useInstanceMetadata="false" identity="${identityDoc}" signature="${identitySig}"/>
</vault:config>
```

##### IAM Connection
Use this connection type to authenticate using Vault's AWS Authentication backend with AWS' IAM authentication

Attributes:

*	vaultUrl - Vault Base URL (i.e. https://localhost:8200)
*	awsAuthMount - Vault mount point for AWS Authentication backend
*	vaultRole - Name of the role against which the login is being attempted. If role is not specified, then the login endpoint looks for a role bearing the name of the AMI ID of the EC2 instance that is trying to login if using the ec2 auth method, or the "friendly name" (i.e., role name or username) of the IAM principal authenticated. If a matching role is not found, login fails
*	iamRequestUrl - HTTP URL used in the signed request. Most likely just https://sts.amazonaws.com/ as most requests will probably use POST with an empty URI
*	iamRequestBody - Body of the signed request. Most likely Action=GetCallerIdentity&Version=2011-06-15
*	iamRequestHeaders - Request headers

```xml
<vault:config name="config" configId="configId" >
    <vault:iam-connection vaultUrl="${vaultUrl}" awsAuthMount="aws" vaultRole="ec2" iamRequestUrl="${iamReqUrl}" iamRequestBody="${iamReqBody}" iamRequestHeaders="${iamReqHeaders}"/>
</vault:config>
```

## Operations

### Getting Secrets
Drag a "Get secret" component from the palette into your flow. Secrets will be retrieved in JSON format.

Attributes:

*	config-ref - the global element configuration to use to connect to Vault
*	path - the path to the secret that should be retrieved
*	target - the target variable for the secret

```xml
<vault:get-secret doc:name="Get secret" config-ref="Vault_Config" path="secret/test/mysecret" target="secret"/>
```

### Writing Secrets
Drag a "Write secret" component from the palette into your flow. Secrets must be JSON formatted.

Attributes:

*	config-ref - the global element configuration to use to connect to Vault
*	path - the path of the secret to write
*	secret - JSON formatted data for the secret

```xml
<vault:write-secret doc:name="Write secret" config-ref="Vault_Config" path="secret/test/mysecret" secret="#[vars.secret]"/>
```

### Encrypting Data
Drag an "Encrypt Data" component from the palette into your flow. The secret engine being used in the connection must be version v1 to use the encryption features. The response will be the encrypted value of the data.

Attributes:

*	config-ref - the global element configuration to use to connect to Vault
*	transitMountpoint - Vault mount point for the transit secret engine
*	keyName - the name of the key to be used for encryption
*	plaintext - the data to be encrypted. The default maximum is 32MB

```xml
<vault:encrypt-data doc:name="Encrypt data" config-ref="Vault_Config" transitMountpoint="transit" keyName="mykey" plaintext="#[vars.myvar]"/>
```

### Decrypting Data
Drag an "Decrypt Data" component from the palette into your flow. The secret engine being used in the connection must be version v1 to use the encryption features. The response will be the decrypted value of the data.

Attributes:

*	config-ref - the global element configuration to use to connect to Vault
*	transitMountpoint - Vault mount point for the transit secret engine
*	keyName - the name of the key to be used for encryption
*	ciphertext - the data to be decrypted. The default maximum is 32MB

```xml
<vault:decrypt-data doc:name="Decrypt data" config-ref="Vault_Config" transitMountpoint="transit" keyName="mykey" ciphertext="vault:v2:9elCvYJCKvqK33KWgB/VwImq5EE7Of2fYEnjfg8xC+BDyIv4DV1j"/>
```

### Re-encrypting Data
Drag an "Reencrypt Data" component from the palette into your flow. The secret engine being used in the connection must be version v1 to use the encryption features. The response will be the encrypted value of the data, encrypted under the new key.

Attributes:

*	config-ref - the global element configuration to use to connect to Vault
*	transitMountpoint - Vault mount point for the transit secret engine
*	keyName - the name of the key to be used for encryption
*	ciphertext - the encrypted data to be reencrypted. The default maximum is 32MB

```xml
<vault:reencrypt-data doc:name="Reencrypt data" config-ref="Vault_Config" transitMountpoint="transit" keyName="mykey" ciphertext="vault:v2:9elCvYJCKvqK33KWgB/VwImq5EE7Of2fYEnjfg8xC+BDyIv4DV1j"/>
```



## Publishing to a Private Exchange

To publish to a private exchange, some updates are necessary in the `pom.xml` file and your Maven `settings.xml`.

Update the `groupId` to the organization ID used by your organization on the Anypoint platform.

In addition, update the `url` in the `distributionManagement` section of the pom to the following, replacing `${orgId}` with your Organization ID:
```
https://maven.anypoint.mulesoft.com/api/v1/organizations/${orgID}/maven
```

Add a `server` for the exchange repository in your Maven `settings.xml` file with the username and password to use for AnyPoint Exchange. 

After it is published in the exchange, the dependency in a project would change to look like this:

```xml
<dependency>
    <groupId>${orgId}</groupId>
    <artifactId>vault-connector</artifactId>
    <version>0.2.0</version>
    <classifier>mule-plugin</classifier>
</dependency>
```
