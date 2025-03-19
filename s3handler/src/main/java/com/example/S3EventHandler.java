package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class S3EventHandler implements RequestHandler<S3Event, String> {

    private final AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.defaultClient();
    private final String TABLE_NAME = "sop_workflow_dynamodb"; // replace with your actual table name
    private final String EXPECTED_BUCKET = "onyxsopworkflow";

    @Override
    public String handleRequest(S3Event s3event, Context context) {
        try {
            s3event.getRecords().forEach(record -> {
                String bucketName = record.getS3().getBucket().getName();
                String filePath = record.getS3().getObject().getKey();

                // Optional: Validate bucket name
                if (!EXPECTED_BUCKET.equals(bucketName)) {
                    System.out.println("Ignoring event from unexpected bucket: " + bucketName);
                    return;
                }

                // Build item for DynamoDB
                Map<String, AttributeValue> item = new HashMap<>();
                item.put("id", new AttributeValue(UUID.randomUUID().toString()));
                item.put("FilePath", new AttributeValue(filePath));
                item.put("BucketName", new AttributeValue(bucketName));
                item.put("Status", new AttributeValue("Unassigned"));
                item.put("DateUploaded", new AttributeValue(Instant.now().toString()));

                // Insert into DynamoDB
                PutItemRequest request = new PutItemRequest()
                        .withTableName(TABLE_NAME)
                        .withItem(item);

                dynamoDB.putItem(request);

                System.out.println("Item added to DynamoDB for file: " + filePath);
            });

            return "Success";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
