package com.example.transactionservice.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Resource Conflict")
public class ResourceConflictException extends RuntimeException{

    public ResourceConflictException(){
        super();
    }

    public ResourceConflictException(String message){
        super(message);
    }

}
