package com.duoc.plataforma.controller;

import com.duoc.plataforma.dto.GuiaRequestDTO;
import com.duoc.plataforma.dto.GuiaResponseDTO;
import com.duoc.plataforma.service.GuiaDespachoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/guias")
@RequiredArgsConstructor
public class GuiaDespachoController {

    private final GuiaDespachoService service;

    @PostMapping
    public ResponseEntity<GuiaResponseDTO> crearGuia(@RequestBody GuiaRequestDTO dto) {
        return ResponseEntity.ok(service.crearGuia(dto));
    }

    @PostMapping("/{id}/subir")
    public ResponseEntity<GuiaResponseDTO> subirGuia(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.subirGuiaS3(id, file));
    }

    @GetMapping("/{id}/descargar")
    public ResponseEntity<byte[]> descargarGuia(@PathVariable Long id) {
        byte[] contenido = service.descargarGuia(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"guia_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(contenido);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GuiaResponseDTO> modificarGuia(
            @PathVariable Long id,
            @RequestBody GuiaRequestDTO dto) {
        return ResponseEntity.ok(service.modificarGuia(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarGuia(@PathVariable Long id) {
        service.eliminarGuia(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<GuiaResponseDTO>> consultarGuias(
            @RequestParam(required = false) String transportista,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(service.consultarGuias(transportista, fecha));
    }
}
