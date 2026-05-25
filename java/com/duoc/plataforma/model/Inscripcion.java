package com.duoc.plataforma.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "INSCRIPCIONES")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_INSCRIPCION")
    private Long idInscripcion;

    @ManyToOne
    @JoinColumn(name = "ID_ESTUDIANTE", nullable = false)
    private Estudiante estudiante;

    @Column(name = "FECHA_INSCRIPCION")
    private LocalDate fechaInscripcion;

    @Column(name = "TOTAL_PAGAR", nullable = false)
    private Double totalPagar;

    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<DetalleInscripcion> detalles;

    @PrePersist
    public void prePersist() {
        if (fechaInscripcion == null) {
            fechaInscripcion = LocalDate.now();
        }
    }
}
