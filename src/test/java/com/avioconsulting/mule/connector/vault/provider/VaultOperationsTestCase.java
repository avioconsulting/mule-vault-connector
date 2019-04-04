package com.avioconsulting.mule.connector.vault.provider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

import com.avioconsulting.mule.connector.vault.util.VaultContainer;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.junit.Test;

import java.io.IOException;


public class VaultOperationsTestCase extends MuleArtifactFunctionalTestCase {

  @ClassRule
  public static final VaultContainer container = new VaultContainer();

  @BeforeClass
  public static void setupContainer() throws IOException, InterruptedException {
    container.initAndUnsealVault();
    container.enableKvSecretsV2();
    container.setupSampleSecret();
    System.setProperty("vaultUrl", container.getAddress());
    System.setProperty("vaultToken", container.getRootToken());
    System.setProperty("pemFile", VaultContainer.CERT_PEMFILE);
  }

  /**
   * Specifies the mule config xml with the flows that are going to be executed in the tests, this file lives in the test resources.
   */
  @Override
  protected String getConfigFile() {
    return "test-mule-config.xml";
  }

  @Test
  public void executeGetSecretOperation() throws Exception {
    String payloadValue = ((String) flowRunner("getSecretFlow").run()
            .getMessage()
            .getPayload()
            .getValue());
    assertThat(payloadValue,containsString("test_value1") );
  }

  @Test
  public void executeWriteSecretOperation() throws Exception {
    String payloadValue = ((String) flowRunner("writeSecretFlow").run()
            .getMessage()
            .getPayload()
            .getValue());
    assertThat(payloadValue,containsString("name"));
  }


}
