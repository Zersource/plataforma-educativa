package com.duoc.plataforma.controller;

import com.duoc.plataforma.dto.CursoRequestDTO;
import com.duoc.plataforma.model.Curso;
import com.duoc.plataforma.service.CursoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    @Autowired
    private CursoService cursoService;

    // GET /api/cursos - Listar todos los cursos disponibles
    @GetMapping
    public ResponseEntity<List<Curso>> listarCursos() {
        List<Curso> cursos = cursoService.listarCursos();
        return ResponseEntity.ok(cursos);
    }

    // POST /api/cursos - Agregar un nuevo curso
    @PostMapping
    public ResponseEntity<Curso> agregarCurso(@RequestBody CursoRequestDTO dto) {
        Curso nuevo = cursoService.agregarCurso(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }
}
