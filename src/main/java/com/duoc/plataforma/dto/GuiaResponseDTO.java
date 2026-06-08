package com.duoc.plataforma.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class GuiaResponseDTO {
    private Long id;
    private String transportista;
    private LocalDate fecha;
    private String nombreArchivo;
    private String rutaS3;
    private String rutaEfs;
    private String estado;
}
