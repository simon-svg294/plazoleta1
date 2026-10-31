package com.plazoletaucc.Plazoleta.service.impl;

import com.plazoletaucc.Plazoleta.dto.reponse.DetallePedidoResponseDTO;
import com.plazoletaucc.Plazoleta.dto.reponse.PedidoResponseDTO;
import com.plazoletaucc.Plazoleta.dto.request.DetallePedidoRequestDTO;
import com.plazoletaucc.Plazoleta.dto.request.PedidoRequestDTO;
import com.plazoletaucc.Plazoleta.entity.DetallePedido;
import com.plazoletaucc.Plazoleta.entity.EstadoPedido;
import com.plazoletaucc.Plazoleta.entity.Pedido;
import com.plazoletaucc.Plazoleta.entity.Plato;
import com.plazoletaucc.Plazoleta.entity.Restaurante;
import com.plazoletaucc.Plazoleta.entity.Usuario;
import com.plazoletaucc.Plazoleta.exception.ReglaNegocioException;
import com.plazoletaucc.Plazoleta.repository.PedidoRepository;
import com.plazoletaucc.Plazoleta.repository.PlatoRespository;
import com.plazoletaucc.Plazoleta.repository.RestauranteRepository;
import com.plazoletaucc.Plazoleta.repository.UsuarioRepository;
import com.plazoletaucc.Plazoleta.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RestauranteRepository restauranteRepository;
    private final PlatoRespository platoRespository;

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

        return convertir(pedidoRepository.save(pedido));
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
        List<DetallePedidoResponseDTO> platos = pedido.getDetalles().stream()
                .map(detalle -> DetallePedidoResponseDTO.builder()
                        .idPlato(detalle.getPlato().getId())
                        .nombre(detalle.getPlato().getNombre())
                        .cantidad(detalle.getCantidad())
                        .precioUnitario(detalle.getPrecioUnitario())
                        .subtotal(detalle.getPrecioUnitario() * detalle.getCantidad())
                        .build())
                .toList();

        return PedidoResponseDTO.builder()
                .id(pedido.getId())
                .idRestaurante(pedido.getRestaurante().getId())
                .nombreRestaurante(pedido.getRestaurante().getNombre())
                .estado(pedido.getEstado())
                .fechaCreacion(pedido.getFechaCreacion())
                .total(pedido.getTotal())
                .platos(platos)
                .build();
    }
}
