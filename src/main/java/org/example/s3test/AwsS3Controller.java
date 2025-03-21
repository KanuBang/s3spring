package org.example.s3test;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class AwsS3Controller {

    private final AwsS3Service awsS3Service;

    @PostMapping("/image")
    public ResponseEntity<PredictResponse> uploadFile(@RequestParam(value = "file")MultipartFile file) throws IOException {
        PredictResponse predictResponse = awsS3Service.uploadFile(file, 1L);
        return ResponseEntity.ok(predictResponse);
    }
}
