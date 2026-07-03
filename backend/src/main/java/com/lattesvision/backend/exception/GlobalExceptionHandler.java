package com.lattesvision.backend.exception;

import com.lattesvision.backend.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String MENSAGEM_ERRO_INTERNO = "Erro interno ao processar a requisicao.";

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        int statusCode = ex.getStatusCode().value();
        HttpStatus status = HttpStatus.resolve(statusCode);
        String reasonPhrase = status != null ? status.getReasonPhrase() : "HTTP " + statusCode;
        String code = status != null ? status.name() : "HTTP_" + statusCode;
        String message = ex.getReason() != null ? ex.getReason() : reasonPhrase;

        ApiErrorResponse body = buildBody(statusCode, reasonPhrase, code, message, request.getRequestURI());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Erro nao tratado na API", ex);

        ApiErrorResponse body = buildBody(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                MENSAGEM_ERRO_INTERNO,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private ApiErrorResponse buildBody(int status, String error, String code, String message, String path) {
        return new ApiErrorResponse(
                Instant.now().toString(),
                status,
                error,
                code,
                message,
                path
        );
    }
}
