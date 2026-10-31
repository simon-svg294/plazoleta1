package com.plazoletaucc.Plazoleta.service.impl;

import com.plazoletaucc.Plazoleta.dto.reponse.PedidoResponseDTO;
import com.plazoletaucc.Plazoleta.dto.request.DetallePedidoRequestDTO;
import com.plazoletaucc.Plazoleta.dto.request.PedidoRequestDTO;
import com.plazoletaucc.Plazoleta.entity.EstadoPedido;
import com.plazoletaucc.Plazoleta.entity.Pedido;
import com.plazoletaucc.Plazoleta.entity.Plato;
import com.plazoletaucc.Plazoleta.entity.Restaurante;
import com.plazoletaucc.Plazoleta.entity.Rol;
import com.plazoletaucc.Plazoleta.entity.Usuario;
import com.plazoletaucc.Plazoleta.exception.ReglaNegocioException;
import com.plazoletaucc.Plazoleta.repository.PedidoRepository;
import com.plazoletaucc.Plazoleta.repository.PlatoRespository;
import com.plazoletaucc.Plazoleta.repository.RestauranteRepository;
import com.plazoletaucc.Plazoleta.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceImplTest {

    private static final String CORREO_CLIENTE = "cliente@plazoleta.com";

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RestauranteRepository restauranteRepository;

    @Mock
    private PlatoRespository platoRespository;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    @Test
    void crearPedido_cuandoTodoEsValido_creaPedidoPendiente() {
        Usuario cliente = cliente();
        Restaurante restaurante = restaurante(10);
        Plato hamburguesa = plato(100, 20000, true, restaurante);
        Plato gaseosa = plato(101, 5000, true, restaurante);
        PedidoRequestDTO request = request(10, item(100, 2), item(101, 1));

        when(usuarioRepository.findByCorreoForUpdate(CORREO_CLIENTE)).thenReturn(Optional.of(cliente));
        when(pedidoRepository.existsByCliente_IdAndEstadoIn(eq(1), anyCollection())).thenReturn(false);
        when(restauranteRepository.findById(10)).thenReturn(Optional.of(restaurante));
        when(platoRespository.findAllById(List.of(100, 101))).thenReturn(List.of(hamburguesa, gaseosa));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            pedido.setId(50);
            return pedido;
        });

        PedidoResponseDTO response = pedidoService.crearPedido(request, CORREO_CLIENTE);

        assertAll(
                () -> assertEquals(50, response.getId()),
                () -> assertEquals(EstadoPedido.PENDIENTE, response.getEstado()),
                () -> assertEquals(10, response.getIdRestaurante()),
                () -> assertEquals("Restaurante 10", response.getNombreRestaurante()),
                () -> assertEquals(45000, response.getTotal()),
                () -> assertEquals(2, response.getPlatos().size()),
                () -> assertNotNull(response.getFechaCreacion())
        );

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(captor.capture());

        Pedido guardado = captor.getValue();
        assertAll(
                () -> assertEquals(EstadoPedido.PENDIENTE, guardado.getEstado()),
                () -> assertEquals(45000, guardado.getTotal()),
                () -> assertEquals(2, guardado.getDetalles().size()),
                () -> assertEquals(20000, guardado.getDetalles().get(0).getPrecioUnitario()),
                () -> assertEquals(1, guardado.getDetalles().get(1).getCantidad())
        );

        verify(pedidoRepository).existsByCliente_IdAndEstadoIn(eq(1), argThat(estados ->
                estados.size() == 3
                        && estados.containsAll(List.of(
                        EstadoPedido.PENDIENTE,
                        EstadoPedido.EN_PREPARACION,
                        EstadoPedido.LISTO
                ))
        ));
    }

    @Test
    void crearPedido_cuandoClienteTienePedidoEnProceso_lanzaReglaNegocio() {
        when(usuarioRepository.findByCorreoForUpdate(CORREO_CLIENTE))
                .thenReturn(Optional.of(cliente()));
        when(pedidoRepository.existsByCliente_IdAndEstadoIn(eq(1), anyCollection()))
                .thenReturn(true);

        ReglaNegocioException exception = assertThrows(ReglaNegocioException.class,
                () -> pedidoService.crearPedido(request(10, item(100, 1)), CORREO_CLIENTE));

        assertEquals("Ya tienes un pedido en proceso", exception.getMessage());
        verify(pedidoRepository, never()).save(any(Pedido.class));
        verifyNoInteractions(restauranteRepository, platoRespository);
    }

    @Test
    void crearPedido_cuandoPlatoEsDeOtroRestaurante_lanzaReglaNegocio() {
        Restaurante restauranteDelPedido = restaurante(10);
        Plato platoDeOtroRestaurante = plato(100, 20000, true, restaurante(20));

        when(usuarioRepository.findByCorreoForUpdate(CORREO_CLIENTE))
                .thenReturn(Optional.of(cliente()));
        when(pedidoRepository.existsByCliente_IdAndEstadoIn(eq(1), anyCollection()))
                .thenReturn(false);
        when(restauranteRepository.findById(10))
                .thenReturn(Optional.of(restauranteDelPedido));
        when(platoRespository.findAllById(anyList()))
                .thenReturn(List.of(platoDeOtroRestaurante));

        ReglaNegocioException exception = assertThrows(ReglaNegocioException.class,
                () -> pedidoService.crearPedido(request(10, item(100, 1)), CORREO_CLIENTE));

        assertEquals("Todos los platos deben ser del mismo restaurante", exception.getMessage());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void crearPedido_cuandoPlatoNoEstaActivo_lanzaReglaNegocio() {
        Restaurante restaurante = restaurante(10);
        Plato platoInactivo = plato(100, 20000, false, restaurante);

        when(usuarioRepository.findByCorreoForUpdate(CORREO_CLIENTE))
                .thenReturn(Optional.of(cliente()));
        when(pedidoRepository.existsByCliente_IdAndEstadoIn(eq(1), anyCollection()))
                .thenReturn(false);
        when(restauranteRepository.findById(10))
                .thenReturn(Optional.of(restaurante));
        when(platoRespository.findAllById(anyList()))
                .thenReturn(List.of(platoInactivo));

        ReglaNegocioException exception = assertThrows(ReglaNegocioException.class,
                () -> pedidoService.crearPedido(request(10, item(100, 1)), CORREO_CLIENTE));

        assertEquals("El plato 'Plato 100' no esta activo", exception.getMessage());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void crearPedido_cuandoPlatoNoExiste_lanzaNotFound() {
        when(usuarioRepository.findByCorreoForUpdate(CORREO_CLIENTE))
                .thenReturn(Optional.of(cliente()));
        when(pedidoRepository.existsByCliente_IdAndEstadoIn(eq(1), anyCollection()))
                .thenReturn(false);
        when(restauranteRepository.findById(10))
                .thenReturn(Optional.of(restaurante(10)));
        when(platoRespository.findAllById(anyList()))
                .thenReturn(List.of());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> pedidoService.crearPedido(request(10, item(100, 1)), CORREO_CLIENTE));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Uno o mas platos no existen", exception.getReason());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void crearPedido_cuandoPlatoEstaRepetido_lanzaReglaNegocio() {
        when(usuarioRepository.findByCorreoForUpdate(CORREO_CLIENTE))
                .thenReturn(Optional.of(cliente()));
        when(pedidoRepository.existsByCliente_IdAndEstadoIn(eq(1), anyCollection()))
                .thenReturn(false);
        when(restauranteRepository.findById(10))
                .thenReturn(Optional.of(restaurante(10)));

        ReglaNegocioException exception = assertThrows(ReglaNegocioException.class,
                () -> pedidoService.crearPedido(
                        request(10, item(100, 1), item(100, 2)),
                        CORREO_CLIENTE));

        assertEquals("No se puede repetir un plato en el pedido", exception.getMessage());
        verifyNoInteractions(platoRespository);
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    private Usuario cliente() {
        return Usuario.builder()
                .id(1)
                .correo(CORREO_CLIENTE)
                .rol(Rol.CLIENTE)
                .build();
    }

    private Restaurante restaurante(Integer id) {
        return Restaurante.builder()
                .id(id)
                .nombre("Restaurante " + id)
                .build();
    }

    private Plato plato(Integer id, Integer precio, Boolean estado, Restaurante restaurante) {
        return Plato.builder()
                .id(id)
                .nombre("Plato " + id)
                .precio(precio)
                .estado(estado)
                .restaurante(restaurante)
                .build();
    }

    private PedidoRequestDTO request(Integer idRestaurante, DetallePedidoRequestDTO... platos) {
        PedidoRequestDTO dto = new PedidoRequestDTO();
        dto.setIdRestaurante(idRestaurante);
        dto.setPlatos(List.of(platos));
        return dto;
    }

    private DetallePedidoRequestDTO item(Integer idPlato, Integer cantidad) {
        DetallePedidoRequestDTO dto = new DetallePedidoRequestDTO();
        dto.setIdPlato(idPlato);
        dto.setCantidad(cantidad);
        return dto;
    }
}
