package com.duoc.plataforma.service;

import com.duoc.plataforma.dto.InscripcionRequestDTO;
import com.duoc.plataforma.dto.InscripcionResponseDTO;
import com.duoc.plataforma.model.*;
import com.duoc.plataforma.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InscripcionService {

    // ---- Repositorios Semana 1 ----
    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private CursoRepository cursoRepository;

    // ---- S3 Semana 2 ----
    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${app.files.temp-dir:temp-files}")
    private String tempDir;

    // =========================================================
    // SEMANA 1 - Inscribir estudiante
    // =========================================================
    @Transactional
    public InscripcionResponseDTO inscribir(InscripcionRequestDTO dto) {

        Estudiante estudiante = estudianteRepository.findById(dto.getIdEstudiante())
                .orElseThrow(() -> new RuntimeException(
                        "Estudiante no encontrado con ID: " + dto.getIdEstudiante()));

        List<Curso> cursos = new ArrayList<>();
        for (Long idCurso : dto.getIdCursos()) {
            Curso curso = cursoRepository.findById(idCurso)
                    .orElseThrow(() -> new RuntimeException(
                            "Curso no encontrado con ID: " + idCurso));
            cursos.add(curso);
        }

        Double total = cursos.stream().mapToDouble(Curso::getCosto).sum();

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setEstudiante(estudiante);
        inscripcion.setFechaInscripcion(LocalDate.now());
        inscripcion.setTotalPagar(total);

        List<DetalleInscripcion> detalles = cursos.stream().map(curso -> {
            DetalleInscripcion detalle = new DetalleInscripcion();
            detalle.setInscripcion(inscripcion);
            detalle.setCurso(curso);
            detalle.setCostoCurso(curso.getCosto());
            return detalle;
        }).collect(Collectors.toList());

        inscripcion.setDetalles(detalles);
        Inscripcion guardada = inscripcionRepository.save(inscripcion);

        return buildResponse(guardada, estudiante, cursos, total);
    }

    private InscripcionResponseDTO buildResponse(Inscripcion inscripcion, Estudiante estudiante,
                                                  List<Curso> cursos, Double total) {
        List<InscripcionResponseDTO.CursoDetalleDTO> cursosDTO = cursos.stream()
                .map(c -> new InscripcionResponseDTO.CursoDetalleDTO(
                        c.getIdCurso(), c.getNombre(), c.getInstructor(),
                        c.getDuracionHoras(), c.getCosto()))
                .collect(Collectors.toList());

        return new InscripcionResponseDTO(
                inscripcion.getIdInscripcion(),
                estudiante.getNombre(),
                estudiante.getEmail(),
                inscripcion.getFechaInscripcion(),
                cursosDTO,
                total
        );
    }

    // =========================================================
    // SEMANA 2 - Generar archivo .txt del resumen en disco local
    // =========================================================
    public String generarArchivoResumen(Long idInscripcion) throws IOException {

        // Buscar inscripción con todos sus datos
        Inscripcion inscripcion = inscripcionRepository.findById(idInscripcion)
                .orElseThrow(() -> new RuntimeException(
                        "Inscripción no encontrada con ID: " + idInscripcion));

        Estudiante estudiante = inscripcion.getEstudiante();
        List<DetalleInscripcion> detalles = inscripcion.getDetalles();

        // Crear directorio temporal si no existe
        File directorio = new File(tempDir);
        if (!directorio.exists()) directorio.mkdirs();

        String nombreArchivo = "resumen_" + idInscripcion + ".txt";
        String rutaArchivo   = tempDir + File.separator + nombreArchivo;

        log.info("Generando archivo resumen en: {}", rutaArchivo);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(rutaArchivo))) {
            writer.write("========================================");
            writer.newLine();
            writer.write("  RESUMEN DE INSCRIPCION");
            writer.newLine();
            writer.write("  PLATAFORMA EDUCATIVA - CDY2204");
            writer.newLine();
            writer.write("========================================");
            writer.newLine();
            writer.write("N° Inscripcion     : " + inscripcion.getIdInscripcion());
            writer.newLine();
            writer.write("Fecha Inscripcion  : " +
                    inscripcion.getFechaInscripcion()
                               .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            writer.newLine();
            writer.write("----------------------------------------");
            writer.newLine();
            writer.write("DATOS DEL ESTUDIANTE:");
            writer.newLine();
            writer.write("  Nombre : " + estudiante.getNombre());
            writer.newLine();
            writer.write("  Email  : " + estudiante.getEmail());
            writer.newLine();
            writer.write("----------------------------------------");
            writer.newLine();
            writer.write("CURSOS INSCRITOS:");
            writer.newLine();
            for (int i = 0; i < detalles.size(); i++) {
                Curso c = detalles.get(i).getCurso();
                writer.write("  " + (i + 1) + ". " + c.getNombre());
                writer.newLine();
                writer.write("     Instructor : " + c.getInstructor());
                writer.newLine();
                writer.write("     Duracion   : " + c.getDuracionHoras() + " horas");
                writer.newLine();
                writer.write("     Costo      : $" + String.format("%,.0f", c.getCosto()));
                writer.newLine();
            }
            writer.write("----------------------------------------");
            writer.newLine();
            writer.write("TOTAL A PAGAR : $" + String.format("%,.0f", inscripcion.getTotalPagar()));
            writer.newLine();
            writer.write("========================================");
            writer.newLine();
        }

        log.info("Archivo generado OK: {}", rutaArchivo);
        return rutaArchivo;
    }

    // =========================================================
    // SEMANA 2 - Generar + subir a S3
    // Ruta S3: bucket/{idInscripcion}/resumen_{idInscripcion}.txt
    // =========================================================
    public String subirResumenAws(Long idInscripcion) throws IOException {
        String rutaArchivo   = generarArchivoResumen(idInscripcion);
        String nombreArchivo = "resumen_" + idInscripcion + ".txt";
        String clave         = idInscripcion + "/" + nombreArchivo;

        log.info("Subiendo a S3 -> bucket: {}, clave: {}", bucketName, clave);

        byte[] contenido = Files.readAllBytes(Paths.get(rutaArchivo));

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(clave)
                .contentType("text/plain")
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(contenido));

        // Limpiar archivo temporal
        new File(rutaArchivo).delete();

        String url = "https://" + bucketName + ".s3.amazonaws.com/" + clave;
        log.info("Archivo subido OK: {}", url);
        return url;
    }

    // =========================================================
    // SEMANA 2 - Actualizar/reemplazar archivo en S3
    // =========================================================
    public String actualizarResumenAws(Long idInscripcion, MultipartFile file) throws IOException {
        String clave = idInscripcion + "/" + file.getOriginalFilename();
        log.info("Actualizando en S3 -> bucket: {}, clave: {}", bucketName, clave);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(clave)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

        String url = "https://" + bucketName + ".s3.amazonaws.com/" + clave;
        log.info("Archivo actualizado OK: {}", url);
        return url;
    }

    // =========================================================
    // SEMANA 2 - Descargar archivo desde S3
    // =========================================================
    public byte[] descargarResumenAws(Long idInscripcion) throws IOException {
        String nombreArchivo = "resumen_" + idInscripcion + ".txt";
        String clave         = idInscripcion + "/" + nombreArchivo;

        log.info("Descargando de S3 -> bucket: {}, clave: {}", bucketName, clave);

        if (!existeEnS3(clave)) {
            throw new RuntimeException(
                    "Archivo no encontrado en S3: " + clave +
                    ". Primero debe subir el resumen.");
        }

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(clave)
                .build();

        ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(request);
        log.info("Archivo descargado OK: {}", clave);
        return response.asByteArray();
    }

    // =========================================================
    // SEMANA 2 - Eliminar archivo de S3
    // =========================================================
    public void eliminarResumenAws(Long idInscripcion) {
        String nombreArchivo = "resumen_" + idInscripcion + ".txt";
        String clave         = idInscripcion + "/" + nombreArchivo;

        log.info("Eliminando de S3 -> bucket: {}, clave: {}", bucketName, clave);

        if (!existeEnS3(clave)) {
            throw new RuntimeException(
                    "Archivo no encontrado en S3: " + clave);
        }

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(clave)
                .build();

        s3Client.deleteObject(request);
        log.info("Archivo eliminado OK: {}", clave);
    }

    // Helper: verifica si un objeto existe en S3
    private boolean existeEnS3(String clave) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(clave)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            log.error("Error verificando S3 {}: {}", clave, e.getMessage());
            return false;
        }
    }
}
