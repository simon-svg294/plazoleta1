package com.plazoletaucc.Plazoleta.service.impl;

import com.plazoletaucc.Plazoleta.dto.reponse.PedidoEstadoLogResponseDTO;
import com.plazoletaucc.Plazoleta.entity.EstadoPedido;
import com.plazoletaucc.Plazoleta.entity.Pedido;
import com.plazoletaucc.Plazoleta.entity.PedidoEstadoLog;
import com.plazoletaucc.Plazoleta.entity.Usuario;
import com.plazoletaucc.Plazoleta.exception.AccesoDenegadoException;
import com.plazoletaucc.Plazoleta.repository.PedidoEstadoLogRepository;
import com.plazoletaucc.Plazoleta.repository.PedidoRepository;
import com.plazoletaucc.Plazoleta.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrazabilidadServiceImplTest {

    private static final String CORREO_CLIENTE = "cliente@plazoleta.com";

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PedidoEstadoLogRepository pedidoEstadoLogRepository;

    @InjectMocks
    private TrazabilidadServiceImpl trazabilidadService;

    @Test
    void consultarTrazabilidad_cuandoEsSuPedido_devuelveLosLogsEnOrden() {
        Usuario cliente = cliente(1);
        Usuario empleado = Usuario.builder()
                .id(2)
                .nombre("Empleado")
                .apellido("Prueba")
                .build();
        Pedido pedido = Pedido.builder().id(50).cliente(cliente).build();
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 3, 10, 0);
        PedidoEstadoLog creado = log(1, pedido, null, EstadoPedido.PENDIENTE, null, inicio);
        PedidoEstadoLog asignado = log(2, pedido, EstadoPedido.PENDIENTE,
                EstadoPedido.EN_PREPARACION, empleado, inicio.plusMinutes(5));
        PedidoEstadoLog listo = log(3, pedido, EstadoPedido.EN_PREPARACION,
                EstadoPedido.LISTO, empleado, inicio.plusMinutes(20));

        when(pedidoRepository.findById(50)).thenReturn(Optional.of(pedido));
        when(usuarioRepository.findByCorreo(CORREO_CLIENTE)).thenReturn(Optional.of(cliente));
        when(pedidoEstadoLogRepository.findByPedido_IdOrderByFechaCambioAscIdAsc(50))
                .thenReturn(List.of(creado, asignado, listo));

        List<PedidoEstadoLogResponseDTO> respuesta =
                trazabilidadService.consultarTrazabilidad(50, CORREO_CLIENTE);

        assertEquals(3, respuesta.size());
        assertAll(
                () -> assertEquals(1, respuesta.get(0).getId()),
                () -> assertNull(respuesta.get(0).getEstadoAnterior()),
                () -> assertEquals(EstadoPedido.PENDIENTE, respuesta.get(0).getEstadoNuevo()),
                () -> assertNull(respuesta.get(0).getIdEmpleado()),
                () -> assertNull(respuesta.get(0).getNombreEmpleado()),
                () -> assertEquals(50, respuesta.get(0).getIdPedido()),
                () -> assertEquals(EstadoPedido.PENDIENTE, respuesta.get(1).getEstadoAnterior()),
                () -> assertEquals(EstadoPedido.EN_PREPARACION, respuesta.get(1).getEstadoNuevo()),
                () -> assertEquals(2, respuesta.get(1).getIdEmpleado()),
                () -> assertEquals("Empleado Prueba", respuesta.get(1).getNombreEmpleado()),
                () -> assertEquals(EstadoPedido.LISTO, respuesta.get(2).getEstadoNuevo())
        );
    }

    @Test
    void consultarTrazabilidad_cuandoNoTieneLogs_devuelveListaVacia() {
        Usuario cliente = cliente(1);
        Pedido pedido = Pedido.builder().id(50).cliente(cliente).build();

        when(pedidoRepository.findById(50)).thenReturn(Optional.of(pedido));
        when(usuarioRepository.findByCorreo(CORREO_CLIENTE)).thenReturn(Optional.of(cliente));
        when(pedidoEstadoLogRepository.findByPedido_IdOrderByFechaCambioAscIdAsc(50))
                .thenReturn(List.of());

        List<PedidoEstadoLogResponseDTO> respuesta =
                trazabilidadService.consultarTrazabilidad(50, CORREO_CLIENTE);

        assertTrue(respuesta.isEmpty());
    }

    @Test
    void consultarTrazabilidad_cuandoElPedidoNoExiste_lanzaNotFound() {
        when(pedidoRepository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> trazabilidadService.consultarTrazabilidad(99, CORREO_CLIENTE));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("El pedido no existe", exception.getReason());
        verifyNoInteractions(usuarioRepository, pedidoEstadoLogRepository);
    }

    @Test
    void consultarTrazabilidad_cuandoElClienteNoExiste_lanzaAccesoDenegado() {
        Pedido pedido = Pedido.builder().id(50).cliente(cliente(1)).build();

        when(pedidoRepository.findById(50)).thenReturn(Optional.of(pedido));
        when(usuarioRepository.findByCorreo(CORREO_CLIENTE)).thenReturn(Optional.empty());

        AccesoDenegadoException exception = assertThrows(AccesoDenegadoException.class,
                () -> trazabilidadService.consultarTrazabilidad(50, CORREO_CLIENTE));

        assertEquals("El cliente no existe", exception.getMessage());
        verifyNoInteractions(pedidoEstadoLogRepository);
    }

    @Test
    void consultarTrazabilidad_cuandoEsDeOtroCliente_lanzaAccesoDenegado() {
        Pedido pedido = Pedido.builder().id(50).cliente(cliente(1)).build();

        when(pedidoRepository.findById(50)).thenReturn(Optional.of(pedido));
        when(usuarioRepository.findByCorreo(CORREO_CLIENTE)).thenReturn(Optional.of(cliente(99)));

        AccesoDenegadoException exception = assertThrows(AccesoDenegadoException.class,
                () -> trazabilidadService.consultarTrazabilidad(50, CORREO_CLIENTE));

        assertEquals("El pedido no pertenece al cliente", exception.getMessage());
        verifyNoInteractions(pedidoEstadoLogRepository);
    }

    private Usuario cliente(Integer id) {
        return Usuario.builder()
                .id(id)
                .nombre("Cliente")
                .apellido("Prueba")
                .correo(CORREO_CLIENTE)
                .build();
    }

    private PedidoEstadoLog log(Integer id, Pedido pedido, EstadoPedido anterior,
                                EstadoPedido nuevo, Usuario empleado, LocalDateTime fecha) {
        return PedidoEstadoLog.builder()
                .id(id)
                .pedido(pedido)
                .estadoAnterior(anterior)
                .estadoNuevo(nuevo)
                .empleado(empleado)
                .fechaCambio(fecha)
                .build();
    }
}
