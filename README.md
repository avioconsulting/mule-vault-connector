# Vault Connector

### Global Element Configurations

##### Basic Connection
Use this connection type to use Vault's Token Authentication backend.

Attributes:

* `vaultUrl` - Vault Base URL (i.e. https://localhost:8200)
* `vaultToken` - Token to use to authenticate to Vault
* `engineVersion` - (Optional) the version of the secrets engine to use. If not specified, V1 will be used.
* `tls:context` - (Optional) TLS trust/key store configuration. See the [official documentation](https://docs.mulesoft.com/mule-runtime/4.3/tls-configuration).

```xml
<vault:config name="config" engineVersion="V2">
  <vault:basic-connection vaultUrl="http://localhost:8200" vaultToken="${token}">
    <tls:context>
      <tls:trust-store type="jks" path="truststore.jks" password="${password}" />
    </tls:context>
  </vault:basic-connection>
</vault:config>
```

##### TLS Connection
Use this connection type to authenticate using Vault's TLS Certificate Authentication backend.

Attributes:

* `vaultUrl` - Vault Base URL (i.e. https://localhost:8200)
* `mount` - (Optional) path the TLS Certificate Authentication backend is mounted on
* `certificateRole` - (Optional) Name of certificate role to authenticate against. If not set, all will be tried.
* `tls:context` - TLS trust/key store configuration. See the [official documentation](https://docs.mulesoft.com/mule-runtime/4.3/tls-configuration).

```xml
<vault:config name="jksConfig">
  <vault:tls-connection vaultUrl="${vaultUrl}" mount="cert" certificateRole="myrole">
    <tls:context>
      <tls:trust-store type="jks" path="truststore.jks" password="${truststorePassword}" />
      <tls:key-store type="jks" path="client.jks" keyPassword="${keystorePassword}" password="${certPassword}" />
    </tls:context>
  </vault:tls-connection>
</vault:config>
```
##### EC2 Connection
Use this connection type to authenticate using Vault's AWS Authentication Backend with instance identity documents.

###### Using Instance Metadata Service
Attributes:

* `vaultUrl` - Vault Base URL (i.e. https://localhost:8200)
* `awsAuthMount` - (Optional) Vault mount point for AWS Authentication backend. If not set, `aws` will be used.
* `vaultRole` - (Optional) Name of the role against which the login is being attempted. If role is not specified, then the login endpoint looks for a role bearing the name of the AMI ID of the EC2 instance that is trying to login if using the ec2 auth method, or the "friendly name" (i.e., role name or username) of the IAM principal authenticated. If a matching role is not found, login fails.
* `nonce` - (Optional) The nonce to be used for subsequent login requests. Subsequent login attempts will not be successful if not provided. 
* `useInstanceMetadata` - true to use the AWS metadata service to retrieve details

```xml
<vault:config name="config-metadata" >
  <vault:ec2-connection vaultUrl="${vaultUrl}" awsAuthMount="aws" vaultRole="ec2" nonce="${nonce}" useInstanceMetadata="true"/>
</vault:config>
```

###### Using Instance Identity Document
Attributes:

* `vaultUrl` - Vault Base URL (i.e. https://localhost:8200)
* `awsAuthMount` - Vault mount point for AWS Authentication backend
* `vaultRole` - Name of the role against which the login is being attempted. If role is not specified, then the login endpoint looks for a role bearing the name of the AMI ID of the EC2 instance that is trying to login if using the ec2 auth method, or the "friendly name" (i.e., role name or username) of the IAM principal authenticated. If a matching role is not found, login fails.
* `useInstanceMetadata` - false to use the Instance Identity Document
* `identity` - Base64 encoded EC2 instance identity document
* `signature` - Base64 encoded SHA256 RSA signature of the instance identity document
* `nonce` - (Optional) The nonce to be used for subsequent login requests. Subsequent login attempts will not be successful if not provided.

```xml
<vault:config name="config" >
  <vault:ec2-connection vaultUrl="${vaultUrl}" awsAuthMount="aws" vaultRole="ec2" useInstanceMetadata="false" identity="test" signature="test" nonce="${nonce}"/>
</vault:config>
```

##### IAM Connection
Use this connection type to authenticate using Vault's AWS Authentication backend with AWS' IAM authentication

Attributes:

*	`vaultUrl` - Vault Base URL (i.e. https://localhost:8200)
*	`awsAuthMount` - (Optional) Vault mount point for AWS Authentication backend. If not specified, `aws` will be used.
*	`vaultRole` - Name of the role against which the login is being attempted. If role is not specified, then the login endpoint looks for a role bearing the name of the AMI ID of the EC2 instance that is trying to login if using the ec2 auth method, or the "friendly name" (i.e., role name or username) of the IAM principal authenticated. If a matching role is not found, login fails
*	`iamRequestUrl` - Base64 encoded HTTP URL used in the signed request. Most likely `aHR0cHM6Ly9zdHMuYW1hem9uYXdzLmNvbS8=`, which is just the Base64 encoded value of `https://sts.amazonaws.com/` as most requests will probably use POST with an empty URI
*	`iamRequestBody` - Base64 encoded body of the signed request. Most likely `QWN0aW9uPUdldENhbGxlcklkZW50aXR5JlZlcnNpb249MjAxMS0wNi0xNQ==`, which is the Base64 encoded value of `Action=GetCallerIdentity&Version=2011-06-15`
*	`iamRequestHeaders` - Request headers

```xml
<vault:config name="config" >
  <vault:iam-connection vaultUrl="${vaultUrl}"
                        awsAuthMount="aws"
                        vaultRole="ec2"
                        iamRequestUrl="aHR0cHM6Ly9zdHMuYW1hem9uYXdzLmNvbS8="
                        iamRequestBody="QWN0aW9uPUdldENhbGxlcklkZW50aXR5JlZlcnNpb249MjAxMS0wNi0xNQ=="
                        iamRequestHeaders="X-Vault-AWS-IAM-Server-ID=dev.vault.test.com"/>
</vault:config>
```

#### Additional Connection Options

##### Engine Version
HashiCorp introduced a second version of the KV secrets engine, which changes the request path for secrets in the API. By default, the V1 version will be used, but if V2 is selected, secret paths will be rewritten for the KV-V2 secrets engine.

##### TLS Context
A TLS Context may be used with any of the connection types. In addition, a global TLS Context may be referenced from any of the connection types.

Sample Global Reference:
```xml
<tls:context name="TLS_Context">
  <tls:trust-store path="truststore.jks" password="${password}" type="jks" />
</tls:context>

<vault:config name="Vault_Config">
  <vault:basic-connection vaultUrl="https://localhost:8200" vaultToken="${token}" tlsContext="TLS_Context" />
</vault:config>
```

##### Proxy Configuration
A proxy may be used with any of the connection types as well.

Examples:
```xml
<!-- Global Reference -->
<vault:proxy name="Proxy" host="myproxy.com" port="8888" />
<vault:config name="Vault_Global_Proxy_Config">
  <vault:basic-connection vaultUrl="http://localhost:8200" vaultToken="${token}" proxyConfig="Proxy"/>
</vault:config>
 
<!-- Connection-specific proxy -->
<vault:config name="Vault_NTLM_Proxy_Config">
  <vault:basic-connection vaultUrl="https://localhost:8200" vaultToken="${token}">
    <vault:proxy-config>
      <vault:ntlm-proxy ntlmDomain="test" host="proxyhost.com" port="7890" />
    </vault:proxy-config>
  </vault:basic-connection>
</vault:config>
```

##### Vault Namespace
Vault namespaces are a Vault Enterprise feature and may be used with this connector as well. To do so, add the `vaultNamespace` attribute to the vault:config element to include it in requests sent to Vault.

##### Vault Request Header
By default, the `X-Vault-Request` header will be sent with every request to Vault. This can be disabled by setting the `includeVaultRequestHeader` attribute to `false` on the vault:config element or in an operation.

## Operations

### Getting Secrets
Drag a "Get secret" component from the palette into your flow. Secrets will be retrieved in JSON format.

Attributes:

*	`config-ref` - the global element configuration to use to connect to Vault
*	`path` - the path to the secret that should be retrieved
*	`target` - the target variable for the secret

```xml
<vault:get-secret doc:name="Get secret" config-ref="Vault_Config" path="secret/test/mysecret" target="secret"/>
```

### Writing Secrets
Drag a "Write secret" component from the palette into your flow. Secrets must be JSON formatted.

Attributes:

*	`config-ref` - the global element configuration to use to connect to Vault
*	`path` - the path of the secret to write
*	`secret` - JSON formatted data for the secret

```xml
<vault:write-secret doc:name="Write secret" config-ref="Vault_Config" path="secret/test/mysecret" secret="#[vars.secret]"/>
```

### Encrypting Data
Drag an "Encrypt Data" component from the palette into your flow. The secret engine being used in the connection must be version v1 to use the encryption features. The response will be the encrypted value of the data.

Attributes:

*	`config-ref` - the global element configuration to use to connect to Vault
*	`transitMountpoint` - Vault mount point for the transit secret engine
*	`keyName` - the name of the key to be used for encryption
*	`plaintext` - Base64 encoded data to be encrypted. The default maximum is 32MB

```xml
<vault:encrypt-data doc:name="Encrypt data" config-ref="Vault_Config" transitMountpoint="transit" keyName="mykey" plaintext="#[vars.myvar]"/>
```

### Decrypting Data
Drag an "Decrypt Data" component from the palette into your flow. The secret engine being used in the connection must be version v1 to use the encryption features. The response will be the Base64 encoded value of the decrypted data.

Attributes:

*	`config-ref` - the global element configuration to use to connect to Vault
*	`transitMountpoint` - Vault mount point for the transit secret engine
*	`keyName` - the name of the key to be used for encryption
*	`ciphertext` - the data to be decrypted. The default maximum is 32MB

```xml
<vault:decrypt-data doc:name="Decrypt data" config-ref="Vault_Config" transitMountpoint="transit" keyName="mykey" ciphertext="vault:v2:9elCvYJCKvqK33KWgB/VwImq5EE7Of2fYEnjfg8xC+BDyIv4DV1j"/>
```

### Re-encrypting Data
Drag an "Reencrypt Data" component from the palette into your flow. The secret engine being used in the connection must be version v1 to use the encryption features. The response will be the encrypted value of the data, encrypted under the new key.

Attributes:

*	`config-ref` - the global element configuration to use to connect to Vault
*	`transitMountpoint` - Vault mount point for the transit secret engine
*	`keyName` - the name of the key to be used for encryption
*	`ciphertext` - the encrypted data to be reencrypted. The default maximum is 32MB

```xml
<vault:reencrypt-data doc:name="Reencrypt data" config-ref="Vault_Config" transitMountpoint="transit" keyName="mykey" ciphertext="vault:v2:9elCvYJCKvqK33KWgB/VwImq5EE7Of2fYEnjfg8xC+BDyIv4DV1j"/>
```

## Deploying to Exchange
The Mule 4 Vault Connector can be deployed to an Exchange with a few small modifications.
> Shamelessly stolen from Manik Mager's [blog post](https://javastreets.com/blog/publish-connectors-to-anypoint-exchange.html)
1. Update the connector pom.xml file
    * Change the `groupId` value to the Organization Id 
        * (Id found in Anypoint -> Access Management -> Organization -> You're Org)
        ```
           <modelVersion>4.0.0</modelVersion>
           <groupId>xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx</groupId>
           <artifactId>vault-connector</artifactId>
           <version>1.0.0-SNAPSHOT</version>
           <packaging>mule-extension</packaging>
           <name>Vault Connector - Mule 4</name>
        ```
    * Update `distributionManagement` to point to the Exchange Repository (Uncomment these lines)
        ```
        <distributionManagement>
          <snapshotRepository>
            <id>exchange-repository</id>
            <name>Exchange Repository</name>
            <url>https://maven.anypoint.mulesoft.com/api/v1/organizations/${pom.groupId}/maven</url>
            <layout>default</layout>
          </snapshotRepository>
          <repository>
            <id>exchange-repository</id>
            <name>Exchange Repository</name>
            <url>https://maven.anypoint.mulesoft.com/api/v1/organizations/${pom.groupId}/maven</url>
            <layout>default</layout>
          </repository>
        </distributionManagement>
        ```
1. Configure your `~/.m2/settings.xml` file with your Exchange credentials
    ```
    <servers>
     <server>
       <id>exchange-repository</id>
       <username>USERNAME</username>
       <password>PASSWORD</password>
     </server>
    </servers>
    ```
1. Execute `mvn deploy` to publish to Exchange