package com.plazoletaucc.Plazoleta.service;

// HU-14: envio del PIN de seguridad al cliente por mensaje SMS.
public interface SmsService {

    void enviarPedidoListo(String celular, String pin, Integer idPedido);
}
