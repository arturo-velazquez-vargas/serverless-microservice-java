package com.serverless;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class UpdateItemHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final DynamoDB dynamoDb = new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient());
    private final String tableName = System.getenv("TABLE_NAME");

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode body = objectMapper.readTree(input.getBody());
            String id = input.getPathParameters().get("id");

            Map<String, String> expressionAttributeNames = new HashMap<>();
            expressionAttributeNames.put("#name", "name");

            Map<String, Object> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":name", body.get("name").asText());

            UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                    .withPrimaryKey("id", id)
                    .withUpdateExpression("set #name = :name")
                    .withNameMap(expressionAttributeNames)
                    .withValueMap(expressionAttributeValues)
                    .withReturnValues(ReturnValue.UPDATED_NEW);

            Table table = dynamoDb.getTable(tableName);
            UpdateItemOutcome outcome = table.updateItem(updateItemSpec);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody("{\"message\":\"Item updated successfully\", \"updatedAttributes\":" + outcome.getItem().toJSON() + "}");
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("{\"message\":\"Error updating item\"}");
        }
    }
}
