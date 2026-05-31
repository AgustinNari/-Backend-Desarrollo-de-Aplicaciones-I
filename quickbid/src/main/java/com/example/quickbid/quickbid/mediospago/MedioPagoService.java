package com.example.quickbid.quickbid.mediospago;

import com.example.quickbid.quickbid.common.exception.AppException;
import com.example.quickbid.quickbid.mediospago.dto.CrearMedioPagoRequest;
import com.example.quickbid.quickbid.mediospago.dto.MedioPagoResponse;
import com.example.quickbid.quickbid.usuario.Usuario;
import com.example.quickbid.quickbid.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedioPagoService {

    private final MedioPagoRepository medioPagoRepository;
    private final UsuarioRepository usuarioRepository;

    // ─── GET /api/usuario/medios-pago ─────────────────────────────────────────

    public List<MedioPagoResponse> listar(String email) {
        Usuario usuario = getUsuario(email);
        return medioPagoRepository
                .findByUsuarioIdAndEliminadoFalse(usuario.getId())
                .stream()
                .map(MedioPagoResponse::new)
                .toList();
    }

    // ─── POST /api/usuario/medios-pago ────────────────────────────────────────

    @Transactional
    public Long crear(String email, CrearMedioPagoRequest req,
                      MultipartFile fotoAnverso, MultipartFile fotoReverso) {
        Usuario usuario = getUsuario(email);
        MedioPago medio;

        switch (req.getTipo()) {
            case "tarjeta_credito", "tarjeta_debito" -> medio = crearTarjeta(req, usuario.getId());
            case "cuenta_bancaria"                   -> medio = crearCuenta(req, usuario.getId());
            case "cheque"                            -> medio = crearCheque(req, usuario.getId(), fotoAnverso, fotoReverso);
            default -> throw new AppException("Tipo de medio de pago invalido", HttpStatus.BAD_REQUEST);
        }

        // Verificar duplicado
        if (medioPagoRepository.existsByUsuarioIdAndTipoAndDatosEnmascaradosAndEliminadoFalse(
                usuario.getId(), medio.getTipo(), medio.getDatosEnmascarados())) {
            throw new AppException("Ya existe un medio identico registrado", HttpStatus.CONFLICT);
        }

        return medioPagoRepository.save(medio).getIdentificador();
    }

    // ─── DELETE /api/usuario/medios-pago/{id} ─────────────────────────────────

    @Transactional
    public void eliminar(String email, Long id) {
        Usuario usuario = getUsuario(email);
        MedioPago medio = medioPagoRepository
                .findByIdentificadorAndUsuarioIdAndEliminadoFalse(id, usuario.getId())
                .orElseThrow(() -> new AppException("Medio de pago no encontrado", HttpStatus.NOT_FOUND));

        // TODO: validar que no este asociado a operacion pendiente cuando existan pujas/compras
        medio.setEliminado(true);
        medio.setEsPrincipal(false);
        medioPagoRepository.save(medio);
    }

    // ─── PATCH /api/usuario/medios-pago/{id}/principal ────────────────────────

    @Transactional
    public void marcarPrincipal(String email, Long id) {
        Usuario usuario = getUsuario(email);
        MedioPago medio = medioPagoRepository
                .findByIdentificadorAndUsuarioIdAndEliminadoFalse(id, usuario.getId())
                .orElseThrow(() -> new AppException("Medio de pago no encontrado", HttpStatus.NOT_FOUND));

        medioPagoRepository.desmarcarPrincipal(usuario.getId());
        medio.setEsPrincipal(true);
        medioPagoRepository.save(medio);
    }

    // ─── Helpers de construccion ──────────────────────────────────────────────

    private MedioPago crearTarjeta(CrearMedioPagoRequest req, Long usuarioId) {
        if (req.getNombreTitular() == null || req.getNumeroTarjeta() == null
                || req.getVencimiento() == null || req.getCvv() == null || req.getNacional() == null) {
            throw new AppException("Campos faltantes para tarjeta", HttpStatus.BAD_REQUEST);
        }
        String numero = req.getNumeroTarjeta().replaceAll("\\s", "");
        if (numero.length() != 16) {
            throw new AppException("El numero de tarjeta debe tener 16 digitos", HttpStatus.BAD_REQUEST);
        }
        if (req.getCvv().length() != 3) {
            throw new AppException("CVV invalido", HttpStatus.BAD_REQUEST);
        }

        String ultimos4 = numero.substring(12);
        String enmascarado = "**** **** **** " + ultimos4;
        String marca = detectarMarca(numero);

        return MedioPago.builder()
                .usuarioId(usuarioId)
                .tipo(req.getTipo())
                .moneda(req.getMoneda())
                .titular(req.getNombreTitular())
                .datosEnmascarados(enmascarado)
                .marca(marca)
                .vencimientoTarjeta(req.getVencimiento())
                .nacional(req.getNacional())
                // CVV NO se persiste
                .build();
    }

    private MedioPago crearCuenta(CrearMedioPagoRequest req, Long usuarioId) {
        if (req.getNumeroCuenta() == null || req.getNombreBanco() == null || req.getNacional() == null) {
            throw new AppException("Campos faltantes para cuenta bancaria", HttpStatus.BAD_REQUEST);
        }
        String enmascarado = "****" + req.getNumeroCuenta()
                .substring(Math.max(0, req.getNumeroCuenta().length() - 4));

        return MedioPago.builder()
                .usuarioId(usuarioId)
                .tipo("cuenta_bancaria")
                .moneda(req.getMoneda())
                .numeroCuenta(req.getNumeroCuenta())
                .nombreBanco(req.getNombreBanco())
                .aliasCbu(req.getAlias())
                .nacional(req.getNacional())
                .datosEnmascarados(enmascarado)
                .build();
    }

    private MedioPago crearCheque(CrearMedioPagoRequest req, Long usuarioId,
                                   MultipartFile fotoAnverso, MultipartFile fotoReverso) {
        if (req.getMonto() == null || req.getFechaVencimiento() == null
                || req.getNumeroCheque() == null) {
            throw new AppException("Campos faltantes para cheque", HttpStatus.BAD_REQUEST);
        }
        if (fotoAnverso == null || fotoAnverso.isEmpty()
                || fotoReverso == null || fotoReverso.isEmpty()) {
            throw new AppException("Se requieren fotos del anverso y reverso del cheque", HttpStatus.BAD_REQUEST);
        }

        byte[] anversoBytes;
        byte[] reversoBytes;
        try {
            anversoBytes = fotoAnverso.getBytes();
            reversoBytes = fotoReverso.getBytes();
        } catch (IOException e) {
            throw new AppException("Error al procesar las imagenes del cheque", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return MedioPago.builder()
                .usuarioId(usuarioId)
                .tipo("cheque")
                .moneda(req.getMoneda())
                .numeroCheque(req.getNumeroCheque())
                .fechaVencimientoCheque(req.getFechaVencimiento())
                .limiteOperativo(req.getMonto())
                .fotoAnversoCheque(anversoBytes)
                .fotoReversoCheque(reversoBytes)
                .datosEnmascarados("Cheque #" + req.getNumeroCheque())
                .build();
    }

    private String detectarMarca(String numero) {
        if (numero.startsWith("4")) return "Visa";
        if (numero.startsWith("5")) return "Mastercard";
        if (numero.startsWith("34") || numero.startsWith("37")) return "Amex";
        return "Otra";
    }

    private Usuario getUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("Usuario no encontrado", HttpStatus.NOT_FOUND));
    }
}
