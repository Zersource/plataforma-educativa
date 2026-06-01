package com.duoc.plataforma.controller;

import com.duoc.plataforma.dto.InscripcionRequestDTO;
import com.duoc.plataforma.dto.InscripcionResponseDTO;
import com.duoc.plataforma.service.InscripcionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/inscripciones")
public class InscripcionController {

    @Autowired
    private InscripcionService inscripcionService;

    // =========================================================
    // SEMANA 1 - Inscribir estudiante en cursos
    // POST /api/inscripciones
    // =========================================================
    @PostMapping
    public ResponseEntity<InscripcionResponseDTO> inscribir(@RequestBody InscripcionRequestDTO dto) {
        InscripcionResponseDTO response = inscripcionService.inscribir(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =========================================================
    // SEMANA 2 - ENDPOINT 1
    // Generar archivo resumen descargable (archivo físico local)
    // POST /api/inscripciones/{idInscripcion}/generar-resumen
    // =========================================================
    @PostMapping("/{idInscripcion}/generar-resumen")
    public ResponseEntity<?> generarResumen(@PathVariable Long idInscripcion) {
        try {
            log.info("Generando archivo resumen para inscripción N°{}", idInscripcion);
            String rutaArchivo   = inscripcionService.generarArchivoResumen(idInscripcion);
            String nombreArchivo = "resumen_" + idInscripcion + ".txt";
            byte[] contenido     = Files.readAllBytes(Paths.get(rutaArchivo));

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .contentLength(contenido.length)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + nombreArchivo + "\"")
                    .body(new ByteArrayResource(contenido));

        } catch (IOException e) {
            log.error("Error generando resumen: {}", e.getMessage());
            return error("Error al generar el archivo: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // =========================================================
    // SEMANA 2 - ENDPOINT 2
    // Subir resumen a S3 (genera + sube automáticamente)
    // POST /api/inscripciones/{idInscripcion}/subir-resumen
    // Ruta en S3: bucket/{idInscripcion}/resumen_{idInscripcion}.txt
    // =========================================================
    @PostMapping("/{idInscripcion}/subir-resumen")
    public ResponseEntity<?> subirResumenAws(@PathVariable Long idInscripcion) {
        try {
            log.info("Subiendo resumen N°{} a AWS S3", idInscripcion);
            String url = inscripcionService.subirResumenAws(idInscripcion);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje",        "Resumen subido exitosamente a S3");
            response.put("idInscripcion",  idInscripcion);
            response.put("carpetaS3",      idInscripcion + "/");
            response.put("archivoS3",      "resumen_" + idInscripcion + ".txt");
            response.put("url",            url);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Error subiendo resumen a S3: {}", e.getMessage());
            return error("Error al subir a S3: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // =========================================================
    // SEMANA 2 - ENDPOINT 3
    // Actualizar/reemplazar archivo en S3
    // PUT /api/inscripciones/{idInscripcion}/actualizar-resumen
    // Body: form-data con campo "file"
    // =========================================================
    @PutMapping("/{idInscripcion}/actualizar-resumen")
    public ResponseEntity<?> actualizarResumenAws(
            @PathVariable Long idInscripcion,
            @RequestParam("file") MultipartFile file) {
        try {
            log.info("Actualizando archivo en S3, inscripción N°{}", idInscripcion);
            String url = inscripcionService.actualizarResumenAws(idInscripcion, file);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje",       "Archivo actualizado exitosamente en S3");
            response.put("idInscripcion", idInscripcion);
            response.put("archivoS3",     file.getOriginalFilename());
            response.put("url",           url);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Error actualizando archivo en S3: {}", e.getMessage());
            return error("Error al actualizar: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // =========================================================
    // SEMANA 2 - ENDPOINT 4
    // Descargar archivo desde S3
    // GET /api/inscripciones/{idInscripcion}/descargar-resumen
    // =========================================================
    @GetMapping("/{idInscripcion}/descargar-resumen")
    public ResponseEntity<?> descargarResumenAws(@PathVariable Long idInscripcion) {
        try {
            log.info("Descargando resumen N°{} desde S3", idInscripcion);
            String nombreArchivo = "resumen_" + idInscripcion + ".txt";
            byte[] contenido     = inscripcionService.descargarResumenAws(idInscripcion);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(contenido.length)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + nombreArchivo + "\"")
                    .body(new ByteArrayResource(contenido));

        } catch (RuntimeException e) {
            log.error("Archivo no encontrado en S3: {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            log.error("Error descargando de S3: {}", e.getMessage());
            return error("Error al descargar: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // =========================================================
    // SEMANA 2 - ENDPOINT 5
    // Eliminar archivo de S3
    // DELETE /api/inscripciones/{idInscripcion}/eliminar-resumen
    // =========================================================
    @DeleteMapping("/{idInscripcion}/eliminar-resumen")
    public ResponseEntity<?> eliminarResumenAws(@PathVariable Long idInscripcion) {
        try {
            log.info("Eliminando resumen N°{} de S3", idInscripcion);
            inscripcionService.eliminarResumenAws(idInscripcion);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje",          "Archivo eliminado exitosamente de S3");
            response.put("idInscripcion",    idInscripcion);
            response.put("archivoEliminado", "resumen_" + idInscripcion + ".txt");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error eliminando de S3: {}", e.getMessage());
            return error(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Helper para respuestas de error
    private ResponseEntity<Map<String, String>> error(String mensaje, HttpStatus status) {
        Map<String, String> err = new HashMap<>();
        err.put("error", mensaje);
        return ResponseEntity.status(status).body(err);
    }
}
