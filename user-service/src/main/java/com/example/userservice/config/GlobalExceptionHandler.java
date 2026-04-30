package com.example.userservice.config;

import com.example.userservice.exceptions.ResourceNotFoundException;
import com.example.userservice.model.dto.ErrorResponse;
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

    //NOTE : MethodArgumentNotValidException (for @Valid @RequestBody)
    //Eg. /addEmployee, name must not be blank, salary must be greater than 0
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

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleGenericErrors(Exception e){
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                List.of("Caught unexpected error : "+e.getMessage()),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }



}
