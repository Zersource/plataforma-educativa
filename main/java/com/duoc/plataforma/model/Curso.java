package com.duoc.plataforma.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "CURSOS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CURSO")
    private Long idCurso;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;

    @Column(name = "INSTRUCTOR", nullable = false, length = 100)
    private String instructor;

    @Column(name = "DURACION_HORAS", nullable = false)
    private Integer duracionHoras;

    @Column(name = "COSTO", nullable = false)
    private Double costo;
}
