package com.avioconsulting.mule.connector.vault.provider;

import com.avioconsulting.mule.connector.vault.util.SSLUtils;
import com.avioconsulting.mule.connector.vault.util.VaultContainer;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

public class VaultSSLConnectionTestCase extends MuleArtifactFunctionalTestCase {

    @ClassRule
    public static final VaultContainer container = new VaultContainer();

    @BeforeClass
    public static void setupContainer() throws IOException, InterruptedException, KeyStoreException, CertificateException,
            NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException,
            OperatorCreationException {
        container.initAndUnsealVault();
        container.enableKvSecretsV2();
        container.setupSampleSecret();
        SSLUtils.createClientCertAndKey();
        container.setupBackendCert();
        System.setProperty("vaultUrl", container.getAddress());
        System.setProperty("vaultToken", container.getRootToken());
        System.setProperty("pemFile", VaultContainer.CERT_PEMFILE);
        System.setProperty("clientPemFile", VaultContainer.CLIENT_CERT_PEMFILE);
        System.setProperty("clientKeyPemFile", VaultContainer.CLIENT_PRIVATE_KEY_PEMFILE);
        System.setProperty("keyStoreFile", VaultContainer.CLIENT_KEYSTORE);
        System.setProperty("keyStorePassword", VaultContainer.CLIENT_KEYSTORE_PASSWORD);
        System.setProperty("trustStoreFile", VaultContainer.CLIENT_TRUSTSTORE);
    }

    /**
     * Specifies the mule config xml with the flows that are going to be executed in the tests, this file lives in the test resources.
     */
    @Override
    protected String getConfigFile() {
        return "mule_config/test-ssl-mule-config.xml";
    }

    @Test
    public void executeGetSecretWithTrustStore() throws Exception {
        String payloadValue = ((String) flowRunner("getSecretFlowTrustStore").run()
                .getMessage()
                .getPayload()
                .getValue());
        assertThat(payloadValue,containsString("test_value1") );
    }

    @Test
    public void executeGetSecretWithPemFile() throws Exception {
        String payloadValue = ((String) flowRunner("getSecretFlowPemFile").run()
                .getMessage()
                .getPayload()
                .getValue());
        assertThat(payloadValue,containsString("test_value1") );
    }

    @Test
    public void executeGetSecretWithJKSConfig() throws Exception {
        String payloadValue = ((String) flowRunner("getSecretFlowJksConfig").run()
                .getMessage()
                .getPayload()
                .getValue());
        assertThat(payloadValue,containsString("test_value1") );
    }

    @Test
    public void executeGetSecretWitPEMConfig() throws Exception {
        String payloadValue = ((String) flowRunner("getSecretFlowPemConfig").run()
                .getMessage()
                .getPayload()
                .getValue());
        assertThat(payloadValue,containsString("test_value1"));
    }


}
