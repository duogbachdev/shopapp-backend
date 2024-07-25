package com.project.shopapp.controller;

import com.project.shopapp.dto.ProductDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/products")
public class ProductController {
    @GetMapping("")
    public ResponseEntity<String> getAllProducts(
            @RequestParam("page") int page,
            @RequestParam("limit") int limit
    ) {
        return ResponseEntity.ok("All products");
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getProductById(@PathVariable("id") String id) {
        return ResponseEntity.ok("Product with id " + id);
    }

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(
            @Valid @ModelAttribute ProductDTO productDTO,
            BindingResult result
    ) {
        try {
            if (result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessages);
            }

            List<MultipartFile> files = productDTO.getFiles();
            files = files == null ? new ArrayList<MultipartFile>() : files;
            for (MultipartFile file : files) {
                    // Kiểm tra kích thước file và định dạng
                    if (file.getSize() > 10 * 1024 * 1024) { // >10MB
                        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("File is too big! Maximum size is 10MB");
                    }
                    String contentType = file.getContentType();
                    if (contentType == null || !contentType.startsWith("image/")) {
                        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("File must be an image");
                    }
                    // Lưu file và cập nhật thumbnail trong DTO
                    String fileName = storeFile(file);
                    //productDTO.setThumbnail(fileName);
                    // Lưu vào đối tượng product trong Database (bạn cần thêm code lưu vào DB ở đây)
                    // Lưu vào bảng product_images
            }
            return ResponseEntity.ok("Product created");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private String storeFile(MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        // Thêm UUID vào trước tên file để đảm bảo tên file duy nhất
        String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
        // Đường dẫn đến thư mục chứa file
        Path uploadDir = Paths.get("uploads");
        // Kiểm tra thư mục có tồn tại hay chưa
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        // Đường dẫn đầy đủ tên file
        Path destination = Paths.get(uploadDir.toString(), uniqueFileName);
        // Sao chép file vào thư mục đích
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFileName;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable("id") long id) {
        return ResponseEntity.status(HttpStatus.OK).body("Product deleted with id " + id);
    }
}
