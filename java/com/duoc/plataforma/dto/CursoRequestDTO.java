package com.duoc.plataforma.dto;

import lombok.Data;
import java.util.List;

// DTO para crear un curso
@Data
public class CursoRequestDTO {
    private String nombre;
    private String instructor;
    private Integer duracionHoras;
    private Double costo;
}
