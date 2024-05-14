package com.example.projectthree;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.List;

public class ImageClassification extends flower {
    private ImageLabeler imageLabeler;
    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        imageLabeler = ImageLabeling.getClient(new ImageLabelerOptions.Builder().setConfidenceThreshold(0.7f).build());
    }
    @Override
    protected void runClassification(Bitmap bitmap){
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
        imageLabeler.process(inputImage)
                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    @Override
                    public void onSuccess(List<ImageLabel> labels) {
                        if (!labels.isEmpty()) {
                            StringBuilder builder = new StringBuilder();
                            for (ImageLabel label : labels) {
                                String labelText = label.getText();
                                float confidence = label.getConfidence();
                                builder.append("Label: ").append(labelText).append(", Confidence: ").append(confidence).append("\n");
                            }
                            getOutputTextView().setText(builder.toString());
                        } else {
                            getOutputTextView().setText("No labels found");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("ImageLabel", "Labeling failed", e);
                        getOutputTextView().setText("Labeling failed: " + e.getMessage());
                    }
                });

    }
}
