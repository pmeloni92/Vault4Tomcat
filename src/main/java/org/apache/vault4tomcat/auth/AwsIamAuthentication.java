package org.apache.vault4tomcat.auth;

import org.apache.vault4tomcat.config.VaultConfig;
import org.apache.vault4tomcat.vault.Vault;
import org.apache.vault4tomcat.vault.response.LogicalResponse;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * VaultAuthenticator implementation for <a href="https://developer.hashicorp.com/vault/docs/auth/aws#iam-authentication-inferences">AWS IAM authentication</a>.
 *
 */
public class AwsIamAuthentication implements VaultAuthenticator {

    // Prepare the AWS STS GetCallerIdentity request to be signed
    public static final String stsEndpoint = "https://sts.amazonaws.com/";
    public static final String stsRegion = "us-east-1";
    public static final String stsService = "sts";

    private static final String stsActionBody = "Action=GetCallerIdentity&Version=2011-06-15";

    private static final String ALGORITHM = "AWS4-HMAC-SHA256";

    /**
     * Makes an API call to fetch client token using app role id and secret id defined in the VaultConfig.
     *
     * @param vault VaultConfig containing the app role id and the secret id
     * @return the Vault token
     * @throws IllegalArgumentException if the app role id is missing or empty
     */
    @Override
    public String authenticate(Vault vault) throws Exception {
        VaultConfig config = vault.getConfig();
        String role = config.getAwsRole();
        if (role == null || role.isEmpty()) {
            throw new IllegalArgumentException("AWS authentication requires a role name");
        }

        String headerJson = createHeaderJsonForPostRequest(config, config.getAwsService(), config.getAwsRegion(), config.getAwsEndpoint());

        String urlB64 = Base64.getEncoder().encodeToString(stsEndpoint.getBytes(StandardCharsets.UTF_8));
        String bodyB64 = Base64.getEncoder().encodeToString(stsActionBody.getBytes(StandardCharsets.UTF_8));
        String headersB64 = Base64.getEncoder().encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
        LogicalResponse logicalResponse = vault.logical().login(role, urlB64, bodyB64, headersB64);

        String token = logicalResponse.getData().get(CLIENT_TOKEN);
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Failed to login");
        }
        return token;
    }

    private String createHeaderJsonForPostRequest(VaultConfig config, String service, String region, String requestUrl) throws URISyntaxException {
        URI uri = new URI(requestUrl);

        Date now = new Date();

        DateFormat dfm = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        dfm.setTimeZone(TimeZone.getTimeZone("UTC"));
        String amzDate = dfm.format(now);

        DateFormat dfm1 = new SimpleDateFormat("yyyyMMdd");
        dfm1.setTimeZone(TimeZone.getTimeZone("UTC"));
        String datestamp = dfm1.format(now);

        String payloadHash = calculateHash(stsActionBody);
        Map<String, String> headers = new HashMap<>();
        headers.put("x-amz-content-sha256", payloadHash);

        headers.put("Content-Length", String.valueOf(stsActionBody.length()));
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");

        String sessionToken = config.getAwsSessionToken();
        if (sessionToken != null && !sessionToken.isEmpty()) {
            headers.put("x-amz-security-token", sessionToken);
        }

        headers.put("X-Amz-Date", amzDate);

        String iamServerId = config.getAwsHeaderValue();
        if (iamServerId != null && !iamServerId.isEmpty()) {
            headers.put("X-Vault-AWS-IAM-Server-Id", iamServerId);
        }

        headers.put("host", uri.getHost());

        String authorizationHeader = getAuthorizationHeader(config, service, region, uri.getPath(), amzDate, datestamp, headers, payloadHash);
        headers.put("Authorization", authorizationHeader);

        return headers.entrySet().stream()
                .filter(map -> !map.getKey().equals("host"))
                .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .map(e -> String.format("\"%s\" : [ \"%s\" ]",
                        e.getKey(),
                        e.getValue()))
                .collect(Collectors.joining(", ", "{ ", " }"));
    }

    private String getAuthorizationHeader(VaultConfig config, String service, String region, String uriPath, String amzDate, String datestamp,
                                          Map<String, String> headers, String payloadHash) {

        // Create the canonical request
        String canonicalHeaders = headers.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .map(entry -> entry.getKey().toLowerCase() + ":" + entry.getValue().trim() + "\n")
                .collect(Collectors.joining());
        String signedHeaders = headers.keySet().stream()
                .map(String::toLowerCase)
                .sorted()
                .collect(Collectors.joining(";"));

        String canonicalRequest = "POST" + "\n" + uriPath + "\n\n" +
                canonicalHeaders + "\n" + signedHeaders + "\n" + payloadHash;

        // Create the string to sign
        String credentialScope = datestamp + "/" + region + "/" + service + "/aws4_request";
        String stringToSign = ALGORITHM + "\n" + amzDate + "\n" + credentialScope + "\n" + calculateHash(canonicalRequest);

        // Calculate the signature
        byte[] signingKey = getSignatureKey(config.getAwsSecretKey(), datestamp, region, service);
        String signature = calculateHmacHex(signingKey, stringToSign);

        // Create the authorization header
        return ALGORITHM + " Credential=" + config.getAwsAccessKey() + "/" + credentialScope +
                ", SignedHeaders=" + signedHeaders + ", Signature=" + signature;
    }

    private byte[] getSignatureKey(String key, String dateStamp, String regionName, String serviceName) {
        byte[] kSecret = ("AWS4" + key).getBytes(StandardCharsets.UTF_8);
        byte[] kDate = hmacSHA256(kSecret, dateStamp);
        byte[] kRegion = hmacSHA256(kDate, regionName);
        byte[] kService = hmacSHA256(kRegion, serviceName);
        return hmacSHA256(kService, "aws4_request");
    }

    private String calculateHmacHex(byte[] key, String data) {
        byte[] hmac = hmacSHA256(key, data);
        return bytesToHex(hmac);
    }

    private byte[] hmacSHA256(byte[] key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC-SHA256", e);
        }
    }

    private String calculateHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate SHA-256 hash", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

}
