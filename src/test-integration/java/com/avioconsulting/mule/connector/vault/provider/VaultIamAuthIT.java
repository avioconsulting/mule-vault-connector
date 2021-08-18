package com.avioconsulting.mule.connector.vault.provider;

import com.avioconsulting.mule.connector.vault.util.AwsCheck;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assume.assumeTrue;

public class VaultIamAuthIT extends MuleArtifactFunctionalTestCase {

    @BeforeClass
    public static void runCheckBeforeTest() {
        assumeTrue(AwsCheck.isAWSCheckEnabled() && AwsCheck.isExecutingOnAws());
    }

    @Override
    protected String getConfigFile() {
        return "mule_config/test-mule-ec2-metadata-auth-config-it.xml";
    }

    @Test
    public void testVaultEc2Authentication() throws Exception {
        assumeTrue(AwsCheck.isAWSCheckEnabled() && AwsCheck.isExecutingOnAws());
        String payloadValue = ((String) flowRunner("getSecretFlow")
                .run()
                .getMessage()
                .getPayload()
                .getValue());

        assertThat(payloadValue,containsString("test_value1") );
    }

}