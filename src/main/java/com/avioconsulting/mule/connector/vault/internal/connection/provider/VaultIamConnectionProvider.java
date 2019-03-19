package com.avioconsulting.mule.connector.vault.internal.connection.provider;

import com.avioconsulting.mule.connector.vault.internal.connection.VaultConnection;
import com.avioconsulting.mule.connector.vault.internal.connection.impl.IamVaultConnection;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

@DisplayName("IAM Connection")
@Alias("iam-connection")
public class VaultIamConnectionProvider implements PoolingConnectionProvider<VaultConnection> {

    @DisplayName("Vault URL")
    @Parameter
    private String vaultUrl;

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
    private String role;

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

    @Override
    public VaultConnection connect() throws ConnectionException {
        return new IamVaultConnection(vaultUrl + ":" + role + ":" + awsAuthMount, vaultUrl, awsAuthMount, role, iamRequestUrl, iamRequestBody, iamRequestHeaders);
    }

    @Override
    public void disconnect(VaultConnection vaultConnection) {

    }

    @Override
    public ConnectionValidationResult validate(VaultConnection vaultConnection) {
        return null;
    }
}
