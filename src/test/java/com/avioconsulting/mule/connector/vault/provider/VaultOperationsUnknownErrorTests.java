package com.avioconsulting.mule.connector.vault.provider;

import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

import static org.junit.Assert.fail;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class VaultOperationsUnknownErrorTests extends MuleArtifactFunctionalTestCase {

    private static String LOOKUP_RESPONSE = "{\"data\":{\"accessor\":\"8609694a-cdbc-db9b-d345-e782dbb562ed\",\"creation_time\":1523979354,\"creation_ttl\":2764800,\"display_name\":\"ldap2-tesla\",\"entity_id\":\"7d2e3179-f69b-450c-7179-ac8ee8bd8ca9\",\"expire_time\":\"2018-05-19T11:35:54.466476215-04:00\",\"explicit_max_ttl\":0,\"id\":\"cf64a70f-3a12-3f6c-791d-6cef6d390eed\",\"identity_policies\":[\"dev-group-policy\"],\"issue_time\":\"2018-04-17T11:35:54.466476078-04:00\",\"meta\":{\"username\":\"tesla\"},\"num_uses\":0,\"orphan\":true,\"path\":\"auth/ldap2/login/tesla\",\"policies\":[\"default\",\"testgroup2-policy\"],\"renewable\":false,\"ttl\":2764790}}";
    private static String MYSECRET_RESPONSE = "{\"request_id\": \"69411f4b-fb02-171a-f66c-485f106e7f5c\",\"lease_id\": \"\",\"renewable\": false,\"lease_duration\": 0,\"data\": {\"data\": {\"att1\": \"test_value1\",\"att2\": \"test_value2\"},\"metadata\": {\"created_time\": \"2019-04-24T23:03:18.63231658Z\",\"deletion_time\": \"\",\"destroyed\": false,\"version\": 1}},\"wrap_info\": null,\"warnings\": null,\"auth\": null}";
    private static String DYNAMIC_RESPONSE = "{\"data\": {\"username\": \"dynUser\", \"password\": \"dynpassword\"}}";
    private static String WRITE_RESPONSE = "{\"data\":{\"created_time\":\"2018-03-22T02:36:43.986212308Z\",\"deletion_time\":\"\",\"destroyed\":false,\"version\":1}}";
    private static String MULE_WRITE_RESPONSE = "{\"request_id\": \"69411f4b-fb02-171a-f66c-485f106e7f5c\",\"lease_id\": \"\",\"renewable\": false,\"lease_duration\": 0,\"data\": {\"data\": {\"name\": \"test\"},\"metadata\": {\"created_time\": \"2019-04-24T23:03:18.63231658Z\",\"deletion_time\": \"\",\"destroyed\": false,\"version\": 1}},\"wrap_info\": null,\"warnings\": null,\"auth\": null}";
    private static String ENCRYPT_RESPONSE = "{\"data\":{\"ciphertext\":\"vault:v1:XjsPWPjqPrBi1N2Ms2s1QM798YyFWnO4TR4lsFA=\"}}";
    private static String DECRYPT_RESPONSE = "{\"data\":{\"plaintext\":\"dGVzdAo=\"}}";
    private static String REENCRYPT_RESPONSE = "{\"data\":{\"ciphertext\":\"vault:v2:XjsPWPjqPrBi1N2Ms2s1QM798YyFWnO4TR4lsFA=\"}}";

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);
    private MockServerClient mockClient;

    protected String getConfigFile() {

        System.setProperty("vaultUrl", String.format("https://%s:%d", mockServerRule.getClient().remoteAddress().getHostString(), mockServerRule.getClient().remoteAddress().getPort()));

        mockClient
                .withSecure(true)
                .when(
                    request()
                        .withMethod("GET")
                        .withPath("/v1/auth/token/lookup-self")
                        .withHeader("X-Vault-Token", "MOCK_TOKEN")
                ).respond(
                    response()
                        .withStatusCode(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(LOOKUP_RESPONSE)
        );

        mockClient
                .withSecure(true)
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/v1/secret/data/test/mysecret")
                                .withHeader("X-Vault-Token", "MOCK_TOKEN")
                ).respond(
                response()
                        .withStatusCode(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody(MYSECRET_RESPONSE)

        );

        mockClient
            .withSecure(true)
            .when(
                request()
                    .withMethod("POST")
                    .withPath("/v1/secret/data/test/mule-write")
                    .withHeader("X-Vault-Token", "MOCK_TOKEN")
            ).respond(
                response()
                    .withStatusCode(500)
                    .withHeader("Content-Type", "application/json")
                    .withBody(WRITE_RESPONSE)
            );

        mockClient
                .withSecure(true)
                .when(
                    request()
                        .withMethod("POST")
                        .withPath("/v1/transit/encrypt/testKey")
                        .withHeader("X-Vault-Token", "MOCK_TOKEN")
                ).respond(
                    response()
                        .withStatusCode(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody(ENCRYPT_RESPONSE)
        );

        mockClient
                .withSecure(true)
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/v1/transit/decrypt/testKey")
                                .withHeader("X-Vault-Token", "MOCK_TOKEN")
                ).respond(
                response()
                        .withStatusCode(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody(DECRYPT_RESPONSE)
        );

        mockClient
                .withSecure(true)
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/v1/transit/rewrap/testKey")
                                .withHeader("X-Vault-Token", "MOCK_TOKEN")
                ).respond(
                response()
                        .withStatusCode(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody(REENCRYPT_RESPONSE)
        );

        return "mule_config/test-operations-mule-config.xml";
    }

    @Test
    public void testGetSecretUnknownError() throws Exception {
        try {
            String payloadValue = ((String) flowRunner("getSecretFlow")
                    .run()
                    .getMessage()
                    .getPayload()
                    .getValue());
            fail("Exception not thrown");
        } catch (Exception e) {

        }

    }

    @Test
    public void testWriteSecretUnknownError() throws Exception {
        try {
            String payloadValue = ((String) flowRunner("writeSecretFlow")
                    .run()
                    .getMessage()
                    .getPayload()
                    .getValue());
            fail("Exception not thrown");
        } catch (Exception e) {

        }
    }

    @Test
    public void testEncryptDataUnknownError() throws Exception {
        try {
            String payloadValue = ((String) flowRunner("encryptDataFlow")
                    .run()
                    .getMessage()
                    .getPayload()
                    .getValue());
            fail("Exception not thrown");
        } catch (Exception e) {

        }
    }

    @Test
    public void testDecryptDataUnknownError() throws Exception {
        try {
            String payloadValue = ((String) flowRunner("decryptDataFlow")
                    .run()
                    .getMessage()
                    .getPayload()
                    .getValue());
            fail("Exception not thrown");
        } catch (Exception e) {

        }

    }

    @Test
    public void testReencryptDataUnknownError() throws Exception {
        try {
            String payloadValue = ((String) flowRunner("reEncryptFlow")
                    .run()
                    .getMessage()
                    .getPayload()
                    .getValue());
            fail("Exception not thrown");
        } catch (Exception e) {

        }

    }
}
