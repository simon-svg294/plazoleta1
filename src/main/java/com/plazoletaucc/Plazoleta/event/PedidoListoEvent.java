package com.plazoletaucc.Plazoleta.event;

/**
 * HU-14: evento que se publica cuando un pedido pasa a LISTO.
 *
 * Se publica dentro de la transaccion y el listener lo procesa despues del commit,
 * para no enviar el SMS si la transaccion termina en rollback.
 */
public record PedidoListoEvent(String celular, String pin, Integer idPedido) {
}
