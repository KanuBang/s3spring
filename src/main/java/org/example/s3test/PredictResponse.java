package org.example.s3test;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PredictResponse {
    private String prediction;
    private double confidence;
}
