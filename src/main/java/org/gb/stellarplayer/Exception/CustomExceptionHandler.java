package org.gb.stellarplayer.Exception;

import org.gb.stellarplayer.Response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class CustomExceptionHandler  {
    // xu ly BadRequestException
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handlerBadRequestException(BadRequestException ex) {
        // Log err
        return new ResponseEntity<> (new ErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage()),HttpStatus.BAD_REQUEST);
    }
    // xu ly ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlerNotFoundException(ResourceNotFoundException ex) {
        // Log err
        return new ResponseEntity<> (new ErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage()),HttpStatus.NOT_FOUND);
    }
    //xu ly cac truong hop con lai
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handlerException(Exception ex) {
        // Log err
        return new ResponseEntity<> (new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handlerMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> error = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((fieldError) -> {
            String fieldName = fieldError.getObjectName();
            String message = fieldError.getDefaultMessage();
            error.put(fieldName, message);

        });
        return null;
    }
}
