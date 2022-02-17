package com.avioconsulting.mule.connector.vault.provider.internal.vault.client.auth;

import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.auth.algorithm.AWSV4Auth;
import org.junit.Assert;
import org.junit.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.TreeMap;

public class AWSV4AuthTests {

    private static final String DEFAULT_HOST = "sts.amazonaws.com";
    private static final String DEFAULT_SERVICE_NAME = "sts";
    private static final String DEFAULT_REGION = "us-east-1";
    private static final String DEFAULT_METHOD = "POST";
    private static final String DEFAULT_PAYLOAD = "Action=GetCallerIdentity&Version=2011-06-15";
    private static final String DEFAULT_CANONICAL_URI = "/";

    private static final String FAKE_ACCESS_KEY = "FAKEACCESSKEY";
    private static final String FAKE_SECRET_KEY = "FAKESECRETKEY";

    @Test
    public void testSignV4WithoutServerId() {

        TreeMap<String, String> awsHeaders = new TreeMap();
        awsHeaders.put("host", DEFAULT_HOST);

        AWSV4Auth awsV4Auth = new AWSV4Auth.Builder(FAKE_ACCESS_KEY, FAKE_SECRET_KEY)
                .regionName(DEFAULT_REGION)
                .serviceName(DEFAULT_SERVICE_NAME)
                .httpMethodName(DEFAULT_METHOD)
                .canonicalURI(DEFAULT_CANONICAL_URI) //end point
                .queryParametes(null)
                .awsHeaders(awsHeaders)
                .payload(DEFAULT_PAYLOAD)
                .dateTime(ZonedDateTime.of(2022, 02, 17, 14, 49, 00, 0, ZoneOffset.UTC))
                .build();

        String authorization = awsV4Auth.getHeaders().get("Authorization");

        String expectedAmzDate = "20220217T144900Z";
        String expectedAuthorization = "AWS4-HMAC-SHA256 Credential=FAKEACCESSKEY/20220217/us-east-1/sts/aws4_request,SignedHeaders=host;x-amz-date,Signature=63b5ecc5745df80ac2299da2e084a24362cea27953b4a274c66fa219f234983c";

        Assert.assertEquals(expectedAmzDate, awsV4Auth.getxAmzDate());
        Assert.assertEquals(expectedAuthorization, authorization);

    }

    @Test
    public void testSignV4WithEmptyServerId() {

        TreeMap<String, String> awsHeaders = new TreeMap();
        awsHeaders.put("host", DEFAULT_HOST);
        awsHeaders.put("x-vault-aws-iam-server-id", "dev.vault.avioconsulting.com");

        AWSV4Auth awsV4Auth = new AWSV4Auth.Builder(FAKE_ACCESS_KEY, FAKE_ACCESS_KEY)
                .regionName(DEFAULT_REGION)
                .serviceName(DEFAULT_SERVICE_NAME)
                .httpMethodName(DEFAULT_METHOD)
                .canonicalURI(DEFAULT_CANONICAL_URI)
                .queryParametes(null)
                .awsHeaders(awsHeaders)
                .payload(DEFAULT_PAYLOAD)
                .dateTime(ZonedDateTime.of(2022, 02, 17, 15, 15, 00, 0, ZoneOffset.UTC))
                .build();

        String authorization = awsV4Auth.getHeaders().get("Authorization");

        String expectedAmzDate = "20220217T151500Z";
        String expectedAuthorization = "AWS4-HMAC-SHA256 Credential=FAKEACCESSKEY/20220217/us-east-1/sts/aws4_request,SignedHeaders=host;x-amz-date;x-vault-aws-iam-server-id,Signature=8c97f180ef50181de13fc03490fe8a988304c5613197ce307097032885076745";

        Assert.assertEquals(expectedAmzDate, awsV4Auth.getxAmzDate());
        Assert.assertEquals(expectedAuthorization, authorization);

    }

}
