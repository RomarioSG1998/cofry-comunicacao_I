package org.example.model;

/**
 * Enum representing boleto payment status.
 */
public enum BoletoStatus {
    OPEN,      // Em aberto (não vencido)
    OVERDUE,   // Vencido ou vencendo hoje
    PAID       // Pago
}

