package com.mendes.scheduling_platform.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.net.URI;

@Service
public class StorageService {

    private final S3Client client;
    private final String bucket;
    private final String publicBaseUrl;

    public StorageService(
            @Value("${app.storage.endpoint:}") String endpoint,
            @Value("${app.storage.region:auto}") String region,
            @Value("${app.storage.bucket}") String bucket,
            @Value("${app.storage.access-key}") String accessKey,
            @Value("${app.storage.secret-key}") String secretKey,
            @Value("${app.storage.public-base-url}") String publicBaseUrl
    ) {
        this.bucket = bucket;
        this.publicBaseUrl = publicBaseUrl.endsWith("/")
                ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
                : publicBaseUrl;

        software.amazon.awssdk.services.s3.S3ClientBuilder builder = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of(region == null || region.isBlank() ? "auto" : region));

        if (endpoint != null && !endpoint.isBlank()) {
            builder = builder.endpointOverride(URI.create(endpoint)).forcePathStyle(true);
        }

        this.client = builder.build();
    }

    /**
     * Envia o arquivo para o bucket e retorna a URL pública final.
     * @param key caminho/nome do arquivo dentro do bucket (ex.: "public-pages/12/uuid-foto.jpg")
     */
    public String upload(String key, InputStream data, long contentLength, String contentType) {
        client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromInputStream(data, contentLength)
        );
        return publicBaseUrl + "/" + key;
    }
}