package com.plazoletaucc.Plazoleta.service.impl;

import com.plazoletaucc.Plazoleta.dto.reponse.RegistroResponseDTO;
import com.plazoletaucc.Plazoleta.dto.request.ClienteRequestDTO;
import com.plazoletaucc.Plazoleta.dto.request.EmpleadoRequestDTO;
import com.plazoletaucc.Plazoleta.dto.request.RegistroRequestDTO;
import com.plazoletaucc.Plazoleta.entity.Restaurante;
import com.plazoletaucc.Plazoleta.entity.Rol;
import com.plazoletaucc.Plazoleta.entity.Usuario;
import com.plazoletaucc.Plazoleta.exception.AccesoDenegadoException;
import com.plazoletaucc.Plazoleta.exception.CorreoDuplicadoException;
import com.plazoletaucc.Plazoleta.exception.DocumentoDuplicadoException;
import com.plazoletaucc.Plazoleta.exception.MenorDeEdadException;
import com.plazoletaucc.Plazoleta.exception.ReglaNegocioException;
import com.plazoletaucc.Plazoleta.repository.RestauranteRepository;
import com.plazoletaucc.Plazoleta.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del servicio de registro de usuarios (HU-01/HU-06/HU-08).
 * Es un requisito explicito del Sprint 3.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    private static final String CORREO_PROPIETARIO = "propietario@plazoleta.com";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RestauranteRepository restauranteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    // ---------------- HU-01: registrar propietario ----------------

    @Test
    void registrarUsuario_cuandoTodoEsValido_guardaPropietarioConClaveCifrada() {
        RegistroRequestDTO request = registroRequest();
        when(usuarioRepository.existsByCorreo(request.getCorreo())).thenReturn(false);
        when(usuarioRepository.existsByDocumentoDeIdentidad(request.getDocumentoDeIdentidad())).thenReturn(false);
        when(passwordEncoder.encode(request.getClave())).thenReturn("hash-bcrypt");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(10);
            return usuario;
        });

        RegistroResponseDTO response = usuarioService.registrarUsuario(request);

        assertAll(
                () -> assertEquals(10, response.getId()),
                () -> assertEquals("Propietario", response.getNombre()),
                () -> assertEquals("Prueba", response.getApellido()),
                () -> assertEquals("propietario@plazoleta.com", response.getCorreo()),
                () -> assertEquals("1000000001", response.getDocumentoDeIdentidad()),
                () -> assertEquals("+573001111111", response.getCelular())
        );

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertAll(
                () -> assertEquals(Rol.PROPIETARIO, guardado.getRol()),
                () -> assertEquals("hash-bcrypt", guardado.getClave())
        );
    }

    @Test
    void registrarUsuario_cuandoElCorreoYaExiste_lanzaCorreoDuplicado() {
        RegistroRequestDTO request = registroRequest();
        when(usuarioRepository.existsByCorreo(request.getCorreo())).thenReturn(true);

        CorreoDuplicadoException exception = assertThrows(CorreoDuplicadoException.class,
                () -> usuarioService.registrarUsuario(request));

        assertEquals("El correo ya existe", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void registrarUsuario_cuandoElDocumentoYaExiste_lanzaDocumentoDuplicado() {
        RegistroRequestDTO request = registroRequest();
        when(usuarioRepository.existsByCorreo(request.getCorreo())).thenReturn(false);
        when(usuarioRepository.existsByDocumentoDeIdentidad(request.getDocumentoDeIdentidad())).thenReturn(true);

        DocumentoDuplicadoException exception = assertThrows(DocumentoDuplicadoException.class,
                () -> usuarioService.registrarUsuario(request));

        assertEquals("El documento de identidad ya existe", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void registrarUsuario_cuandoEsMenorDeEdad_lanzaMenorDeEdad() {
        RegistroRequestDTO request = registroRequest();
        request.setFechaDeNacimiento(LocalDate.now().minusYears(17));
        when(usuarioRepository.existsByCorreo(request.getCorreo())).thenReturn(false);
        when(usuarioRepository.existsByDocumentoDeIdentidad(request.getDocumentoDeIdentidad())).thenReturn(false);

        MenorDeEdadException exception = assertThrows(MenorDeEdadException.class,
                () -> usuarioService.registrarUsuario(request));

        assertEquals("El usuario debe ser mayor de edad (18 anios o mas)", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // ---------------- HU-06: crear empleado ----------------

    @Test
    void crearEmpleado_cuandoElPropietarioEsDuenoDelRestaurante_guardaEmpleado() {
        EmpleadoRequestDTO request = empleadoRequest();
        Restaurante restaurante = restauranteDelPropietario();
        when(usuarioRepository.existsByCorreo(request.getCorreo())).thenReturn(false);
        when(usuarioRepository.existsByDocumentoDeIdentidad(request.getDocumentoDeIdentidad())).thenReturn(false);
        when(restauranteRepository.findById(10)).thenReturn(Optional.of(restaurante));
        when(passwordEncoder.encode(request.getClave())).thenReturn("hash-empleado");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(20);
            return usuario;
        });

        RegistroResponseDTO response = usuarioService.crearEmpleado(request, CORREO_PROPIETARIO);

        assertEquals(20, response.getId());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertAll(
                () -> assertEquals(Rol.EMPLEADO, guardado.getRol()),
                () -> assertEquals("hash-empleado", guardado.getClave()),
                () -> assertEquals(restaurante, guardado.getRestaurante())
        );
    }

    @Test
    void crearEmpleado_cuandoElDocumentoYaExiste_lanzaDocumentoDuplicado() {
        EmpleadoRequestDTO request = empleadoRequest();
        when(usuarioRepository.existsByCorreo(request.getCorreo())).thenReturn(false);
        when(usuarioRepository.existsByDocumentoDeIdentidad(request.getDocumentoDeIdentidad())).thenReturn(true);

        DocumentoDuplicadoException exception = assertThrows(DocumentoDuplicadoException.class,
                () -> usuarioService.crearEmpleado(request, CORREO_PROPIETARIO));

        assertEquals("El documento de identidad ya existe", exception.getMessage());
        verifyNoInteractions(restauranteRepository, passwordEncoder);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void crearEmpleado_cuandoElRestauranteNoExiste_lanzaReglaNegocio() {
        EmpleadoRequestDTO request = empleadoRequest();
        when(usuarioRepository.existsByCorreo(request.getCorreo())).thenReturn(false);
        when(usuarioRepository.existsByDocumentoDeIdentidad(request.getDocumentoDeIdentidad())).thenReturn(false);
        when(restauranteRepository.findById(10)).thenReturn(Optional.empty());

        ReglaNegocioException exception = assertThrows(ReglaNegocioException.class,
                () -> usuarioService.crearEmpleado(request, CORREO_PROPIETARIO));

        assertEquals("El restaurante no existe", exception.getMessage());
        verifyNoInteractions(passwordEncoder);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void crearEmpleado_cuandoElPropietarioNoEsDueno_lanzaAccesoDenegado() {
        EmpleadoRequestDTO request = empleadoRequest();
        Restaurante restaurante = restauranteDelPropietario();
        when(usuarioRepository.existsByCorreo(request.getCorreo())).thenReturn(false);
        when(usuarioRepository.existsByDocumentoDeIdentidad(request.getDocumentoDeIdentidad())).thenReturn(false);
        when(restauranteRepository.findById(10)).thenReturn(Optional.of(restaurante));

        AccesoDenegadoException exception = assertThrows(AccesoDenegadoException.class,
                () -> usuarioService.crearEmpleado(request, "otro@plazoleta.com"));

        assertEquals("Solo el propietario del restaurante puede crear sus empleados", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // ---------------- HU-08: registrar cliente ----------------

    @Test
    void registrarCliente_cuandoTodoEsValido_guardaClienteConClaveCifrada() {
        ClienteRequestDTO request = clienteRequest();
        when(usuarioRepository.existsByCorreo(request.getCorreo())).thenReturn(false);
        when(usuarioRepository.existsByDocumentoDeIdentidad(request.getDocumentoDeIdentidad())).thenReturn(false);
        when(passwordEncoder.encode(request.getClave())).thenReturn("hash-cliente");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(30);
            return usuario;
        });

        RegistroResponseDTO response = usuarioService.registrarCliente(request);

        assertEquals(30, response.getId());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertAll(
                () -> assertEquals(Rol.CLIENTE, guardado.getRol()),
                () -> assertEquals("hash-cliente", guardado.getClave()),
                () -> assertEquals("cliente@plazoleta.com", guardado.getCorreo())
        );
    }

    @Test
    void registrarCliente_cuandoElCorreoYaExiste_lanzaCorreoDuplicado() {
        ClienteRequestDTO request = clienteRequest();
        when(usuarioRepository.existsByCorreo(request.getCorreo())).thenReturn(true);

        CorreoDuplicadoException exception = assertThrows(CorreoDuplicadoException.class,
                () -> usuarioService.registrarCliente(request));

        assertEquals("El correo ya existe", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(passwordEncoder);
    }

    // ---------------- Utilidades ----------------

    private RegistroRequestDTO registroRequest() {
        RegistroRequestDTO dto = new RegistroRequestDTO();
        dto.setNombre("Propietario");
        dto.setApellido("Prueba");
        dto.setDocumentoDeIdentidad("1000000001");
        dto.setCelular("+573001111111");
        dto.setFechaDeNacimiento(LocalDate.of(1990, 1, 1));
        dto.setCorreo(CORREO_PROPIETARIO);
        dto.setClave("Clave123*");
        return dto;
    }

    private EmpleadoRequestDTO empleadoRequest() {
        EmpleadoRequestDTO dto = new EmpleadoRequestDTO();
        dto.setNombre("Empleado");
        dto.setApellido("Prueba");
        dto.setDocumentoDeIdentidad("2000000002");
        dto.setCelular("+573002222222");
        dto.setCorreo("empleado@plazoleta.com");
        dto.setIdRestaurante(10);
        dto.setClave("Clave123*");
        return dto;
    }

    private ClienteRequestDTO clienteRequest() {
        ClienteRequestDTO dto = new ClienteRequestDTO();
        dto.setNombre("Cliente");
        dto.setApellido("Prueba");
        dto.setDocumentoDeIdentidad("2000000003");
        dto.setCelular("+573003333333");
        dto.setCorreo("cliente@plazoleta.com");
        dto.setClave("Clave123*");
        return dto;
    }

    private Restaurante restauranteDelPropietario() {
        Usuario propietario = Usuario.builder()
                .id(1)
                .nombre("Propietario")
                .correo(CORREO_PROPIETARIO)
                .rol(Rol.PROPIETARIO)
                .build();
        return Restaurante.builder()
                .id(10)
                .nombre("Restaurante Demo")
                .propietario(propietario)
                .build();
    }
}
