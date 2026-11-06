package com.plazoletaucc.Plazoleta.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejo centralizado de errores de la API.
 *
 * Todas las respuestas conservan el formato {"mensaje": "..."} (o el mapa de
 * campos cuando falla la validacion de un DTO), de modo que el cliente siempre
 * recibe un error entendible y no un stack trace.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CorreoDuplicadoException.class)
    public ResponseEntity<Map<String, String>> handleCorreoDuplicado(CorreoDuplicadoException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(DocumentoDuplicadoException.class)
    public ResponseEntity<Map<String, String>> handleDocumentoDuplicado(DocumentoDuplicadoException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(NitDuplicadoException.class)
    public ResponseEntity<Map<String, String>> handleNitDuplicado(NitDuplicadoException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(PropietarioNoValidoException.class)
    public ResponseEntity<Map<String, String>> handlePropietarioNoValido(PropietarioNoValidoException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Regla de negocio no cumplida (restaurante/categoria no existe, estado invalido, etc.) -> 400
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, String>> handleReglaNegocio(ReglaNegocioException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Logueado pero sin permisos sobre el recurso -> 403
    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<Map<String, String>> handleAccesoDenegado(AccesoDenegadoException ex) {
        return error(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // Errores de formato del DTO (@NotBlank, @Positive...) -> 400 con detalle por campo
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errores.put(e.getField(), e.getDefaultMessage()));
        ex.getBindingResult().getGlobalErrors()
                .forEach(e -> errores.put(e.getObjectName(), e.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }

    // HU-05: credenciales invalidas -> 401
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Map<String, String>> handleCredenciales(CredencialesInvalidasException ex) {
        return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(MenorDeEdadException.class)
    public ResponseEntity<Map<String, String>> handleMenorDeEdad(MenorDeEdadException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // HU-07: recurso no encontrado -> 404 manteniendo el formato {"mensaje": "..."}
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException ex) {
        String mensaje = ex.getReason() == null ? "Error en la peticion" : ex.getReason();
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("mensaje", mensaje));
    }

    // Parametros con formato invalido (por ejemplo estado=NO_EXISTE) -> 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return error(HttpStatus.BAD_REQUEST,
                "El parametro '" + ex.getName() + "' tiene un valor invalido");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleMissingParameter(MissingServletRequestParameterException ex) {
        return error(HttpStatus.BAD_REQUEST,
                "Falta el parametro obligatorio '" + ex.getParameterName() + "'");
    }

    // JSON malformado o cuerpo vacio -> 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleNotReadable(HttpMessageNotReadableException ex) {
        return error(HttpStatus.BAD_REQUEST, "El cuerpo de la peticion no es un JSON valido");
    }

    // Violacion de una restriccion de base de datos (unique, foreign key, etc.) -> 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Conflicto de integridad de datos", ex);
        return error(HttpStatus.CONFLICT,
                "No se pudo completar la operacion porque un dato entra en conflicto con otro existente");
    }

    // Dos peticiones intentaron cambiar el mismo pedido al mismo tiempo -> 409
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        return error(HttpStatus.CONFLICT,
                "El recurso fue modificado por otra operacion. Actualiza los datos e intenta de nuevo");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Ruta inexistente -> 404 (evita que el manejador generico la convierta en 500)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoResource(NoResourceFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "El recurso solicitado no existe");
    }

    // Cualquier error no previsto: se registra y se responde sin exponer detalles internos.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        log.error("Error no controlado", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrio un error interno. Contacta al administrador si el problema persiste");
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String mensaje) {
        String detalle = (mensaje == null || mensaje.isBlank())
                ? "La operacion no pudo completarse"
                : mensaje;
        return ResponseEntity.status(status).body(Map.of("mensaje", detalle));
    }
}
