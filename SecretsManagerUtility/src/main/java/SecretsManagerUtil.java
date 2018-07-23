import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * This class is a utility class for fetching the secret from SecretsManager.
 * <p>
 * Created by utkarsh on 02/07/18.
 */
public class SecretsManagerUtil {

    private AWSSecretsManager client;

    /**
     * Constructor for SecretsManagerUtil that initializes the AWS SecretsManger client.
     */
    public SecretsManagerUtil() {
        AWSSecretsManagerClientBuilder builder = AWSSecretsManagerClientBuilder.standard();
        this.client = builder.build();
    }

    /**
     * Returns the key corresponding to the secretName from AWS Secrets Manager.
     *
     * @param secretName : The secret name of the secret to be fetched
     * @return : The secret requested fetched from SecretsManager.
     */
    public String getSecret(String secretName) throws InvalidParameterException, InvalidRequestException,
            ResourceNotFoundException, IllegalArgumentException {
        String secret = null;
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                .withSecretId(secretName);
        GetSecretValueResult getSecretValueResult = null;
        getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        if (getSecretValueResult == null) {
            throw new IllegalArgumentException("No secret value returned for the secret name " + secretName);
        }
        // Decrypted secret using the associated KMS CMK
        if (getSecretValueResult.getSecretString() != null) {
            // Fetch secret if stored as a String
            secret = getSecretValueResult.getSecretString();
        } else {
            throw new AWSSecretsManagerException("Secret can only be fetched when stored as a String");
        }
        return secret;
    }

    /**
     * Secret retrieved from AWS Secrets Manager is in the form of a JSON. This function can be used to retrieve
     * a particular key from that JSON.
     *
     * @param secretName : The name of the secret to be retrieved from Secrets Manager
     * @param keyName    : The particular field to be retrieved from the secret fetched.
     * @return : The value corresponding to keyName field from the secret fetched.
     */
    public String getKey(String secretName, String keyName) throws InvalidParameterException, InvalidRequestException,
            ResourceNotFoundException, IllegalArgumentException, IOException {
        JsonNode key = getJsonNode(getSecret(secretName));
        return key.get(keyName).asText();
    }

    /**
     * Function that shuts down AWSSecretsManager client.
     * NOTE: SHOULD ONLY BE CALLED WHEN SecretsManagerUtil IS NO LONGER REQUIRED.
     */
    public String release() {
        try {
            client.shutdown();
        } catch (Exception e) {
            return "Error while trying to shutdown secrets manager client";
        }
        return null;
    }

    private JsonNode getJsonNode(String node) throws IOException {
        ObjectMapper om = new ObjectMapper();
        return om.readTree(node);
    }
}
