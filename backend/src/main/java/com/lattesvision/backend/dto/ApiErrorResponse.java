package com.lattesvision.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload padronizado para respostas de erro da API")
public class ApiErrorResponse {

    @Schema(description = "Data/hora do erro em formato ISO-8601 UTC", example = "2026-04-27T15:12:30.123Z")
    private String timestamp;

    @Schema(description = "Codigo HTTP numerico", example = "400")
    private int status;

    @Schema(description = "Descricao curta do status HTTP", example = "Bad Request")
    private String error;

    @Schema(description = "Codigo textual para tratamento no cliente", example = "BAD_REQUEST")
    private String code;

    @Schema(description = "Mensagem de erro para consumo da API", example = "ID Lattes invalido. Use exatamente 16 digitos numericos.")
    private String message;

    @Schema(description = "Endpoint que gerou o erro", example = "/api/lattes/123")
    private String path;

    public ApiErrorResponse() {
    }

    public ApiErrorResponse(String timestamp, int status, String error, String code, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.code = code;
        this.message = message;
        this.path = path;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
