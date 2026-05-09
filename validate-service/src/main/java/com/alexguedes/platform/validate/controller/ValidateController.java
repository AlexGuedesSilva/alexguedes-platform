package com.alexguedes.platform.validate.controller;

import com.alexguedes.platform.shared.dto.ApiResponse;
import com.alexguedes.platform.shared.security.SignatureRequest;
import com.alexguedes.platform.validate.service.ValidationResult;
import com.alexguedes.platform.validate.service.ValidationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/validate")
public class ValidateController {
    private final ValidationService validationService;

    public ValidateController(ValidationService validationService) {
        this.validationService = validationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ValidationResult>> validate(@Valid @RequestBody SignatureRequest request) {
        ValidationResult result = validationService.validate(request);
        HttpStatus status = result.valid() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status).body(new ApiResponse<>(result.valid(), result.message(), result, java.time.Instant.now()));
    }
}
