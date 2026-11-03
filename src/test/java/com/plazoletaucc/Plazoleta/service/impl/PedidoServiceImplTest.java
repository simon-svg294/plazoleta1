package com.plazoletaucc.Plazoleta.service.impl;

import com.plazoletaucc.Plazoleta.dto.reponse.PaginaResponseDTO;
import com.plazoletaucc.Plazoleta.dto.reponse.PedidoListadoResponseDTO;
import com.plazoletaucc.Plazoleta.dto.reponse.PedidoListoResponseDTO;
import com.plazoletaucc.Plazoleta.dto.reponse.PedidoResponseDTO;
import com.plazoletaucc.Plazoleta.dto.request.DetallePedidoRequestDTO;
import com.plazoletaucc.Plazoleta.dto.request.PedidoRequestDTO;
import com.plazoletaucc.Plazoleta.entity.DetallePedido;
import com.plazoletaucc.Plazoleta.entity.EstadoPedido;
import com.plazoletaucc.Plazoleta.entity.Pedido;
import com.plazoletaucc.Plazoleta.entity.Plato;
import com.plazoletaucc.Plazoleta.entity.Restaurante;
import com.plazoletaucc.Plazoleta.entity.Rol;
import com.plazoletaucc.Plazoleta.entity.Usuario;
import com.plazoletaucc.Plazoleta.exception.AccesoDenegadoException;
import com.plazoletaucc.Plazoleta.exception.ReglaNegocioException;
import com.plazoletaucc.Plazoleta.repository.PedidoRepository;
import com.plazoletaucc.Plazoleta.repository.PlatoRespository;
import com.plazoletaucc.Plazoleta.repository.RestauranteRepository;
import com.plazoletaucc.Plazoleta.repository.UsuarioRepository;
import com.plazoletaucc.Plazoleta.service.SmsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
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
    private static final String CORREO_EMPLEADO = "empleado@plazoleta.com";

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RestauranteRepository restauranteRepository;

    @Mock
    private PlatoRespository platoRespository;

    @Mock
    private SmsService smsService;

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

    // ---------------- HU-12: listar pedidos filtrando por estado ----------------

    @Test
    void listarPedidosPorEstado_cuandoEmpleadoTieneRestaurante_devuelveSusPedidosPaginados() {
        Restaurante restaurante = restaurante(10);
        Usuario empleado = empleado(restaurante);
        Pedido pedido = pedido(50, EstadoPedido.PENDIENTE, restaurante);

        when(usuarioRepository.findByCorreo(CORREO_EMPLEADO)).thenReturn(Optional.of(empleado));
        when(pedidoRepository.findByRestaurante_IdAndEstado(eq(10), eq(EstadoPedido.PENDIENTE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(pedido), PageRequest.of(0, 10), 1));

        PaginaResponseDTO<PedidoListadoResponseDTO> respuesta =
                pedidoService.listarPedidosPorEstado(EstadoPedido.PENDIENTE, 0, 10, CORREO_EMPLEADO);

        assertAll(
                () -> assertEquals(1, respuesta.getContenido().size()),
                () -> assertEquals(0, respuesta.getPagina()),
                () -> assertEquals(10, respuesta.getTamano()),
                () -> assertEquals(1L, respuesta.getTotalElementos()),
                () -> assertEquals(1, respuesta.getTotalPaginas())
        );

        PedidoListadoResponseDTO dto = respuesta.getContenido().get(0);
        assertAll(
                () -> assertEquals(50, dto.getId()),
                () -> assertEquals(1, dto.getIdCliente()),
                () -> assertEquals("Cliente Prueba", dto.getNombreCliente()),
                () -> assertEquals(10, dto.getIdRestaurante()),
                () -> assertEquals("Restaurante 10", dto.getNombreRestaurante()),
                () -> assertEquals(EstadoPedido.PENDIENTE, dto.getEstado()),
                () -> assertEquals(40000, dto.getTotal()),
                () -> assertNotNull(dto.getFechaCreacion()),
                () -> assertEquals(1, dto.getPlatos().size()),
                () -> assertEquals(2, dto.getPlatos().get(0).getCantidad())
        );

        verify(pedidoRepository).findByRestaurante_IdAndEstado(eq(10), eq(EstadoPedido.PENDIENTE), any(Pageable.class));
    }

    @Test
    void listarPedidosPorEstado_cuandoEmpleadoNoTieneRestaurante_lanzaAccesoDenegado() {
        when(usuarioRepository.findByCorreo(CORREO_EMPLEADO)).thenReturn(Optional.of(empleado(null)));

        AccesoDenegadoException exception = assertThrows(AccesoDenegadoException.class,
                () -> pedidoService.listarPedidosPorEstado(EstadoPedido.PENDIENTE, 0, 10, CORREO_EMPLEADO));

        assertEquals("El empleado no tiene un restaurante asignado", exception.getMessage());
        verifyNoInteractions(pedidoRepository);
    }

    @Test
    void listarPedidosPorEstado_cuandoPaginaEsNegativa_lanzaReglaNegocio() {
        ReglaNegocioException exception = assertThrows(ReglaNegocioException.class,
                () -> pedidoService.listarPedidosPorEstado(EstadoPedido.PENDIENTE, -1, 10, CORREO_EMPLEADO));

        assertEquals("La pagina debe ser 0 o mayor", exception.getMessage());
        verifyNoInteractions(usuarioRepository, pedidoRepository);
    }

    @Test
    void listarPedidosPorEstado_cuandoTamanoEsCero_lanzaReglaNegocio() {
        ReglaNegocioException exception = assertThrows(ReglaNegocioException.class,
                () -> pedidoService.listarPedidosPorEstado(EstadoPedido.PENDIENTE, 0, 0, CORREO_EMPLEADO));

        assertEquals("El tamano de pagina debe ser 1 o mayor", exception.getMessage());
        verifyNoInteractions(usuarioRepository, pedidoRepository);
    }

    // ---------------- HU-13: asignarse a un pedido ----------------

    @Test
    void asignarPedido_cuandoEstaPendienteYEsDelRestaurante_loAsignaYQuedaEnPreparacion() {
        Restaurante restaurante = restaurante(10);
        Usuario empleado = empleado(restaurante);
        Pedido pedido = pedido(50, EstadoPedido.PENDIENTE, restaurante);

        when(pedidoRepository.findById(50)).thenReturn(Optional.of(pedido));
        when(usuarioRepository.findByCorreo(CORREO_EMPLEADO)).thenReturn(Optional.of(empleado));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PedidoResponseDTO response = pedidoService.asignarPedido(50, CORREO_EMPLEADO);

        assertAll(
                () -> assertEquals(50, response.getId()),
                () -> assertEquals(EstadoPedido.EN_PREPARACION, response.getEstado()),
                () -> assertEquals(2, response.getIdEmpleadoAsignado())
        );

        assertAll(
                () -> assertEquals(empleado, pedido.getEmpleado()),
                () -> assertEquals(EstadoPedido.EN_PREPARACION, pedido.getEstado())
        );
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void asignarPedido_cuandoEsDeOtroRestaurante_lanzaAccesoDenegado() {
        Pedido pedido = pedido(50, EstadoPedido.PENDIENTE, restaurante(20));

        when(pedidoRepository.findById(50)).thenReturn(Optional.of(pedido));
        when(usuarioRepository.findByCorreo(CORREO_EMPLEADO))
                .thenReturn(Optional.of(empleado(restaurante(10))));

        AccesoDenegadoException exception = assertThrows(AccesoDenegadoException.class,
                () -> pedidoService.asignarPedido(50, CORREO_EMPLEADO));

        assertEquals("El pedido no pertenece al restaurante del empleado", exception.getMessage());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void asignarPedido_cuandoNoEstaPendiente_lanzaReglaNegocio() {
        Restaurante restaurante = restaurante(10);
        Pedido pedido = pedido(50, EstadoPedido.EN_PREPARACION, restaurante);

        when(pedidoRepository.findById(50)).thenReturn(Optional.of(pedido));
        when(usuarioRepository.findByCorreo(CORREO_EMPLEADO))
                .thenReturn(Optional.of(empleado(restaurante)));

        ReglaNegocioException exception = assertThrows(ReglaNegocioException.class,
                () -> pedidoService.asignarPedido(50, CORREO_EMPLEADO));

        assertEquals("Solo se pueden asignar pedidos en estado PENDIENTE", exception.getMessage());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void asignarPedido_cuandoNoExiste_lanzaNotFound() {
        when(pedidoRepository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> pedidoService.asignarPedido(99, CORREO_EMPLEADO));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("El pedido no existe", exception.getReason());
        verifyNoInteractions(usuarioRepository);
    }

    // ---------------- HU-14: notificar pedido listo ----------------

    @Test
    void marcarPedidoListo_cuandoEstaEnPreparacion_generaPinYEnviaSms() {
        Restaurante restaurante = restaurante(10);
        Usuario empleado = empleado(restaurante);
        Pedido pedido = pedido(50, EstadoPedido.EN_PREPARACION, restaurante);

        when(pedidoRepository.findById(50)).thenReturn(Optional.of(pedido));
        when(usuarioRepository.findByCorreo(CORREO_EMPLEADO)).thenReturn(Optional.of(empleado));

        PedidoListoResponseDTO response = pedidoService.marcarPedidoListo(50, CORREO_EMPLEADO);

        assertAll(
                () -> assertEquals(50, response.getId()),
                () -> assertEquals(EstadoPedido.LISTO, response.getEstado()),
                () -> assertNotNull(response.getPin()),
                () -> assertEquals(4, response.getPin().length())
        );

        assertAll(
                () -> assertEquals(EstadoPedido.LISTO, pedido.getEstado()),
                () -> assertEquals(response.getPin(), pedido.getPin())
        );

        verify(pedidoRepository).save(pedido);
        verify(smsService).enviarPedidoListo("+573001111111", response.getPin(), 50);
    }

    @Test
    void marcarPedidoListo_cuandoNoEstaEnPreparacion_lanzaReglaNegocio() {
        Restaurante restaurante = restaurante(10);
        Pedido pedido = pedido(50, EstadoPedido.PENDIENTE, restaurante);

        when(pedidoRepository.findById(50)).thenReturn(Optional.of(pedido));
        when(usuarioRepository.findByCorreo(CORREO_EMPLEADO))
                .thenReturn(Optional.of(empleado(restaurante)));

        ReglaNegocioException exception = assertThrows(ReglaNegocioException.class,
                () -> pedidoService.marcarPedidoListo(50, CORREO_EMPLEADO));

        assertEquals("Solo se puede marcar como LISTO un pedido en estado EN_PREPARACION",
                exception.getMessage());
        verify(pedidoRepository, never()).save(any(Pedido.class));
        verifyNoInteractions(smsService);
    }

    @Test
    void marcarPedidoListo_cuandoEsDeOtroRestaurante_lanzaAccesoDenegado() {
        Pedido pedido = pedido(50, EstadoPedido.EN_PREPARACION, restaurante(20));

        when(pedidoRepository.findById(50)).thenReturn(Optional.of(pedido));
        when(usuarioRepository.findByCorreo(CORREO_EMPLEADO))
                .thenReturn(Optional.of(empleado(restaurante(10))));

        AccesoDenegadoException exception = assertThrows(AccesoDenegadoException.class,
                () -> pedidoService.marcarPedidoListo(50, CORREO_EMPLEADO));

        assertEquals("El pedido no pertenece al restaurante del empleado", exception.getMessage());
        verifyNoInteractions(smsService);
    }

    @Test
    void marcarPedidoListo_cuandoNoExiste_lanzaNotFound() {
        when(pedidoRepository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> pedidoService.marcarPedidoListo(99, CORREO_EMPLEADO));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("El pedido no existe", exception.getReason());
        verifyNoInteractions(smsService, usuarioRepository);
    }

    // ---------------- HU-15: entregar pedido con PIN ----------------

    @Test
    void entregarPedido_cuandoEstaListoYElPinEsCorrecto_quedaEntregado() {
        Restaurante restaurante = restaurante(10);
        Usuario empleado = empleado(restaurante);
        Pedido pedido = pedido(50, EstadoPedido.LISTO, restaurante);
        pedido.setPin("1234");
        pedido.setEmpleado(empleado);

        when(pedidoRepository.findById(50)).thenReturn(Optional.of(pedido));
        when(usuarioRepository.findByCorreo(CORREO_EMPLEADO)).thenReturn(Optional.of(empleado));
        when(pedidoRepository.save(any(Pedido.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PedidoResponseDTO response = pedidoService.entregarPedido(50, "1234", CORREO_EMPLEADO);

        assertAll(
                () -> assertEquals(50, response.getId()),
                () -> assertEquals(EstadoPedido.ENTREGADO, response.getEstado()),
                () -> assertEquals(2, response.getIdEmpleadoAsignado())
        );
        assertEquals(EstadoPedido.ENTREGADO, pedido.getEstado());
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void entregarPedido_cuandoElPinEsIncorrecto_lanzaReglaNegocio() {
        Restaurante restaurante = restaurante(10);
        Pedido pedido = pedido(50, EstadoPedido.LISTO, restaurante);
        pedido.setPin("1234");

        when(pedidoRepository.findById(50)).thenReturn(Optional.of(pedido));
        when(usuarioRepository.findByCorreo(CORREO_EMPLEADO))
                .thenReturn(Optional.of(empleado(restaurante)));

        ReglaNegocioException exception = assertThrows(ReglaNegocioException.class,
                () -> pedidoService.entregarPedido(50, "0000", CORREO_EMPLEADO));

        assertEquals("El PIN de seguridad no es correcto", exception.getMessage());
        assertEquals(EstadoPedido.LISTO, pedido.getEstado());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void entregarPedido_cuandoElPinEsNulo_lanzaReglaNegocio() {
        Restaurante restaurante = restaurante(10);
        Pedido pedido = pedido(50, EstadoPedido.LISTO, restaurante);
        pedido.setPin("1234");

        when(pedidoRepository.findById(50)).thenReturn(Optional.of(pedido));
        when(usuarioRepository.findByCorreo(CORREO_EMPLEADO))
                .thenReturn(Optional.of(empleado(restaurante)));

        ReglaNegocioException exception = assertThrows(ReglaNegocioException.class,
                () -> pedidoService.entregarPedido(50, null, CORREO_EMPLEADO));

        assertEquals("El PIN de seguridad no es correcto", exception.getMessage());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void entregarPedido_cuandoNoEstaListo_lanzaReglaNegocio() {
        Restaurante restaurante = restaurante(10);
        Pedido pedido = pedido(50, EstadoPedido.EN_PREPARACION, restaurante);
        pedido.setPin("1234");

        when(pedidoRepository.findById(50)).thenReturn(Optional.of(pedido));
        when(usuarioRepository.findByCorreo(CORREO_EMPLEADO))
                .thenReturn(Optional.of(empleado(restaurante)));

        ReglaNegocioException exception = assertThrows(ReglaNegocioException.class,
                () -> pedidoService.entregarPedido(50, "1234", CORREO_EMPLEADO));

        assertEquals("Solo se puede entregar un pedido en estado LISTO", exception.getMessage());
        assertEquals(EstadoPedido.EN_PREPARACION, pedido.getEstado());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void entregarPedido_cuandoYaEstaEntregado_lanzaReglaNegocio() {
        Restaurante restaurante = restaurante(10);
        Pedido pedido = pedido(50, EstadoPedido.ENTREGADO, restaurante);
        pedido.setPin("1234");

        when(pedidoRepository.findById(50)).thenReturn(Optional.of(pedido));
        when(usuarioRepository.findByCorreo(CORREO_EMPLEADO))
                .thenReturn(Optional.of(empleado(restaurante)));

        ReglaNegocioException exception = assertThrows(ReglaNegocioException.class,
                () -> pedidoService.entregarPedido(50, "1234", CORREO_EMPLEADO));

        assertEquals("El pedido ya esta ENTREGADO", exception.getMessage());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void entregarPedido_cuandoEsDeOtroRestaurante_lanzaAccesoDenegado() {
        Pedido pedido = pedido(50, EstadoPedido.LISTO, restaurante(20));
        pedido.setPin("1234");

        when(pedidoRepository.findById(50)).thenReturn(Optional.of(pedido));
        when(usuarioRepository.findByCorreo(CORREO_EMPLEADO))
                .thenReturn(Optional.of(empleado(restaurante(10))));

        AccesoDenegadoException exception = assertThrows(AccesoDenegadoException.class,
                () -> pedidoService.entregarPedido(50, "1234", CORREO_EMPLEADO));

        assertEquals("El pedido no pertenece al restaurante del empleado", exception.getMessage());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void entregarPedido_cuandoNoExiste_lanzaNotFound() {
        when(pedidoRepository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> pedidoService.entregarPedido(99, "1234", CORREO_EMPLEADO));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("El pedido no existe", exception.getReason());
        verifyNoInteractions(usuarioRepository);
    }

    private Usuario cliente() {
        return Usuario.builder()
                .id(1)
                .nombre("Cliente")
                .apellido("Prueba")
                .correo(CORREO_CLIENTE)
                .celular("+573001111111")
                .rol(Rol.CLIENTE)
                .build();
    }

    private Usuario empleado(Restaurante restaurante) {
        return Usuario.builder()
                .id(2)
                .nombre("Empleado")
                .apellido("Prueba")
                .correo(CORREO_EMPLEADO)
                .rol(Rol.EMPLEADO)
                .restaurante(restaurante)
                .build();
    }

    private Pedido pedido(Integer id, EstadoPedido estado, Restaurante restaurante) {
        Plato plato = plato(100, 20000, true, restaurante);

        Pedido pedido = Pedido.builder()
                .id(id)
                .cliente(cliente())
                .restaurante(restaurante)
                .estado(estado)
                .fechaCreacion(LocalDateTime.of(2026, 10, 31, 10, 15, 30))
                .total(40000)
                .build();

        pedido.agregarDetalle(DetallePedido.builder()
                .plato(plato)
                .cantidad(2)
                .precioUnitario(20000)
                .build());

        return pedido;
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
