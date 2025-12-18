package io.github._3xhaust.root_server.domain.image.controller;

import io.github._3xhaust.root_server.domain.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import io.github._3xhaust.root_server.domain.image.dto.ImageUploadResponse;
import io.github._3xhaust.root_server.global.common.ApiResponse;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Tag(name = "Image", description = "이미지 업로드 및 조회 API")
public class ImageController {
    private final ImageService imageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImageUploadResponse> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        ImageUploadResponse response = imageService.saveImage(file);
        return ApiResponse.ok(HttpStatus.CREATED, response);
    }

    @PostMapping("/upload/base64")
    public ApiResponse<ImageUploadResponse> uploadBase64(@RequestParam("base64") String base64) throws IOException {
        ImageUploadResponse response = imageService.saveBase64Image(base64);
        return ApiResponse.ok(HttpStatus.CREATED, response);
    }

    @GetMapping("/{filename}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) throws IOException {
        byte[] image = imageService.loadImage(filename);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(image, headers, HttpStatus.OK);
    }
}
