package org.example.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "boletos")
public class Boleto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "boleto_id")
    private Long id;
    
    @Column(name = "title", nullable = false, length = 255)
    private String title; // Nome da cobrança
    
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BoletoStatus status;
    
    @Column(name = "bank_code", nullable = false, length = 3)
    private String bankCode; // Código FEBRABAN do banco (ex: 001, 033)
    
    @Column(name = "wallet_code", nullable = false, length = 5)
    private String walletCode; // Código da carteira de cobrança
    
    @Column(name = "our_number", nullable = false, length = 23)
    private String ourNumber; // Nosso número completo (23 dígitos)
    
    @Column(name = "boleto_code", nullable = false, length = 48)
    private String boletoCode; // Linha digitável (48 dígitos)
    
    @Column(name = "user_id")
    private Integer userId; // Opcional: usuário associado ao boleto
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt; // Data/hora do pagamento
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Boleto() {
        this.status = BoletoStatus.OPEN;
    }

    public Boleto(String title, BigDecimal amount, LocalDate dueDate, String bankCode, 
                  String walletCode, String ourNumber) {
        this.title = title;
        this.amount = amount;
        this.dueDate = dueDate;
        this.bankCode = bankCode;
        this.walletCode = walletCode;
        this.ourNumber = ourNumber;
        this.status = BoletoStatus.OPEN;
    }
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        // Atualiza status baseado na data de vencimento
        updateStatusBasedOnDueDate();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // Atualiza status baseado na data de vencimento
        updateStatusBasedOnDueDate();
    }
    
    /**
     * Atualiza o status do boleto baseado na data de vencimento.
     * Se já foi pago, mantém PAID.
     */
    private void updateStatusBasedOnDueDate() {
        if (this.status == BoletoStatus.PAID) {
            return; // Se já está pago, não muda
        }
        
        LocalDate today = LocalDate.now();
        if (this.dueDate != null) {
            if (this.dueDate.isBefore(today) || this.dueDate.isEqual(today)) {
                this.status = BoletoStatus.OVERDUE;
            } else {
                this.status = BoletoStatus.OPEN;
            }
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        updateStatusBasedOnDueDate(); // Atualiza status quando a data muda
    }

    public BoletoStatus getStatus() {
        return status;
    }

    public void setStatus(BoletoStatus status) {
        this.status = status;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getWalletCode() {
        return walletCode;
    }

    public void setWalletCode(String walletCode) {
        this.walletCode = walletCode;
    }

    public String getOurNumber() {
        return ourNumber;
    }

    public void setOurNumber(String ourNumber) {
        this.ourNumber = ourNumber;
    }

    public String getBoletoCode() {
        return boletoCode;
    }

    public void setBoletoCode(String boletoCode) {
        this.boletoCode = boletoCode;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

