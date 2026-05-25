package com.duoc.plataforma.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InscripcionResponseDTO {
    private Long idInscripcion;
    private String nombreEstudiante;
    private String emailEstudiante;
    private LocalDate fechaInscripcion;
    private List<CursoDetalleDTO> cursosInscritos;
    private Double totalPagar;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CursoDetalleDTO {
        private Long idCurso;
        private String nombreCurso;
        private String instructor;
        private Integer duracionHoras;
        private Double costo;
    }
}
