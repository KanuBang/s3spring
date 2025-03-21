package org.example.s3test;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class AwsS3Service {

    @Value("${spring.cloud.aws.s3.bucketName}")
    private String bucket;

    private final AmazonS3 amazonS3;


    // 단일 파일 업로드
    public PredictResponse uploadFile(MultipartFile multipartFile, Long userId) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }

        String fileName = createFileName(multipartFile.getOriginalFilename(), userId);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(multipartFile.getSize());
        objectMetadata.setContentType(multipartFile.getContentType());

        // S3 업로드
        amazonS3.putObject(
                new PutObjectRequest(bucket, fileName, multipartFile.getInputStream(), objectMetadata)
        );

        // URL 가져오기
        String generatedImageUrl = amazonS3.getUrl(bucket, fileName).toString();

        // fast_api로 전송

        RestTemplate restTemplate = new RestTemplate();

        String fastApiUrl = "http://43.201.65.255/predict"; // "http://<EC2-퍼블릭-IP>/predict"

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("image_url", generatedImageUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<PredictResponse> response = restTemplate.exchange(fastApiUrl, HttpMethod.POST, requestEntity, PredictResponse.class);
        System.out.println(response.getBody());

        if (response.getStatusCode() == HttpStatus.TEMPORARY_REDIRECT) {
            String newUrl = response.getHeaders().getLocation().toString(); // 새로운 URL 가져오기
            response = restTemplate.exchange(newUrl, HttpMethod.POST, requestEntity, PredictResponse.class);
        }

        PredictResponse body = response.getBody();
        return body;
    }


    // 파일명 생성
    public String createFileName(String fileName, Long userId) {
        String ext = getFileExtension(fileName);
        String newFilename = userId + "submitted-ai-image" + String.valueOf(LocalDateTime.now());
        newFilename = fileName.replace(" ", "-")
                .replace(":", "-")
                .replace(".", "-").concat(ext);
        return newFilename;
    }

    // 확장자를 얻는다
    public String getFileExtension(String fileName) {
        try {
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e) {
            throw e;
        }

    }
}
