package com.duoc.plataforma.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class GuiaRequestDTO {
    private String transportista;
    private LocalDate fecha;
    private String nombreArchivo;
}
