package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.time.Instant;
import java.util.*;

public class S3EventHandler implements RequestHandler<S3Event, String> {


    private static final String EXPECTED_BUCKET = "BucketName";
    private static final String TABLE_NAME = "TableName";

    private final AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.defaultClient();

    @Override
public String handleRequest(S3Event s3event, Context context) {
    try {
        s3event.getRecords().forEach(record -> {
            String bucketName = record.getS3().getBucket().getName();
            String filePath = record.getS3().getObject().getKey();

            if (!EXPECTED_BUCKET.equals(bucketName)) {
                System.out.println("Ignoring event from unexpected bucket: " + bucketName);
                return;
            }

            Map<String, AttributeValue> item = buildDynamoItem(bucketName, filePath);

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


private Map<String, AttributeValue> buildDynamoItem(String bucket, String key) {
    Map<String, AttributeValue> item = new HashMap<>();
    item.put("id", new AttributeValue(UUID.randomUUID().toString()));
    item.put("UserId", new AttributeValue("76567"));
    item.put("FilePath", new AttributeValue(key));
    item.put("Assigned User", new AttributeValue("Ramesh"));
    item.put("PdfStatus", new AttributeValue("Unassigned"));
    item.put("File Filter", new AttributeValue("Hand Delivered"));
    item.put("DateUploaded", new AttributeValue(Instant.now().toString()));

    return item;
}


    private void log(String msg) {
        System.out.println("[S3EventHandler] " + msg);
    }
}
