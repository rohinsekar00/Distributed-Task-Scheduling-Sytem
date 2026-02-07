package com.faang.taskscheduler.executor.impl;

import com.faang.taskscheduler.executor.TaskExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Downloads image from URL and converts to base64.
 * First task in image processing pipeline.
 */
@Component
@Slf4j
public class ImageDownloadExecutor implements TaskExecutor {
    
    @Override
    public Map<String, Object> execute(Map<String, Object> parameters) throws Exception {
        String imageUrl = (String) parameters.get("imageUrl");
        
        log.info("Downloading image from: {}", imageUrl);
        
        // Simulate network delay
        Thread.sleep(500);
        
        // Download image
        URL url = new URL(imageUrl);
        BufferedImage image = ImageIO.read(url);
        
        if (image == null) {
            throw new IllegalArgumentException("Invalid image URL or unsupported format");
        }
        
        // Convert to base64 for passing to next task
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
        
        Map<String, Object> result = new HashMap<>();
        result.put("imageData", base64Image);
        result.put("width", image.getWidth());
        result.put("height", image.getHeight());
        result.put("format", "jpg");
        result.put("downloadedFrom", imageUrl);
        
        log.info("Image downloaded successfully: {}x{}", image.getWidth(), image.getHeight());
        
        return result;
    }
    
    @Override
    public String getTaskType() {
        return "IMAGE_DOWNLOAD";
    }
    
    @Override
    public void validateParameters(Map<String, Object> parameters) {
        if (!parameters.containsKey("imageUrl")) {
            throw new IllegalArgumentException("imageUrl parameter is required");
        }
    }
    
    @Override
    public long estimateExecutionTime(Map<String, Object> parameters) {
        return 2000; // 2 seconds
    }
}
