package com.avioconsulting.mule.connector.vault.provider;

import com.avioconsulting.mule.connector.vault.util.AwsCheck;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assume.assumeTrue;

public class VaultEc2IDDocAuthTestCase extends MuleArtifactFunctionalTestCase {

    @BeforeClass
    public static void runCheckBeforeTest() {
        assumeTrue(AwsCheck.isExecutingOnAws());
    }

    @Override
    protected String getConfigFile() {
        return "mule_config/test-mule-ec2-identitydoc-auth-config.xml";
    }

    @Test
    public void testVaultEc2Authentication() throws Exception {
        assumeTrue(AwsCheck.isExecutingOnAws());
        String payloadValue = ((String) flowRunner("getSecretFlow")
                .run()
                .getMessage()
                .getPayload()
                .getValue());

        assertThat(payloadValue,containsString("test_value1") );
    }

}