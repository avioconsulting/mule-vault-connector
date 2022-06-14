package com.avioconsulting.mule.connector.vault.provider.internal.vault.client.auth;

import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.auth.algorithm.AWSV4SignProperties;
import com.avioconsulting.mule.connector.vault.provider.internal.vault.client.exception.VaultException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

@RunWith(Parameterized.class)
public class AWSV4SignPropertiesTests {

    private final String url;
    private final String host;
    private final String canonicalUri;
    private final Map<String, String> queryParameters;
    private Class<Throwable> exceptionClass;

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Parameterized.Parameters(name = "{index}: Test with url={0}, host: {1}, canonicalUri: {2}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"http://sts.amazonaws.com", "sts.amazonaws.com", "", Collections.emptyMap(), null},
                {"https://sts.amazonaws.com", "sts.amazonaws.com", "", Collections.emptyMap(), null},
                {"https://sts.amazonaws.com/", "sts.amazonaws.com", "/", Collections.emptyMap(), null},
                {"https://sts.amazonaws.com/?", "sts.amazonaws.com", "/", Collections.emptyMap(), null},
                {"https://sts.amazonaws.com/?Action", "sts.amazonaws.com", "/", Collections.emptyMap(), null},
                {"https://sts.amazonaws.com/?Action=", "sts.amazonaws.com", "/", Collections.emptyMap(), null},
                {"https://sts.amazonaws.com/?Action=GetCallerIdentity", "sts.amazonaws.com", "/", Collections.singletonMap("Action", "GetCallerIdentity"), null},
                {"https://sts.amazonaws.com/?Action=Your%20text", "sts.amazonaws.com", "/", Collections.singletonMap("Action", "Your text"), null},
                {"https://sts.amazonaws.com/?Action=GetCallerIdentity&Version=2011-06-15", "sts.amazonaws.com", "/", ImmutableMap.of("Action", "GetCallerIdentity", "Version", "2011-06-15"), null},
                {"https://sts.amazonaws.com/?Action=GetCallerIdentity&Version=2011-06-15", "sts.amazonaws.com", "/", ImmutableMap.of("Action", "GetCallerIdentity", "Version", "2011-06-15"), null},
                {"https://sts.amazonaws.com?Action=GetCallerIdentity&Version=2011-06-15", "sts.amazonaws.com", "", ImmutableMap.of("Action", "GetCallerIdentity", "Version", "2011-06-15"), null},
                {":invalid-url.com", "", "", Collections.emptyMap(), VaultException.class},
        });
    }

    public AWSV4SignPropertiesTests(String url, String host, String canonicalUri, Map<String, String> queryParameters,
                                    Class<Throwable> exceptionClass) {
        this.url = url;
        this.host = host;
        this.canonicalUri = canonicalUri;
        this.queryParameters = queryParameters;
        this.exceptionClass = exceptionClass;
    }

    @Test
    public void testUrl() throws VaultException {

        if (exceptionClass != null) {
            expected.expect(exceptionClass);
        }

        AWSV4SignProperties awsV4SignProperties = new AWSV4SignProperties(url);

        Assert.assertEquals(host, awsV4SignProperties.getHost());
        Assert.assertEquals(canonicalUri, awsV4SignProperties.getCanonicalUri());
        Assert.assertEquals(queryParameters, awsV4SignProperties.getQueryParameters());

    }

}
