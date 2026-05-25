package com.duoc.plataforma.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "DETALLE_INSCRIPCION")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleInscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DETALLE")
    private Long idDetalle;

    @ManyToOne
    @JoinColumn(name = "ID_INSCRIPCION", nullable = false)
    private Inscripcion inscripcion;

    @ManyToOne
    @JoinColumn(name = "ID_CURSO", nullable = false)
    private Curso curso;

    @Column(name = "COSTO_CURSO", nullable = false)
    private Double costoCurso;
}
