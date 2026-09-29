package ar.edu.siglo21.tribu.dto;

import java.util.Set;

public record AvanceCurso(int leccionesTotales, int leccionesHechas, Set<Long> idsLeccionesHechas) {
    public int porcentaje() {
        if (leccionesTotales == 0) {
            return 0;
        }
        return Math.round((leccionesHechas * 100f) / leccionesTotales);
    }

    public boolean completo() {
        return leccionesTotales > 0 && leccionesHechas >= leccionesTotales;
    }
}
