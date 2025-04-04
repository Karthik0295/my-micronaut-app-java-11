package com.example;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Part;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.CompletedFileUpload;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.core.sync.RequestBody;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Controller("/s3")
public class S3Controller {

    @Inject
    @Singleton // Used this as there are many instances for s3 client
    private S3Client s3Client;
    @Inject
    private DynamoDbClient dynamoDbClient;

    private static final String BUCKET_NAME = "bucketname";
    private static final String TABLE_NAME = "tablename";


    @Post(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA)
    public String uploadFile(
            @Part("file") CompletedFileUpload file,
            @Part("date") String date, // Expected format: yyyy-MM-dd
            @Part("docType") String docType,
            @Part("fileLocation") String fileLocation,
            @Part("fileFilter") String fileFilter) {

        try {
            // Validate and format the date
            LocalDate parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // Extract the original file name and extension
            String originalFileName = file.getFilename();
            String fileExtension = "";
            int dotIndex = originalFileName.lastIndexOf(".");
            if (dotIndex > 0) {
                fileExtension = originalFileName.substring(dotIndex); // e.g., ".pdf"
                originalFileName = originalFileName.substring(0, dotIndex); // Remove extension from filename
            }

            // Generate a timestamp to ensure unique file names
            String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
            String finalFileName = originalFileName + "_" + timestamp + fileExtension;

            // Construct the S3 Key dynamically based on request parameters
            String s3Key = String.format("%s/%s/%s/%s/%s",
                    parsedDate, docType, fileLocation, fileFilter, finalFileName);

            // Upload file to S3
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(BUCKET_NAME)
                            .key(s3Key)
                            .contentType(file.getContentType().map(MediaType::toString).orElse("application/octet-stream"))
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );

            return "File uploaded successfully: " + s3Key;
        } catch (IOException e) {
            return "Error uploading file: " + e.getMessage();
        }
    }



    @Get("/getAllRecords")
    public List<Map<String, String>> getAllRecords() {
        // Scan request to fetch all data from DynamoDB table
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build();

        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

        // Convert DynamoDB items to JSON-friendly format
        return scanResponse.items().stream()
                .map(item -> item.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().s() // Extracting string values
                        )))
                .collect(Collectors.toList());
    }


    @Post(value = "/uploadmultiple", consumes = MediaType.MULTIPART_FORM_DATA)
    public List<String> uploadMultipleFiles(@Part List<CompletedFileUpload> files) {
        if (files == null || files.isEmpty()) {
            return List.of("No files received!");
        }

        System.out.println("Received files: " + files.size()); // Debugging line

        return files.stream().map(file -> {
            try {
                s3Client.putObject(
                    PutObjectRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key(file.getFilename()) 
                        .contentType(file.getContentType().map(MediaType::toString).orElse("application/octet-stream"))
                        .build(),
                    RequestBody.fromBytes(file.getBytes())
                );
                return "File uploaded successfully: " + file.getFilename();
            } catch (IOException e) {
                return "Error uploading file: " + file.getFilename() + " -> " + e.getMessage();
            }
        }).collect(Collectors.toList());
    }




    @Get("/list")
    public List<String> listFiles() {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(BUCKET_NAME)
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);

        return response.contents().stream()
                .map(S3Object::key) 
                .collect(Collectors.toList());
    }
}

