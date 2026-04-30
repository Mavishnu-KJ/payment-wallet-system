package com.example.walletservice.config;

import com.example.walletservice.exceptions.InsufficientBalanceException;
import com.example.walletservice.exceptions.ResourceNotFoundException;
import com.example.walletservice.model.dto.ErrorResponse;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @ExceptionHandler(InsufficientBalanceException.class)
    ResponseEntity<ErrorResponse> handleInsufficientBalanceException(InsufficientBalanceException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Insufficient Balance",
                List.of(e.getMessage()),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    //Validation Errors - @Valid @RequestBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){

        List<String> errorList = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() +" : "+err.getDefaultMessage())
                .toList();

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                errorList,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    //For @Valid on PathVariable / RequestParam
    //eg. @Positive(message = "minSalary must be greater than 0") @RequestParam (name="minSalary", required = false) int salary falls in global error
    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(HandlerMethodValidationException e){

        List<String> errorList = new ArrayList<>();

        e.getAllErrors().forEach(err->{
            if(err instanceof FieldError fieldError){
                errorList.add(fieldError.getField() + " : " +fieldError.getDefaultMessage());
            }else{
                //Rare global error
                errorList.add("global : "+err.getDefaultMessage());
            }
        });

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Handler Method Validation Failed",
                errorList,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    //eg. GetMapping("/api/employees/{id}", /api/employees/abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e){

        List<String> errorList = List.of("Invalid parameter : "+e.getName()+" Expected type "+e.getRequiredType().getSimpleName() +" but, got "+e.getValue());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Type mismatch",
                errorList,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

    }

    //For other unhandled exceptions
    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleGenericErrors(Exception e){
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL SERVER ERROR",
                List.of("Unexpected error : "+e.getMessage()),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
