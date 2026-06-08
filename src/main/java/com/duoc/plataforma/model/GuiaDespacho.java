package com.duoc.plataforma.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "guias_despacho")
@Data
public class GuiaDespacho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String transportista;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private String nombreArchivo;

    @Column
    private String rutaS3;

    @Column
    private String rutaEfs;

    @Column
    private String estado;
}
