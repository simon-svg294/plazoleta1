package com.plazoletaucc.Plazoleta.event;

import com.plazoletaucc.Plazoleta.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * HU-14: envia la notificacion al cliente solo cuando la transaccion del pedido
 * ya fue confirmada. Asi se evita notificar un pedido que termino en rollback.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoListoNotificacionListener {

    private final SmsService smsService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onPedidoListo(PedidoListoEvent event) {
        try {
            smsService.enviarPedidoListo(event.celular(), event.pin(), event.idPedido());
        } catch (RuntimeException ex) {
            // El pedido ya quedo LISTO; un fallo del proveedor SMS no debe romper la operacion.
            log.error("No se pudo enviar la notificacion del pedido {}", event.idPedido(), ex);
        }
    }
}
