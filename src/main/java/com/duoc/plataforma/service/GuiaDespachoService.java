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
        // Si no se puede crear en EFS, continuamos igual
        System.out.println("Advertencia EFS: " + e.getMessage());
    }

    GuiaDespacho saved = repository.save(guia);
    return toResponse(saved);
}