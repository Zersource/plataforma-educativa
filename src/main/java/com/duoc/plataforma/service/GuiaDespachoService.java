package com.duoc.plataforma.service;

import com.duoc.plataforma.dto.GuiaRequestDTO;
import com.duoc.plataforma.dto.GuiaResponseDTO;
import com.duoc.plataforma.model.GuiaDespacho;
import com.duoc.plataforma.repository.GuiaDespachoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuiaDespachoService {

    private final GuiaDespachoRepository repository;
    private final S3Client s3Client;
    private final PdfService pdfService;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${app.efs.path:/app/efs}")
    private String efsPath;

    public GuiaResponseDTO crearGuia(GuiaRequestDTO dto) {
        GuiaDespacho guia = new GuiaDespacho();
        guia.setTransportista(dto.getTransportista());
        guia.setFecha(dto.getFecha() != null ? dto.getFecha() : LocalDate.now());
        guia.setNombreArchivo(dto.getNombreArchivo());
        guia.setEstado("PENDIENTE");

        String rutaEfs = efsPath + "/" + dto.getNombreArchivo();
        guia.setRutaEfs(rutaEfs);

        try {
            Path dirPath = Paths.get(efsPath);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            Path filePath = Paths.get(rutaEfs);
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al crear archivo en EFS: " + e.getMessage());
        }

        GuiaDespacho saved = repository.save(guia);
        return toResponse(saved);
    }

    public GuiaResponseDTO subirGuiaS3(Long id, MultipartFile file) {
        GuiaDespacho guia = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guía no encontrada con id: " + id));

        String s3Key = guia.getId() + "/" + guia.getNombreArchivo();

        try {
            String rutaEfs = efsPath + "/" + guia.getNombreArchivo();
            File tempFile = new File(rutaEfs);
            tempFile.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(file.getBytes());
            }

            s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build(),
                RequestBody.fromFile(tempFile)
            );

            guia.setRutaS3(s3Key);
            guia.setRutaEfs(rutaEfs);
            guia.setEstado("SUBIDO");
            GuiaDespacho saved = repository.save(guia);
            return toResponse(saved);

        } catch (IOException e) {
            throw new RuntimeException("Error al subir guía: " + e.getMessage());
        }
    }

    public byte[] generarPdfResumen(Long id) {
        GuiaDespacho guia = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guía no encontrada con id: " + id));
        return pdfService.generarResumenPdf(guia);
    }

    public byte[] descargarGuia(Long id) {
        GuiaDespacho guia = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guía no encontrada con id: " + id));

        if (guia.getRutaS3() == null) {
            throw new RuntimeException("La guía aún no ha sido subida a S3");
        }

        try {
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(
                GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(guia.getRutaS3())
                    .build()
            );
            return response.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Error al descargar guía: " + e.getMessage());
        }
    }

    public GuiaResponseDTO modificarGuia(Long id, GuiaRequestDTO dto) {
        GuiaDespacho guia = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guía no encontrada con id: " + id));

        if (dto.getTransportista() != null) guia.setTransportista(dto.getTransportista());
        if (dto.getFecha() != null) guia.setFecha(dto.getFecha());
        if (dto.getNombreArchivo() != null) guia.setNombreArchivo(dto.getNombreArchivo());

        GuiaDespacho saved = repository.save(guia);
        return toResponse(saved);
    }

    public void eliminarGuia(Long id) {
        GuiaDespacho guia = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guía no encontrada con id: " + id));

        if (guia.getRutaS3() != null) {
            s3Client.deleteObject(
                DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(guia.getRutaS3())
                    .build()
            );
        }

        if (guia.getRutaEfs() != null) {
            try {
                Files.deleteIfExists(Paths.get(guia.getRutaEfs()));
            } catch (IOException e) {
                // ignorar
            }
        }

        repository.deleteById(id);
    }

    public List<GuiaResponseDTO> consultarGuias(String transportista, LocalDate fecha) {
        List<GuiaDespacho> guias;

        if (transportista != null && fecha != null) {
            guias = repository.findByTransportistaAndFecha(transportista, fecha);
        } else if (transportista != null) {
            guias = repository.findByTransportista(transportista);
        } else if (fecha != null) {
            guias = repository.findByFecha(fecha);
        } else {
            guias = repository.findAll();
        }

        return guias.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private GuiaResponseDTO toResponse(GuiaDespacho guia) {
        GuiaResponseDTO dto = new GuiaResponseDTO();
        dto.setId(guia.getId());
        dto.setTransportista(guia.getTransportista());
        dto.setFecha(guia.getFecha());
        dto.setNombreArchivo(guia.getNombreArchivo());
        dto.setRutaS3(guia.getRutaS3());
        dto.setRutaEfs(guia.getRutaEfs());
        dto.setEstado(guia.getEstado());
        return dto;
    }
}