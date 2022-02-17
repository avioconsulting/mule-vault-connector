package com.avioconsulting.mule.connector.vault.provider.internal.vault.client.auth.algorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class AWSV4SignProperties {

    private static final Logger logger = LoggerFactory.getLogger(AWSV4SignProperties.class);

    private static final String DEFAULT_HOST = "sts.amazonaws.com";
    private static final String DEFAULT_SERVICE_NAME = "sts";
    private static final String DEFAULT_REGION = "us-east-1";
    private static final String DEFAULT_METHOD = "POST";
    private static final String DEFAULT_PAYLOAD = "Action=GetCallerIdentity&Version=2011-06-15";
    private static final String DEFAULT_CANONICAL_URI = "/";

    private String region = DEFAULT_REGION;
    private String serviceName = DEFAULT_SERVICE_NAME;
    private String method = DEFAULT_METHOD;
    private String host;
    private String canonicalUri;
    private Map<String, String> queryParameters;

    public AWSV4SignProperties(String url) {
        process(url);
    }

    public String getRegion() {
        return region;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getMethod() {
        return method;
    }

    public String getHost() {
        return host;
    }

    public String getCanonicalUri() {
        return canonicalUri;
    }

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    private void process(String url) {
        URI uri;
        try {
            uri = new URI(url);
            host = uri.getHost();
            canonicalUri = uri.getPath();
            queryParameters = splitQuery(uri.getQuery());
        } catch (URISyntaxException e) {
            logger.error(String.format("Failed to parse URL and extract the properties. URL: %s", url), e);
            logger.warn("Setting all variables to the default values");
            host = DEFAULT_HOST;
            canonicalUri = DEFAULT_CANONICAL_URI;
            queryParameters = Collections.emptyMap();
        }
    }


    private Map<String, String> splitQuery(String query) {
        if (query == null || query.isEmpty()) {
            return Collections.emptyMap();
        }
        return Arrays.stream(query.
                        split("&")).
                map(this::splitQueryParameter).
                filter(q -> q != null).
                collect(Collectors.toMap(AbstractMap.SimpleImmutableEntry::getKey, AbstractMap.SimpleImmutableEntry::getValue));
    }

    private AbstractMap.SimpleImmutableEntry<String, String> splitQueryParameter(String it) {
        final int idx = it.indexOf("=");
        final String key = idx > 0 ? it.substring(0, idx) : it;
        final String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
        if (key == null || value == null) {
            logger.warn(String.format("Query parameter is not set properly. Key: %s, Value: %s", key, value));
            return null;
        }
        try {
            return new AbstractMap.SimpleImmutableEntry<>(
                    URLDecoder.decode(key, "UTF-8"),
                    URLDecoder.decode(value, "UTF-8")
            );
        } catch (UnsupportedEncodingException e) {
            logger.error(String.format("Failed to create map record for query parameter: %s", it), e);
            return null;
        }
    }

}
