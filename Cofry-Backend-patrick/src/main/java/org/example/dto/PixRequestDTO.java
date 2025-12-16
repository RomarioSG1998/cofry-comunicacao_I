package org.example.dto;

/**
 * DTO for PIX transfer requests from front-end.
 */
public class PixRequestDTO {
    private Integer sourceAccountId; // Conta de origem (quem envia)
    private Integer destinationAccountId; // Conta de destino (quem recebe) - opcional, pode usar destinationUserId
    private Integer destinationUserId; // ID do usuário de destino (alternativa a destinationAccountId)
    private String amount; // Valor da transferência
    private String description; // Descrição/observação
    
    // Constructors
    public PixRequestDTO() {
    }
    
    // Getters and Setters
    public Integer getSourceAccountId() {
        return sourceAccountId;
    }
    
    public void setSourceAccountId(Integer sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }
    
    public Integer getDestinationAccountId() {
        return destinationAccountId;
    }
    
    public void setDestinationAccountId(Integer destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }
    
    public Integer getDestinationUserId() {
        return destinationUserId;
    }
    
    public void setDestinationUserId(Integer destinationUserId) {
        this.destinationUserId = destinationUserId;
    }
    
    public String getAmount() {
        return amount;
    }
    
    public void setAmount(String amount) {
        this.amount = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}

