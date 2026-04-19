package com.example.transactionservice.config;

import com.example.transactionservice.exceptions.ResourceNotFoundException;
import com.example.transactionservice.model.dto.ErrorResponse;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException e){
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Resource Not Found",
                List.of(e.getMessage()),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    //Returns the exact error body sent by client service
    //Return feign errorMessage if the error body from client service is empty
    @ExceptionHandler(FeignException.class)
    ResponseEntity<String> handleFeignException(FeignException e){
        String error = e.contentUTF8() != null && !e.contentUTF8().isBlank() ? e.contentUTF8() : e.getMessage();
        int statusCode = e.status();

        return new ResponseEntity<>(error, HttpStatus.valueOf(statusCode));
    }

}
