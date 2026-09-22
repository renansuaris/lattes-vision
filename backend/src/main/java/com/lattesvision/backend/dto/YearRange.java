package com.lattesvision.backend.dto;

/**
 * Recorte temporal aplicado somente às consultas do dashboard.
 * A coleta e a persistência do currículo permanecem independentes deste filtro.
 */
public record YearRange(Integer startYear, Integer endYear) {

    public boolean isActive() {
        return startYear != null || endYear != null;
    }

    public boolean includes(Integer year) {
        if (!isActive()) {
            return true;
        }
        if (year == null) {
            return false;
        }
        return (startYear == null || year >= startYear)
                && (endYear == null || year <= endYear);
    }
}
