package com.avioconsulting.mule.connector.vault.provider.internal.vault.client.auth.algorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Signing AWS Requests with Signature Version 4 in Java.
 *
 * @reference: http://docs.aws.amazon.com/general/latest/gr/sigv4_signing.html
 * @author AVIO Consulting
 * @date February 16th, 2022
 */
public class AWSV4Auth {

    private static final Logger logger = LoggerFactory.getLogger(AWSV4Auth.class);

    private AWSV4Auth() {
    }

    public static class Builder {

        private String accessKeyID;
        private String secretAccessKey;
        private String regionName;
        private String serviceName;
        private String httpMethodName;
        private String canonicalURI;
        private TreeMap<String, String> queryParametes;
        private TreeMap<String, String> awsHeaders;
        private String payload;
        private ZonedDateTime dateTime;

        public Builder(String accessKeyID, String secretAccessKey) {
            this.accessKeyID = accessKeyID;
            this.secretAccessKey = secretAccessKey;
        }

        public Builder regionName(String regionName) {
            this.regionName = regionName;
            return this;
        }

        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder httpMethodName(String httpMethodName) {
            this.httpMethodName = httpMethodName;
            return this;
        }

        public Builder canonicalURI(String canonicalURI) {
            this.canonicalURI = canonicalURI;
            return this;
        }

        public Builder queryParametes(TreeMap<String, String> queryParametes) {
            this.queryParametes = queryParametes;
            return this;
        }

        public Builder awsHeaders(TreeMap<String, String> awsHeaders) {
            this.awsHeaders = awsHeaders;
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public Builder dateTime(ZonedDateTime dateTime) {
            this.dateTime = dateTime;
            return this;
        }

        public AWSV4Auth build() {
            return new AWSV4Auth(this);
        }
    }

    private String accessKeyID;
    private String secretAccessKey;
    private String regionName;
    private String serviceName;
    private String httpMethodName;
    private String canonicalURI;
    private TreeMap<String, String> queryParametes;
    private TreeMap<String, String> awsHeaders;
    private String payload;

    /* Other variables */
    private final String HMACAlgorithm = "AWS4-HMAC-SHA256";
    private final String aws4Request = "aws4_request";
    private String strSignedHeader;
    private String xAmzDate;
    private ZonedDateTime currentDateTime;
    private String currentDate;


    private Map<String, String> headers;

    private AWSV4Auth(Builder builder) {
        accessKeyID = builder.accessKeyID;
        secretAccessKey = builder.secretAccessKey;
        regionName = builder.regionName;
        serviceName = builder.serviceName;
        httpMethodName = builder.httpMethodName;
        canonicalURI = builder.canonicalURI;
        queryParametes = builder.queryParametes;
        awsHeaders = builder.awsHeaders;
        payload = builder.payload;
        currentDateTime = builder.dateTime != null ? builder.dateTime : ZonedDateTime.now(ZoneOffset.UTC);

        /* Get current timestamp value.(UTC) */
        xAmzDate = getTimeStamp();
        currentDate = getDate();

        processSignature();
    }


    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getxAmzDate() {
        return xAmzDate;
    }

    /**
     * Task 1: Create a Canonical Request for Signature Version 4.
     *
     * @return
     */
    private String prepareCanonicalRequest() {
        StringBuilder canonicalURL = new StringBuilder("");

        /* Step 1.1 Start with the HTTP request method (GET, PUT, POST, etc.), followed by a newline character. */
        canonicalURL.append(httpMethodName).append("\n");

        /* Step 1.2 Add the canonical URI parameter, followed by a newline character. */
        canonicalURI = canonicalURI == null || canonicalURI.trim().isEmpty() ? "/" : canonicalURI;
        canonicalURL.append(canonicalURI).append("\n");

        /* Step 1.3 Add the canonical query string, followed by a newline character. */
        StringBuilder queryString = new StringBuilder("");
        if (queryParametes != null && !queryParametes.isEmpty()) {
            for (Map.Entry<String, String> entrySet : queryParametes.entrySet()) {
                String key = entrySet.getKey();
                String value = entrySet.getValue();
                queryString.append(key).append("=").append(encodeParameter(value)).append("&");
            }
            /* @co-author https://github.com/dotkebi @git #1 @date 16th March, 2017 */
            queryString.deleteCharAt(queryString.lastIndexOf("&"));
        }
        queryString.append("\n");
        canonicalURL.append(queryString);

        /* Step 1.4 Add the canonical headers, followed by a newline character. */
        StringBuilder signedHeaders = new StringBuilder("");
        if (awsHeaders != null && !awsHeaders.isEmpty()) {
            for (Map.Entry<String, String> entrySet : awsHeaders.entrySet()) {
                String key = entrySet.getKey();
                String value = entrySet.getValue();
                signedHeaders.append(key).append(";");
                canonicalURL.append(key).append(":").append(value).append("\n");
            }

            /* Note: Each individual header is followed by a newline character, meaning the complete list ends with a newline character. */
        }
        canonicalURL.append("\n");

        /* Step 1.5 Add the signed headers, followed by a newline character. */
        strSignedHeader = signedHeaders.substring(0, signedHeaders.length() - 1); // Remove last ";"
        canonicalURL.append(strSignedHeader).append("\n");

        /* Step 1.6 Use a hash (digest) function like SHA256 to create a hashed value from the payload in the body of the HTTP or HTTPS. */
        payload = payload == null ? "" : payload;
        canonicalURL.append(generateHex(payload));

        logger.debug("##Canonical Request:\n" + canonicalURL.toString());

        return canonicalURL.toString();
    }

    /**
     * Task 2: Create a String to Sign for Signature Version 4.
     *
     * @param canonicalURL
     * @return
     */
    private String prepareStringToSign(String canonicalURL) {
        String stringToSign = "";

        /* Step 2.1 Start with the algorithm designation, followed by a newline character. */
        stringToSign = HMACAlgorithm + "\n";

        /* Step 2.2 Append the request date value, followed by a newline character. */
        stringToSign += xAmzDate + "\n";

        /* Step 2.3 Append the credential scope value, followed by a newline character. */
        stringToSign += currentDate + "/" + regionName + "/" + serviceName + "/" + aws4Request + "\n";

        /* Step 2.4 Append the hash of the canonical request that you created in Task 1: Create a Canonical Request for Signature Version 4. */
        stringToSign += generateHex(canonicalURL);

        logger.debug("##String to sign:\n" + stringToSign);

        return stringToSign;
    }

    /**
     * Task 3: Calculate the AWS Signature Version 4.
     *
     * @param stringToSign
     * @return
     */
    private String calculateSignature(String stringToSign) {
        try {
            /* Step 3.1 Derive your signing key */
            byte[] signatureKey = getSignatureKey(secretAccessKey, currentDate, regionName, serviceName);

            /* Step 3.2 Calculate the signature. */
            byte[] signature = HmacSHA256(signatureKey, stringToSign);

            /* Step 3.2.1 Encode signature (byte[]) to Hex */
            String strHexSignature = bytesToHex(signature);
            return strHexSignature;
        } catch (Exception ex) {
            logger.error("Exception calculating signature", ex);
        }
        return null;
    }

    /**
     * Task 4: Add the Signing Information to the Request. We'll return Map of
     * all headers put this headers in your request.
     *
     * @return
     */
    private void processSignature() {
        awsHeaders.put("x-amz-date", xAmzDate);

        /* Execute Task 1: Create a Canonical Request for Signature Version 4. */
        String canonicalURL = prepareCanonicalRequest();

        /* Execute Task 2: Create a String to Sign for Signature Version 4. */
        String stringToSign = prepareStringToSign(canonicalURL);

        /* Execute Task 3: Calculate the AWS Signature Version 4. */
        String signature = calculateSignature(stringToSign);

        if (signature != null) {
            Map<String, String> header = new HashMap(0);
            header.put("x-amz-date", xAmzDate);
            header.put("Authorization", buildAuthorizationString(signature));

            logger.debug("##Signature:\n" + signature);
            logger.debug("##Header:");
            for (Map.Entry<String, String> entrySet : header.entrySet()) {
                logger.debug(entrySet.getKey() + " = " + entrySet.getValue());
            }
            logger.debug("================================");
            headers = header;
        } else {
            logger.debug("##Signature:\n" + signature);
        }
    }



    /**
     * Build string for Authorization header.
     *
     * @param strSignature
     * @return
     */
    private String buildAuthorizationString(String strSignature) {
        return HMACAlgorithm + " "
                + "Credential=" + accessKeyID + "/" + getDate() + "/" + regionName + "/" + serviceName + "/" + aws4Request + ","
                + "SignedHeaders=" + strSignedHeader + ","
                + "Signature=" + strSignature;
    }

    /**
     * Generate Hex code of String.
     *
     * @param data
     * @return
     */
    private String generateHex(String data) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(data.getBytes("UTF-8"));
            byte[] digest = messageDigest.digest();
            return String.format("%064x", new java.math.BigInteger(1, digest));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            logger.error("Exception while generating hex from string", e);
        }
        return null;
    }

    /**
     * Apply HmacSHA256 on data using given key.
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     * @reference:
     * http://docs.aws.amazon.com/general/latest/gr/signature-v4-examples.html#signature-v4-examples-java
     */
    private byte[] HmacSHA256(byte[] key, String data) throws Exception {
        if (data != null) {
            String algorithm = "HmacSHA256";
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(key, algorithm));
            return mac.doFinal(data.getBytes("UTF8"));
        } else {
            return null;
        }
    }

    /**
     * Generate AWS signature key.
     *
     * @param key
     * @param date
     * @param regionName
     * @param serviceName
     * @return
     * @throws Exception
     * @reference
     * http://docs.aws.amazon.com/general/latest/gr/signature-v4-examples.html#signature-v4-examples-java
     */
    private byte[] getSignatureKey(String key, String date, String regionName, String serviceName) throws Exception {
        byte[] kSecret = ("AWS4" + key).getBytes("UTF8");
        byte[] kDate = HmacSHA256(kSecret, date);
        byte[] kRegion = HmacSHA256(kDate, regionName);
        byte[] kService = HmacSHA256(kRegion, serviceName);
        byte[] kSigning = HmacSHA256(kService, aws4Request);
        return kSigning;
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    /**
     * Convert byte array to Hex
     *
     * @param bytes
     * @return
     */
    private String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars).toLowerCase();
    }

    /**
     * Get timestamp. yyyyMMdd'T'HHmmss'Z'
     *
     * @return
     */
    private String getTimeStamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX");
        return currentDateTime.format(formatter);
    }

    /**
     * Get date. yyyyMMdd
     *
     * @return
     */
    private String getDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return currentDateTime.format(formatter);
    }

    /**
     * Using {@link URLEncoder#encode(String, String) } instead of
     * {@link URLEncoder#encode(String) }
     *
     * @co-author https://github.com/dotkebi
     * @date 16th March, 2017
     * @git #1
     * @param param
     * @return
     */
    private String encodeParameter(String param){
        try {
            return URLEncoder.encode(param, "UTF-8");
        } catch (Exception e) {
            return URLEncoder.encode(param);
        }
    }
}