package com.plazoletaucc.Plazoleta.service.impl;

import com.plazoletaucc.Plazoleta.dto.reponse.DetallePedidoResponseDTO;
import com.plazoletaucc.Plazoleta.dto.reponse.PaginaResponseDTO;
import com.plazoletaucc.Plazoleta.dto.reponse.PedidoListadoResponseDTO;
import com.plazoletaucc.Plazoleta.dto.reponse.PedidoListoResponseDTO;
import com.plazoletaucc.Plazoleta.dto.reponse.PedidoPinResponseDTO;
import com.plazoletaucc.Plazoleta.dto.reponse.PedidoResponseDTO;
import com.plazoletaucc.Plazoleta.dto.request.DetallePedidoRequestDTO;
import com.plazoletaucc.Plazoleta.dto.request.PedidoRequestDTO;
import com.plazoletaucc.Plazoleta.entity.DetallePedido;
import com.plazoletaucc.Plazoleta.entity.EstadoPedido;
import com.plazoletaucc.Plazoleta.entity.Pedido;
import com.plazoletaucc.Plazoleta.entity.PedidoEstadoLog;
import com.plazoletaucc.Plazoleta.entity.Plato;
import com.plazoletaucc.Plazoleta.entity.Restaurante;
import com.plazoletaucc.Plazoleta.entity.Usuario;
import com.plazoletaucc.Plazoleta.event.PedidoListoEvent;
import com.plazoletaucc.Plazoleta.exception.AccesoDenegadoException;
import com.plazoletaucc.Plazoleta.exception.ReglaNegocioException;
import com.plazoletaucc.Plazoleta.repository.PedidoEstadoLogRepository;
import com.plazoletaucc.Plazoleta.repository.PedidoRepository;
import com.plazoletaucc.Plazoleta.repository.PlatoRespository;
import com.plazoletaucc.Plazoleta.repository.RestauranteRepository;
import com.plazoletaucc.Plazoleta.repository.UsuarioRepository;
import com.plazoletaucc.Plazoleta.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private static final int MAX_PAGE_SIZE = 50;

    private final PedidoRepository pedidoRepository;
    private final PedidoEstadoLogRepository pedidoEstadoLogRepository;
    private final UsuarioRepository usuarioRepository;
    private final RestauranteRepository restauranteRepository;
    private final PlatoRespository platoRespository;
    private final ApplicationEventPublisher eventPublisher;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public PedidoResponseDTO crearPedido(PedidoRequestDTO dto, String correoCliente) {
        // 1. Se bloquea al cliente para que dos peticiones simultaneas no creen dos pedidos.
        Usuario cliente = usuarioRepository.findByCorreoForUpdate(correoCliente)
                .orElseThrow(() -> new ReglaNegocioException("El cliente no existe"));

        // 2. Solo se permite un pedido en proceso por cliente.
        List<EstadoPedido> estadosEnProceso = List.of(
                EstadoPedido.PENDIENTE,
                EstadoPedido.EN_PREPARACION,
                EstadoPedido.LISTO
        );

        if (pedidoRepository.existsByCliente_IdAndEstadoIn(cliente.getId(), estadosEnProceso)) {
            throw new ReglaNegocioException("Ya tienes un pedido en proceso");
        }

        // 3. El restaurante indicado debe existir.
        Restaurante restaurante = restauranteRepository.findById(dto.getIdRestaurante())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El restaurante no existe"));

        // 4. Todos los platos deben existir, estar activos y ser del mismo restaurante.
        Map<Integer, Plato> platos = cargarYValidarPlatos(dto, restaurante);

        // 5. El pedido nace en PENDIENTE.
        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .restaurante(restaurante)
                .estado(EstadoPedido.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .build();

        int total = 0;

        for (DetallePedidoRequestDTO item : dto.getPlatos()) {
            Plato plato = platos.get(item.getIdPlato());
            total += plato.getPrecio() * item.getCantidad();

            pedido.agregarDetalle(DetallePedido.builder()
                    .plato(plato)
                    .cantidad(item.getCantidad())
                    .precioUnitario(plato.getPrecio())
                    .build());
        }

        pedido.setTotal(total);

        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        registrarCambioEstado(pedidoGuardado, null, EstadoPedido.PENDIENTE, null);

        return convertir(pedidoGuardado);
    }

    // HU-12: el empleado solo ve los pedidos de su restaurante, filtrados por estado.
    @Override
    @Transactional(readOnly = true)
    public PaginaResponseDTO<PedidoListadoResponseDTO> listarPedidosPorEstado(EstadoPedido estado, int pagina,
                                                                             int tamano, String correoEmpleado) {
        // 1. La pagina y el tamano deben tener sentido.
        if (pagina < 0) {
            throw new ReglaNegocioException("La pagina debe ser 0 o mayor");
        }
        if (tamano < 1) {
            throw new ReglaNegocioException("El tamano de pagina debe ser 1 o mayor");
        }
        if (tamano > MAX_PAGE_SIZE) {
            throw new ReglaNegocioException("El tamano de pagina no puede superar " + MAX_PAGE_SIZE);
        }
        if (estado == null) {
            throw new ReglaNegocioException("El estado es obligatorio");
        }

        // 2. El empleado debe existir y tener un restaurante asignado.
        Usuario empleado = obtenerEmpleadoConRestaurante(correoEmpleado);
        Restaurante restaurante = empleado.getRestaurante();

        // 3. Se consultan solo los pedidos del restaurante del empleado.
        Pageable pageable = PageRequest.of(pagina, tamano,
                Sort.by("fechaCreacion").ascending().and(Sort.by("id").ascending()));
        Page<Pedido> resultado = pedidoRepository.findByRestaurante_IdAndEstado(
                restaurante.getId(), estado, pageable);

        List<PedidoListadoResponseDTO> contenido = resultado.getContent().stream()
                .map(this::convertirListado)
                .toList();

        return PaginaResponseDTO.<PedidoListadoResponseDTO>builder()
                .contenido(contenido)
                .pagina(resultado.getNumber())
                .tamano(resultado.getSize())
                .totalElementos(resultado.getTotalElements())
                .totalPaginas(resultado.getTotalPages())
                .build();
    }

    // HU-13: el empleado se asigna al pedido y este pasa a EN_PREPARACION.
    @Override
    @Transactional
    public PedidoResponseDTO asignarPedido(Integer idPedido, String correoEmpleado) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El pedido no existe"));

        Usuario empleado = obtenerEmpleadoConRestaurante(correoEmpleado);
        validarPedidoDelRestaurante(pedido, empleado);

        // Solo un pedido pendiente puede asignarse; ya asignado queda EN_PREPARACION.
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new ReglaNegocioException("Solo se pueden asignar pedidos en estado PENDIENTE");
        }

        EstadoPedido estadoAnterior = pedido.getEstado();
        pedido.setEmpleado(empleado);
        pedido.setEstado(EstadoPedido.EN_PREPARACION);
        pedidoRepository.save(pedido);
        registrarCambioEstado(pedido, estadoAnterior, EstadoPedido.EN_PREPARACION, empleado);

        return convertir(pedido);
    }

    // HU-14: el empleado notifica que el pedido esta listo y se envia el PIN por SMS.
    @Override
    @Transactional
    public PedidoListoResponseDTO marcarPedidoListo(Integer idPedido, String correoEmpleado) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El pedido no existe"));

        Usuario empleado = obtenerEmpleadoConRestaurante(correoEmpleado);
        validarPedidoDelRestaurante(pedido, empleado);

        if (pedido.getEstado() != EstadoPedido.EN_PREPARACION) {
            throw new ReglaNegocioException(
                    "Solo se puede marcar como LISTO un pedido en estado EN_PREPARACION");
        }

        EstadoPedido estadoAnterior = pedido.getEstado();
        String pin = generarPin();
        pedido.setPin(pin);
        pedido.setEstado(EstadoPedido.LISTO);
        pedidoRepository.save(pedido);
        registrarCambioEstado(pedido, estadoAnterior, EstadoPedido.LISTO, empleado);

        // El cliente recibe el PIN en su celular para reclamar el pedido.
        // El evento se procesa despues del commit para no notificar un pedido que termine en rollback.
        eventPublisher.publishEvent(new PedidoListoEvent(
                pedido.getCliente().getCelular(), pin, pedido.getId()));

        // Importante: el PIN NO se devuelve en la respuesta al empleado.
        return PedidoListoResponseDTO.builder()
                .id(pedido.getId())
                .estado(pedido.getEstado())
                .mensaje("Pedido listo. Se envio el PIN de seguridad al cliente por SMS")
                .build();
    }

    // HU-14/HU-15: el cliente consulta el PIN de su propio pedido cuando esta LISTO.
    @Override
    @Transactional(readOnly = true)
    public PedidoPinResponseDTO consultarPinPedido(Integer idPedido, String correoCliente) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El pedido no existe"));

        Usuario cliente = usuarioRepository.findByCorreo(correoCliente)
                .orElseThrow(() -> new AccesoDenegadoException("El cliente no existe"));

        if (!pedido.getCliente().getId().equals(cliente.getId())) {
            throw new AccesoDenegadoException("El pedido no pertenece al cliente");
        }

        if (pedido.getEstado() != EstadoPedido.LISTO || pedido.getPin() == null) {
            throw new ReglaNegocioException("El pedido no esta LISTO");
        }

        return PedidoPinResponseDTO.builder()
                .id(pedido.getId())
                .pin(pedido.getPin())
                .build();
    }

    // HU-15: el empleado entrega el pedido solo si esta LISTO y digita el PIN correcto.
    @Override
    @Transactional
    public PedidoResponseDTO entregarPedido(Integer idPedido, String pin, String correoEmpleado) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El pedido no existe"));

        Usuario empleado = obtenerEmpleadoConRestaurante(correoEmpleado);
        validarPedidoDelRestaurante(pedido, empleado);

        // HU-15: un pedido ENTREGADO no puede modificarse a otro estado ni reentregarse.
        if (pedido.getEstado() == EstadoPedido.ENTREGADO) {
            throw new ReglaNegocioException("El pedido ya esta ENTREGADO");
        }

        // Solo un pedido LISTO puede pasar a ENTREGADO.
        if (pedido.getEstado() != EstadoPedido.LISTO) {
            throw new ReglaNegocioException("Solo se puede entregar un pedido en estado LISTO");
        }

        // El PIN debe coincidir con el que se envio al cliente cuando el pedido quedo LISTO.
        if (pedido.getPin() == null || pin == null || !pedido.getPin().equals(pin)) {
            throw new ReglaNegocioException("El PIN de seguridad no es correcto");
        }

        EstadoPedido estadoAnterior = pedido.getEstado();
        pedido.setEstado(EstadoPedido.ENTREGADO);
        pedidoRepository.save(pedido);
        registrarCambioEstado(pedido, estadoAnterior, EstadoPedido.ENTREGADO, empleado);

        return convertir(pedido);
    }

    // HU-16: el cliente cancela su pedido solo si esta PENDIENTE.
    @Override
    @Transactional
    public PedidoResponseDTO cancelarPedido(Integer idPedido, String correoCliente) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El pedido no existe"));

        Usuario cliente = usuarioRepository.findByCorreo(correoCliente)
                .orElseThrow(() -> new AccesoDenegadoException("El cliente no existe"));

        // El cliente solo puede cancelar sus propios pedidos.
        if (!pedido.getCliente().getId().equals(cliente.getId())) {
            throw new AccesoDenegadoException("El pedido no pertenece al cliente");
        }

        // HU-16: cualquier estado distinto de PENDIENTE impide la cancelacion.
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new ReglaNegocioException(
                    "Lo sentimos, tu pedido ya está en preparación y no puede cancelarse");
        }

        EstadoPedido estadoAnterior = pedido.getEstado();
        pedido.setEstado(EstadoPedido.CANCELADO);
        pedidoRepository.save(pedido);
        registrarCambioEstado(pedido, estadoAnterior, EstadoPedido.CANCELADO, null);

        return convertir(pedido);
    }

    private Usuario obtenerEmpleadoConRestaurante(String correoEmpleado) {
        Usuario empleado = usuarioRepository.findByCorreo(correoEmpleado)
                .orElseThrow(() -> new AccesoDenegadoException("El empleado no existe"));

        if (empleado.getRestaurante() == null) {
            throw new AccesoDenegadoException("El empleado no tiene un restaurante asignado");
        }

        return empleado;
    }

    private void validarPedidoDelRestaurante(Pedido pedido, Usuario empleado) {
        if (!pedido.getRestaurante().getId().equals(empleado.getRestaurante().getId())) {
            throw new AccesoDenegadoException("El pedido no pertenece al restaurante del empleado");
        }
    }

    private String generarPin() {
        return String.format("%04d", secureRandom.nextInt(10_000));
    }

    // HU-17: registra cada cambio de estado para que el cliente consulte la trazabilidad.
    private void registrarCambioEstado(Pedido pedido, EstadoPedido estadoAnterior,
                                       EstadoPedido estadoNuevo, Usuario empleado) {
        pedidoEstadoLogRepository.save(PedidoEstadoLog.builder()
                .pedido(pedido)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(estadoNuevo)
                .empleado(empleado)
                .fechaCambio(LocalDateTime.now())
                .build());
    }

    private Map<Integer, Plato> cargarYValidarPlatos(PedidoRequestDTO dto, Restaurante restaurante) {
        List<Integer> ids = dto.getPlatos().stream()
                .map(DetallePedidoRequestDTO::getIdPlato)
                .toList();

        Set<Integer> idsUnicos = new HashSet<>(ids);
        if (idsUnicos.size() != ids.size()) {
            throw new ReglaNegocioException("No se puede repetir un plato en el pedido");
        }

        List<Plato> encontrados = platoRespository.findAllById(ids);

        if (encontrados.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Uno o mas platos no existen");
        }

        Map<Integer, Plato> porId = encontrados.stream()
                .collect(Collectors.toMap(Plato::getId, Function.identity()));

        for (Plato plato : encontrados) {
            if (!plato.getRestaurante().getId().equals(restaurante.getId())) {
                throw new ReglaNegocioException("Todos los platos deben ser del mismo restaurante");
            }
            if (!Boolean.TRUE.equals(plato.getEstado())) {
                throw new ReglaNegocioException("El plato '" + plato.getNombre() + "' no esta activo");
            }
        }

        return porId;
    }

    private PedidoResponseDTO convertir(Pedido pedido) {
        return PedidoResponseDTO.builder()
                .id(pedido.getId())
                .idRestaurante(pedido.getRestaurante().getId())
                .nombreRestaurante(pedido.getRestaurante().getNombre())
                .estado(pedido.getEstado())
                .idEmpleadoAsignado(pedido.getEmpleado() == null ? null : pedido.getEmpleado().getId())
                .fechaCreacion(pedido.getFechaCreacion())
                .total(pedido.getTotal())
                .platos(convertirPlatos(pedido))
                .build();
    }

    // HU-12: se listan todos los campos del pedido y sus platos.
    private PedidoListadoResponseDTO convertirListado(Pedido pedido) {
        Usuario cliente = pedido.getCliente();
        String nombreCliente = cliente.getApellido() == null
                ? cliente.getNombre()
                : cliente.getNombre() + " " + cliente.getApellido();

        return PedidoListadoResponseDTO.builder()
                .id(pedido.getId())
                .idCliente(cliente.getId())
                .nombreCliente(nombreCliente)
                .idRestaurante(pedido.getRestaurante().getId())
                .nombreRestaurante(pedido.getRestaurante().getNombre())
                .estado(pedido.getEstado())
                .idEmpleadoAsignado(pedido.getEmpleado() == null ? null : pedido.getEmpleado().getId())
                .fechaCreacion(pedido.getFechaCreacion())
                .total(pedido.getTotal())
                .platos(convertirPlatos(pedido))
                .build();
    }

    private List<DetallePedidoResponseDTO> convertirPlatos(Pedido pedido) {
        return pedido.getDetalles().stream()
                .map(detalle -> DetallePedidoResponseDTO.builder()
                        .idPlato(detalle.getPlato().getId())
                        .nombre(detalle.getPlato().getNombre())
                        .cantidad(detalle.getCantidad())
                        .precioUnitario(detalle.getPrecioUnitario())
                        .subtotal(detalle.getPrecioUnitario() * detalle.getCantidad())
                        .build())
                .toList();
    }
}
