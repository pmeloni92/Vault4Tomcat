package org.apache.vault4tomcat.vault.response;

import org.apache.vault4tomcat.vault.json.Json;
import org.apache.vault4tomcat.vault.json.JsonObject;
import org.apache.vault4tomcat.vault.json.JsonValue;
import org.apache.vault4tomcat.vault.rest.RestResponse;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A simplified response wrapper for Vault logical operations, tailored for KV v2 read-only access.
 */
public class LogicalResponse {

    private final Map<String, String> data = new HashMap<>();
    private final Map<String, String> metadata = new HashMap<>();

    /**
     * Constructs a LogicalResponse with status and secret data.
     *
     * @param restResponse The raw HTTP response from Vault.
     */
    public LogicalResponse(final RestResponse restResponse, final String operation) {
        parseResponseData(restResponse.body(), operation);
    }

    private void parseResponseData(final byte[] responseBytes, final String operation) {
        try {
            final String jsonString = new String(responseBytes, StandardCharsets.UTF_8);
            JsonObject jsonObject = Json.parse(jsonString).asObject();

            if (operation.equals("readV2")) {
                jsonObject = jsonObject.get("data").asObject();
                JsonValue metadataValue = jsonObject.get("metadata");
                if (null != metadataValue) {
                    parseJsonIntoMap(metadataValue.asObject(), this.metadata);
                }
            } else if (operation.equals("login")) {
                jsonObject = jsonObject.get("auth").asObject();
                parseJsonIntoMap(jsonObject, this.data);
                return;
            }

            parseJsonIntoMap(jsonObject.get("data").asObject(), this.data);
        } catch (Exception ignored) {
        }
    }

    private void parseJsonIntoMap(final JsonObject jsonObject, final Map<String, String> map) {
        for (final JsonObject.Member member : jsonObject) {
            final JsonValue jsonValue = member.getValue();

            if (jsonValue == null || jsonValue.isNull()) {
                continue;
            }

            if (jsonValue.isString()) {
                map.put(member.getName(), jsonValue.asString());
            } else {
                map.put(member.getName(), jsonValue.toString());
            }
        }
    }

    /**
     * @return Unmodifiable map of secret key-value pairs
     */
    public Map<String, String> getData() {
        return Collections.unmodifiableMap(data);
    }

}
