package com.lattesvision.backend.dto;

public record BookChapterDetailDTO(Long id, Integer year, String title, String authors, String publisher, String isbn, String edition, String volume, String pages, String rawDescription, String bookTitle, String organizers) {
}
