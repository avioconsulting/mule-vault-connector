package com.avioconsulting.mule.connector.vault.provider.api.connection.provider;

import com.avioconsulting.mule.connector.vault.provider.api.connection.VaultConnection;
import com.avioconsulting.mule.connector.vault.provider.api.connection.impl.IamVaultConnection;
import com.avioconsulting.mule.connector.vault.provider.api.parameter.EngineVersion;
import com.avioconsulting.mule.connector.vault.provider.api.parameter.SSLProperties;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

/**
 * This class provides {@link IamVaultConnection} instances and the functionality to disconnect and validate those
 * connections. This is a {@link PoolingConnectionProvider} which will pool and reuse connections.
 */
@DisplayName("IAM Connection")
@Alias("iam-connection")
public class VaultIamConnectionProvider implements PoolingConnectionProvider<VaultConnection> {

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
    @Summary("Name of the role against which the login is being attempted. If role is not specified, then the login " +
            "endpoint looks for a role bearing the name of the AMI ID of the EC2 instance that is trying to login if " +
            "using the ec2 auth method, or the \"friendly name\" (i.e., role name or username) of the IAM principal " +
            "authenticated. If a matching role is not found, login fails.")
    @Optional
    @Parameter
    private String vaultRole;

    @DisplayName("IAM Request URL")
    @Summary("Most likely https://sts.amazonaws.com/")
    @Parameter
    private String iamRequestUrl;

    @DisplayName("IAM Request Body")
    @Summary("Body of the signed request")
    @Parameter
    private String iamRequestBody;

    @DisplayName("IAM Request Headers")
    @Parameter
    private String iamRequestHeaders;

    @DisplayName("SSL Properties")
    @Parameter
    @Optional
    @Placement(tab = Placement.CONNECTION_TAB)
    private SSLProperties sslProperties;

    @Override
    public VaultConnection connect() throws ConnectionException {
        return new IamVaultConnection(vaultUrl + ":" + vaultRole + ":" + awsAuthMount, vaultUrl, awsAuthMount,
                vaultRole, iamRequestUrl, iamRequestBody, iamRequestHeaders, sslProperties, engineVersion);
    }

    @Override
    public void disconnect(VaultConnection vaultConnection) {

    }

    @Override
    public ConnectionValidationResult validate(VaultConnection vaultConnection) {
        return null;
    }
}
