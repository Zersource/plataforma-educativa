package com.duoc.plataforma.dto;

import lombok.Data;
import java.util.List;

@Data
public class InscripcionRequestDTO {
    private Long idEstudiante;
    private List<Long> idCursos;
}
