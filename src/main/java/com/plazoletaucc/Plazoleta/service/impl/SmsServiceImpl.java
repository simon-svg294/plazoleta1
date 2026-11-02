package com.plazoletaucc.Plazoleta.service.impl;

import com.plazoletaucc.Plazoleta.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    // HU-14: integracion simulada. Para produccion se reemplaza por un proveedor
    // real (por ejemplo Twilio) implementando esta misma interfaz.
    @Override
    public void enviarPedidoListo(String celular, String pin, Integer idPedido) {
        log.info("SMS a {}: su pedido #{} esta listo. PIN de seguridad para reclamarlo: {}",
                celular, idPedido, pin);
    }
}
