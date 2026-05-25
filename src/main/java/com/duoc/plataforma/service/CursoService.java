package com.duoc.plataforma.service;

import com.duoc.plataforma.dto.CursoRequestDTO;
import com.duoc.plataforma.model.Curso;
import com.duoc.plataforma.repository.CursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CursoService {

    @Autowired
    private CursoRepository cursoRepository;

    // Listar todos los cursos disponibles
    public List<Curso> listarCursos() {
        return cursoRepository.findAll();
    }

    // Agregar un nuevo curso
    public Curso agregarCurso(CursoRequestDTO dto) {
        Curso curso = new Curso();
        curso.setNombre(dto.getNombre());
        curso.setInstructor(dto.getInstructor());
        curso.setDuracionHoras(dto.getDuracionHoras());
        curso.setCosto(dto.getCosto());
        return cursoRepository.save(curso);
    }

    // Buscar curso por ID
    public Curso buscarPorId(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado con ID: " + id));
    }
}
