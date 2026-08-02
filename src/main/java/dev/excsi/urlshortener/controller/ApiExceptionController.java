package dev.excsi.urlshortener.controller;

import dev.excsi.urlshortener.dto.ErrorResponse;
import dev.excsi.urlshortener.exception.UrlNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionController {

    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(UrlNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("URL_NOT_FOUND", String.format("Short url %s doesn't map to anything", exception.getShortUrl())));
    }

}
