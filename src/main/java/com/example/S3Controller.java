package com.example;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
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

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Controller("/s3")
public class S3Controller {

    @Inject
    @Singleton // Ensure only one instance exists
    private S3Client s3Client;


    private static final String BUCKET_NAME = "micronaut-s3";

    @Post(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA)
    public String uploadFile(CompletedFileUpload file) {
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
            return "Error uploading file: " + e.getMessage();
        }
    }

    @Get("/list")
    public List<String> listFiles() {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(BUCKET_NAME)
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);

        return response.contents().stream()
                .map(S3Object::key) // Get object names (filenames)
                .collect(Collectors.toList());
    }
}

