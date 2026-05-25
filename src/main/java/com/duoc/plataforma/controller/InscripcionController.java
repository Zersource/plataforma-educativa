package com.duoc.plataforma.controller;

import com.duoc.plataforma.dto.InscripcionRequestDTO;
import com.duoc.plataforma.dto.InscripcionResponseDTO;
import com.duoc.plataforma.service.InscripcionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inscripciones")
public class InscripcionController {

    @Autowired
    private InscripcionService inscripcionService;

    // POST /api/inscripciones - Inscribir estudiante en cursos
    @PostMapping
    public ResponseEntity<InscripcionResponseDTO> inscribir(@RequestBody InscripcionRequestDTO dto) {
        InscripcionResponseDTO response = inscripcionService.inscribir(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
