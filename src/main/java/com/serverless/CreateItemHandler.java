package com.serverless;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CreateItemHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger logger = LogManager.getLogger(CreateItemHandler.class);

    private final DynamoDB dynamoDb = new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient());
    private final String tableName = System.getenv("TABLE_NAME");

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        logger.info("Received request from gateway: {}", input.getBody());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode body = objectMapper.readTree(input.getBody());
            logger.info("Parsed request body: {}", body.toString());

            Table table = dynamoDb.getTable(tableName);
            Item item = new Item()
                    .withPrimaryKey("id", body.get("id").asText())
                    .withString("name", body.get("name").asText());

            logger.info("Putting item into table: {}", item.toJSON());
            table.putItem(new PutItemSpec().withItem(item));

            logger.info("Item created successfully");
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(201)
                    .withBody("{\"message\":\"Item created successfully\"}");
        } catch (Exception e) {
            logger.error("Error creating item", e);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("{\"message\":\"Error creating item\"}");
        }
    }
}
