package com.avioconsulting.mule.connector.vault.provider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringStartsWith.startsWith;

import com.avioconsulting.mule.connector.vault.util.VaultContainer;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.junit.Test;
import org.junit.Assert;

import java.io.IOException;

public class VaultOperationsTestCase extends MuleArtifactFunctionalTestCase {

  @ClassRule
  public static final VaultContainer container = new VaultContainer();

  @BeforeClass
  public static void setupContainer() throws IOException, InterruptedException {
    container.initAndUnsealVault();
    container.enableKvSecretsV2();
    container.setupSampleSecret();
    container.setupTransitEngine();
    System.setProperty("vaultUrl", container.getAddress());
    System.setProperty("vaultToken", container.getRootToken());
    System.setProperty("pemFile", VaultContainer.CERT_PEMFILE);
    System.setProperty("cipherText", container.getCipherText());
  }

  /**
   * Specifies the mule config xml with the flows that are going to be executed in the tests, this file lives in the test resources.
   */
  @Override
  protected String getConfigFile() {
    return "mule_config/test-mule-config.xml";
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

  @Test
  public void executeEncryptDataOperation() throws Exception {
    String payloadValue = ((String) flowRunner("encryptDataFlow").run()
            .getMessage()
            .getPayload()
            .getValue());
    assertThat(payloadValue,startsWith("vault"));
  }

  @Test
  public void executeDecryptDataOperation() throws Exception {
    String payloadValue = ((String) flowRunner("decryptDataFlow").run()
            .getMessage()
            .getPayload()
            .getValue());
    assertThat(payloadValue,containsString("plaintext"));
  }

  @Test
  public void executeReencryptDataOperation() throws Exception {
    String payloadValue = ((String) flowRunner("reEncryptFlow").run()
            .getMessage()
            .getPayload()
            .getValue());
    assertThat(payloadValue,startsWith("vault"));
  }

  @Test
  public void executeGetSecretDoesNotExist() throws Exception {
    try {
      String payloadValue = ((String) flowRunner("missingFlow").run()
              .getMessage()
              .getPayload()
              .getValue());
      Assert.fail("The payload is not present, so there should have been an exception.");
    } catch (Exception ignored) { }

  }

}
