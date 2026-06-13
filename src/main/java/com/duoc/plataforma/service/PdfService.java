package com.duoc.plataforma.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.duoc.plataforma.model.GuiaDespacho;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    public byte[] generarResumenPdf(GuiaDespacho guia) {
        try {
            Document document = new Document();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);
            document.open();

            Font tituloFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font labelFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font valorFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);

            Paragraph titulo = new Paragraph("Resumen de Inscripción / Guía de Despacho", tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(20);
            document.add(titulo);

            document.add(crearFila("ID Resumen:", String.valueOf(guia.getId()), labelFont, valorFont));
            document.add(crearFila("Transportista:", guia.getTransportista(), labelFont, valorFont));
            document.add(crearFila("Fecha:", guia.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), labelFont, valorFont));
            document.add(crearFila("Estado:", guia.getEstado() != null ? guia.getEstado() : "N/A", labelFont, valorFont));
            document.add(crearFila("Nombre Archivo:", guia.getNombreArchivo() != null ? guia.getNombreArchivo() : "N/A", labelFont, valorFont));

            document.close();
            return baos.toByteArray();

        } catch (DocumentException e) {
            throw new RuntimeException("Error al generar el PDF", e);
        }
    }

    private Paragraph crearFila(String label, String valor, Font labelFont, Font valorFont) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + " ", labelFont));
        p.add(new Chunk(valor, valorFont));
        p.setSpacingAfter(8);
        return p;
    }
}