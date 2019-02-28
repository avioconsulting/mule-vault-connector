package com.avioconsulting.mule.connector.vault;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.junit.Test;

public class VaultOperationsTestCase extends MuleArtifactFunctionalTestCase {

  /**
   * Specifies the mule config xml with the flows that are going to be executed in the tests, this file lives in the test resources.
   */
  @Override
  protected String getConfigFile() {
    return "test-mule-config.xml";
  }

  @Test
  public void executeSayHiOperation() throws Exception {
    String payloadValue = ((String) flowRunner("sayHiFlow").run()
                                      .getMessage()
                                      .getPayload()
                                      .getValue());
    assertThat(payloadValue, is("Hello Mariano Gonzalez!!!"));
  }

  @Test
  public void executeGetSecretOperation() throws Exception {
    String payloadValue = ((String) flowRunner("getSecretFlow").run().getMessage().getPayload().getValue());
    assertThat(payloadValue,containsString("token_uri") );
  }

//  @Test
//  public void executeRetrieveInfoOperation() throws Exception {
//    String payloadValue = ((String) flowRunner("retrieveInfoFlow")
//                                      .run()
//                                      .getMessage()
//                                      .getPayload()
//                                      .getValue());
//    assertThat(payloadValue, is("Using Configuration [configId] with Connection id [aValue:100]"));
//  }
}
