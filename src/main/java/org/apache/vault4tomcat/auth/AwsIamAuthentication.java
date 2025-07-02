package org.apache.vault4tomcat.auth;

import org.apache.vault4tomcat.config.VaultConfig;
import org.apache.vault4tomcat.vault.Vault;
import org.apache.vault4tomcat.vault.response.LogicalResponse;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.http.ContentStreamProvider;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.http.SdkHttpRequest;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4HttpSigner;
import software.amazon.awssdk.http.auth.spi.signer.SignedRequest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * VaultAuthenticator implementation for <a href="https://developer.hashicorp.com/vault/docs/auth/aws#iam-authentication-inferences">AWS IAM authentication</a>.
 *
 */
public class AwsIamAuthentication implements VaultAuthenticator {

    // Prepare the AWS STS GetCallerIdentity request to be signed
    private static final String stsEndpoint = "https://sts.amazonaws.com/";
    private static final String stsActionBody = "Action=GetCallerIdentity&Version=2011-06-15";
    private static final byte[] bodyBytes = stsActionBody.getBytes(StandardCharsets.UTF_8);


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

        SignedRequest signed = generateSignedRequest(config);
        // Extract signed headers (except the Host header) to build the Vault login payload
        Map<String, List<String>> signedHeaders = signed.request().headers();

        String headerJson = "{" + signedHeaders.entrySet().stream()
                .filter(map -> !map.getKey().equals("Host"))
                .map(e -> "\"" + e.getKey() + "\": [\"" + e.getValue().getFirst() + "\"]")
                .collect(Collectors.joining(", ")) + "}";

        String urlB64 = Base64.getEncoder().encodeToString(stsEndpoint.getBytes(StandardCharsets.UTF_8));
        String bodyB64 = Base64.getEncoder().encodeToString(bodyBytes);
        String headersB64 = Base64.getEncoder().encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
        LogicalResponse logicalResponse = vault.logical().login(role, urlB64, bodyB64, headersB64);

        String token = logicalResponse.getData().get(CLIENT_TOKEN);
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Failed to login");
        }
        return token;
    }

    private SignedRequest generateSignedRequest(VaultConfig config) {

        AwsCredentials awsCreds = resolveAwsCredentials(config);

        // Build the unsigned STS HTTP request (method, URL, headers, body)
        SdkHttpRequest.Builder requestBuilder = SdkHttpRequest.builder()
                .method(SdkHttpMethod.POST)
                .uri(stsEndpoint)
                .putHeader("Host", "sts.amazonaws.com")
                .putHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                .putHeader("Content-Length", String.valueOf(bodyBytes.length));
        // Include Vault AWS IAM server ID header if configured
        String iamServerId = config.getAwsHeaderValue();
        if (iamServerId != null && !iamServerId.isEmpty()) {
            requestBuilder.putHeader("X-Vault-AWS-IAM-Server-Id", iamServerId);
        }
        SdkHttpRequest unsignedRequest = requestBuilder.build();

        // Create the request payload to be signed
        ContentStreamProvider requestPayload = ContentStreamProvider.fromUtf8String(stsActionBody);

        return AwsV4HttpSigner.create().sign(r -> r.identity(awsCreds)
                .request(unsignedRequest)
                .payload(requestPayload)
                .putProperty(AwsV4HttpSigner.SERVICE_SIGNING_NAME, "sts")
                .putProperty(AwsV4HttpSigner.REGION_NAME, config.getAwsRegion()));
    }

    private AwsCredentialsProvider createAwsCredentialsProvider(VaultConfig config) {
        String accessKey = config.getAwsAccessKey();
        String secretKey = config.getAwsSecretKey();
        String sessionToken = config.getAwsSessionToken();
        if (accessKey != null && !accessKey.isEmpty() && secretKey != null && !secretKey.isEmpty()) {
            // Use static credentials from environment (VaultConfig) if provided
            AwsCredentials creds;
            if (sessionToken != null && !sessionToken.isEmpty()) {
                creds = AwsSessionCredentials.create(accessKey, secretKey, sessionToken);
            } else {
                creds = AwsBasicCredentials.create(accessKey, secretKey);
            }
            return StaticCredentialsProvider.create(creds);
        } else {
            // Fall back to default AWS credentials provider chain
            return DefaultCredentialsProvider.create();
        }
    }

    private AwsCredentials resolveAwsCredentials(VaultConfig config) {
        try {
            return createAwsCredentialsProvider(config).resolveCredentials();
        } catch (Exception e) {
            throw new IllegalArgumentException("No AWS credentials available for IAM authentication: " + e.getMessage());
        }
    }
}
