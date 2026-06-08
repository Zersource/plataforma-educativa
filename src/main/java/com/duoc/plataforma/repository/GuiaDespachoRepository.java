package com.duoc.plataforma.repository;

import com.duoc.plataforma.model.GuiaDespacho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GuiaDespachoRepository extends JpaRepository<GuiaDespacho, Long> {
    List<GuiaDespacho> findByTransportista(String transportista);
    List<GuiaDespacho> findByFecha(LocalDate fecha);
    List<GuiaDespacho> findByTransportistaAndFecha(String transportista, LocalDate fecha);
}
