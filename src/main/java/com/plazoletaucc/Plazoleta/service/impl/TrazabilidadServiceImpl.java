package com.plazoletaucc.Plazoleta.service.impl;

import com.plazoletaucc.Plazoleta.dto.reponse.PedidoEstadoLogResponseDTO;
import com.plazoletaucc.Plazoleta.entity.Pedido;
import com.plazoletaucc.Plazoleta.entity.PedidoEstadoLog;
import com.plazoletaucc.Plazoleta.entity.Usuario;
import com.plazoletaucc.Plazoleta.exception.AccesoDenegadoException;
import com.plazoletaucc.Plazoleta.repository.PedidoEstadoLogRepository;
import com.plazoletaucc.Plazoleta.repository.PedidoRepository;
import com.plazoletaucc.Plazoleta.repository.UsuarioRepository;
import com.plazoletaucc.Plazoleta.service.TrazabilidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrazabilidadServiceImpl implements TrazabilidadService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoEstadoLogRepository pedidoEstadoLogRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PedidoEstadoLogResponseDTO> consultarTrazabilidad(Integer idPedido, String correoCliente) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El pedido no existe"));

        Usuario cliente = usuarioRepository.findByCorreo(correoCliente)
                .orElseThrow(() -> new AccesoDenegadoException("El cliente no existe"));

        // HU-17: cada cliente solo puede consultar los logs de sus propios pedidos.
        if (!pedido.getCliente().getId().equals(cliente.getId())) {
            throw new AccesoDenegadoException("El pedido no pertenece al cliente");
        }

        return pedidoEstadoLogRepository.findByPedido_IdOrderByFechaCambioAscIdAsc(idPedido)
                .stream()
                .map(this::convertir)
                .toList();
    }

    private PedidoEstadoLogResponseDTO convertir(PedidoEstadoLog log) {
        Usuario empleado = log.getEmpleado();

        return PedidoEstadoLogResponseDTO.builder()
                .id(log.getId())
                .idPedido(log.getPedido().getId())
                .estadoAnterior(log.getEstadoAnterior())
                .estadoNuevo(log.getEstadoNuevo())
                .idEmpleado(empleado == null ? null : empleado.getId())
                .nombreEmpleado(empleado == null ? null : nombreCompleto(empleado))
                .fechaCambio(log.getFechaCambio())
                .build();
    }

    private String nombreCompleto(Usuario usuario) {
        if (usuario.getApellido() == null || usuario.getApellido().isBlank()) {
            return usuario.getNombre();
        }
        return usuario.getNombre() + " " + usuario.getApellido();
    }
}
