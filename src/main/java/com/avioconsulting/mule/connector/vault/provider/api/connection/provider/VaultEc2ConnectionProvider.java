package com.avioconsulting.mule.connector.vault.provider.api.connection.provider;

import com.avioconsulting.mule.connector.vault.provider.api.connection.VaultConnection;
import com.avioconsulting.mule.connector.vault.provider.api.connection.impl.Ec2VaultConnection;
import com.avioconsulting.mule.connector.vault.provider.api.parameter.EngineVersion;
import com.avioconsulting.mule.connector.vault.provider.api.parameter.SSLProperties;
import com.bettercloud.vault.rest.Rest;
import com.bettercloud.vault.rest.RestException;
import com.bettercloud.vault.rest.RestResponse;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * This class provides {@link Ec2VaultConnection} instances and the functionality to disconnect and validate those
 * connections. This is a {@link PoolingConnectionProvider} which will pool and reuse connections.
 */
@DisplayName("EC2 Connection")
@Alias("ec2-connection")
public class VaultEc2ConnectionProvider implements PoolingConnectionProvider<VaultConnection> {

    // This is the URI to use to retrieve the PKCS7 Signature
    // See: https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-identity-documents.html
    private final static String INSTANCE_PKCS7_URI = "http://169.254.169.254/latest/dynamic/instance-identity/pkcs7";
    private final Logger LOGGER = LoggerFactory.getLogger(VaultEc2ConnectionProvider.class);

    @DisplayName("Vault URL")
    @Parameter
    private String vaultUrl;

    @DisplayName("Secrets Engine Version")
    @Parameter
    @Optional
    private EngineVersion engineVersion;

    @DisplayName("Vault AWS Authentication Mount")
    @Summary("Mount point for AWS Authentication in Vault")
    @Parameter
    private String awsAuthMount;

    @DisplayName("Vault Role")
    @Parameter
    private String vaultRole;

    @DisplayName("PKCS7 Signature")
    @Summary("PKCS7 signature of the identity document with all \\n characters removed.")
    @Optional
    @Parameter
    private String pkcs7;

    @DisplayName("Identity Document")
    @Summary("Base64 encoded EC2 instance identity document.")
    @Optional
    @Parameter
    private String identity;

    @DisplayName("Identity Document Signature")
    @Summary("Base64 encoded SHA256 RSA signature of the instance identity document")
    @Optional
    @Parameter
    private String signature;

    @DisplayName("Use Instance Metadata")
    @Summary("Retrieve Instance metadata")
    @Parameter
    private boolean useInstanceMetadata = false;

    @DisplayName("SSL Properties")
    @Parameter
    @Optional
    @Placement(tab = Placement.CONNECTION_TAB)
    private SSLProperties sslProperties;

    /**
     * Constructs an {@link Ec2VaultConnection}. When useInstanceMetadata is true, the PKCS7 value is looked up from
     * the AWS Metadata Service
     *
     * @return an {@link Ec2VaultConnection}
     * @throws ConnectionException
     */
    @Override
    public VaultConnection connect() throws ConnectionException {
        if (useInstanceMetadata) {
            pkcs7 = lookupPKCS7();
        }
        boolean pkcsUnavailable = pkcs7 == null || pkcs7.isEmpty();
        boolean identityUnavailable = identity == null || identity.isEmpty() || signature == null || signature.isEmpty();
        if (pkcsUnavailable && identityUnavailable) {
            LOGGER.error("PKCS7 Signature, Identity Document, and Identity Signature are all null or empty");
            throw new ConnectionException("PKCS7 Signature or the Identity Document and Signature are required");
        }
        StringBuilder idBuilder = new StringBuilder(vaultUrl + ":" + vaultRole);
        if (!pkcsUnavailable) {
            idBuilder.append(":" + pkcs7);
        } else {
            idBuilder.append(":" + identity);
        }
        return new Ec2VaultConnection(idBuilder.toString(),vaultUrl,vaultRole,pkcs7,null,identity,
                signature,awsAuthMount, sslProperties, engineVersion);
    }

    @Override
    public void disconnect(VaultConnection connection) {
        connection.invalidate();
    }

    @Override
    public ConnectionValidationResult validate(VaultConnection connection) {
        if (connection.isValid()) {
            return ConnectionValidationResult.success();
        } else {
            return ConnectionValidationResult.failure("Connection Invalid", null);
        }
    }

    /**
     * EC2 Provides a service to retrieve the instance identity. This method uses that service to look up the PKCS7.
     *
     * @return the PKCS7 value with the '\n' characters removed
     */
    private String lookupPKCS7() {
        String pkcs7 = null;
        try {
            final RestResponse response = new Rest().url(INSTANCE_PKCS7_URI).get();
            String responseStr = new String(response.getBody(), StandardCharsets.UTF_8);
            // remove \n characters
            pkcs7 = responseStr.replaceAll("\n", "");
        } catch (RestException re) {
            LOGGER.error("Error looking up PKCS7 from Metadata Service",re);
        }
        return pkcs7;
    }
}
