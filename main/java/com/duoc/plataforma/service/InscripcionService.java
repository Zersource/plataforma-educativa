package com.duoc.plataforma.service;

import com.duoc.plataforma.dto.InscripcionRequestDTO;
import com.duoc.plataforma.dto.InscripcionResponseDTO;
import com.duoc.plataforma.model.*;
import com.duoc.plataforma.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InscripcionService {

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Transactional
    public InscripcionResponseDTO inscribir(InscripcionRequestDTO dto) {

        // Buscar estudiante
        Estudiante estudiante = estudianteRepository.findById(dto.getIdEstudiante())
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con ID: " + dto.getIdEstudiante()));

        // Buscar cursos
        List<Curso> cursos = new ArrayList<>();
        for (Long idCurso : dto.getIdCursos()) {
            Curso curso = cursoRepository.findById(idCurso)
                    .orElseThrow(() -> new RuntimeException("Curso no encontrado con ID: " + idCurso));
            cursos.add(curso);
        }

        // Calcular total
        Double total = cursos.stream().mapToDouble(Curso::getCosto).sum();

        // Crear inscripcion
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setEstudiante(estudiante);
        inscripcion.setFechaInscripcion(LocalDate.now());
        inscripcion.setTotalPagar(total);

        // Crear detalles
        List<DetalleInscripcion> detalles = cursos.stream().map(curso -> {
            DetalleInscripcion detalle = new DetalleInscripcion();
            detalle.setInscripcion(inscripcion);
            detalle.setCurso(curso);
            detalle.setCostoCurso(curso.getCosto());
            return detalle;
        }).collect(Collectors.toList());

        inscripcion.setDetalles(detalles);
        Inscripcion guardada = inscripcionRepository.save(inscripcion);

        // Armar respuesta
        return buildResponse(guardada, estudiante, cursos, total);
    }

    private InscripcionResponseDTO buildResponse(Inscripcion inscripcion, Estudiante estudiante,
                                                  List<Curso> cursos, Double total) {
        List<InscripcionResponseDTO.CursoDetalleDTO> cursosDTO = cursos.stream()
                .map(c -> new InscripcionResponseDTO.CursoDetalleDTO(
                        c.getIdCurso(),
                        c.getNombre(),
                        c.getInstructor(),
                        c.getDuracionHoras(),
                        c.getCosto()
                )).collect(Collectors.toList());

        return new InscripcionResponseDTO(
                inscripcion.getIdInscripcion(),
                estudiante.getNombre(),
                estudiante.getEmail(),
                inscripcion.getFechaInscripcion(),
                cursosDTO,
                total
        );
    }
}
