package com.tradepost.procure.common;

import jakarta.validation.ConstraintViolationException;
import java.io.FileNotFoundException;
import java.nio.file.NoSuchFileException;
import java.util.Map;
import javax.naming.NamingException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.expression.ExpressionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.xml.sax.SAXException;
import org.yaml.snakeyaml.error.YAMLException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({NotFoundException.class, EmptyResultDataAccessException.class})
    public ResponseEntity<Map<String, String>> notFound(Exception ex) {
        return reply(HttpStatus.NOT_FOUND, "resource not found");
    }

    @ExceptionHandler({NoSuchFileException.class, FileNotFoundException.class})
    public ResponseEntity<Map<String, String>> missingFile(Exception ex) {
        return reply(HttpStatus.NOT_FOUND, "file not found");
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> conflict(ConflictException ex) {
        return reply(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({InvalidRequestException.class, IllegalArgumentException.class, SAXException.class,
            ExpressionException.class, YAMLException.class, HandlerMethodValidationException.class,
            ConstraintViolationException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<Map<String, String>> invalid(Exception ex) {
        return reply(HttpStatus.BAD_REQUEST, "request rejected");
    }

    @ExceptionHandler({RestClientException.class, NamingException.class, ToolUnavailableException.class})
    public ResponseEntity<Map<String, String>> upstream(Exception ex) {
        return reply(HttpStatus.BAD_GATEWAY, "upstream system unavailable");
    }

    private ResponseEntity<Map<String, String>> reply(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }
}
