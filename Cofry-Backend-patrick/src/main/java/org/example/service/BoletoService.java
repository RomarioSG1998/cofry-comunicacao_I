package org.example.service;

import org.example.dao.BoletoDAO;
import org.example.model.Boleto;
import org.example.model.BoletoStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service para gerenciar boletos bancários.
 * Responsável pela criação, atualização e listagem de boletos.
 */
public class BoletoService {
    
    private final BoletoDAO boletoDAO;
    private final BoletoCodeService boletoCodeService;
    
    public BoletoService() {
        this.boletoDAO = new BoletoDAO();
        this.boletoCodeService = new BoletoCodeService();
    }
    
    /**
     * Cria um novo boleto.
     * Gera automaticamente o código do boleto e define o status baseado na data de vencimento.
     * 
     * @param title Nome da cobrança
     * @param amount Valor do boleto
     * @param dueDate Data de vencimento
     * @param bankCode Código do banco (3 dígitos)
     * @param walletCode Código da carteira (5 dígitos)
     * @param ourNumber Nosso número (até 23 dígitos)
     * @param userId ID do usuário (opcional)
     * @return Boleto criado
     */
    public Boleto createBoleto(String title, BigDecimal amount, LocalDate dueDate, 
                               String bankCode, String walletCode, String ourNumber, Integer userId) {
        // Validações
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Título do boleto é obrigatório");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do boleto deve ser maior que zero");
        }
        if (dueDate == null) {
            throw new IllegalArgumentException("Data de vencimento é obrigatória");
        }
        if (bankCode == null || bankCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Código do banco é obrigatório");
        }
        if (walletCode == null || walletCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Código da carteira é obrigatório");
        }
        if (ourNumber == null || ourNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Nosso número é obrigatório");
        }
        
        // Verifica se o usuário existe (se fornecido)
        if (userId != null) {
            UserService userService = new UserService();
            userService.getUserById(userId); // Lança exceção se não existir
        }
        
        // Gera o código do boleto
        String boletoCode = boletoCodeService.generateBoletoCode(
            bankCode, walletCode, ourNumber, dueDate, amount
        );
        
        // Cria o boleto
        Boleto boleto = new Boleto();
        boleto.setTitle(title);
        boleto.setAmount(amount);
        boleto.setDueDate(dueDate);
        boleto.setBankCode(bankCode);
        boleto.setWalletCode(walletCode);
        boleto.setOurNumber(ourNumber);
        boleto.setBoletoCode(boletoCode);
        boleto.setUserId(userId);
        
        // Define o status baseado na data de vencimento
        updateStatusBasedOnDueDate(boleto);
        
        // Salva no banco
        return boletoDAO.save(boleto);
    }
    
    /**
     * Busca um boleto por ID.
     * 
     * @param id ID do boleto
     * @return Boleto encontrado
     * @throws IllegalArgumentException se o boleto não for encontrado
     */
    public Boleto getBoletoById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return boletoDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Boleto não encontrado com ID: " + id));
    }
    
    /**
     * Lista todos os boletos.
     * 
     * @return Lista de todos os boletos
     */
    public List<Boleto> listBoletos() {
        List<Boleto> boletos = boletoDAO.findAll();
        // Atualiza status de todos os boletos antes de retornar
        boletos.forEach(this::updateStatusBasedOnDueDate);
        return boletos;
    }
    
    /**
     * Lista boletos por usuário.
     * 
     * @param userId ID do usuário
     * @return Lista de boletos do usuário
     */
    public List<Boleto> listBoletosByUserId(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        List<Boleto> boletos = boletoDAO.findByUserId(userId);
        // Atualiza status de todos os boletos antes de retornar
        boletos.forEach(this::updateStatusBasedOnDueDate);
        return boletos;
    }
    
    /**
     * Lista boletos por status.
     * 
     * @param status Status do boleto
     * @return Lista de boletos com o status especificado
     */
    public List<Boleto> listBoletosByStatus(BoletoStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status não pode ser nulo");
        }
        List<Boleto> boletos = boletoDAO.findByStatus(status);
        // Atualiza status de todos os boletos antes de retornar
        boletos.forEach(this::updateStatusBasedOnDueDate);
        return boletos;
    }
    
    /**
     * Lista boletos por CPF do usuário.
     * Busca o usuário pelo CPF e retorna todos os seus boletos.
     * 
     * @param cpf CPF do usuário (com ou sem formatação)
     * @return Lista de boletos do usuário
     */
    public List<Boleto> listBoletosByCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF não pode ser nulo ou vazio");
        }
        
        // Normaliza o CPF usando ValidateCPF
        org.example.utils.ValidateCPF validateCPF = new org.example.utils.ValidateCPF();
        String cleanCpf = validateCPF.unformat(cpf.trim());
        
        // Valida o CPF
        if (!validateCPF.isValid(cleanCpf)) {
            throw new IllegalArgumentException("CPF inválido: " + cpf);
        }
        
        // Formata o CPF para buscar no banco (formato: XXX.XXX.XXX-XX)
        String formattedCpf = validateCPF.format(cleanCpf);
        
        // Busca o usuário pelo CPF
        org.example.service.UserService userService = new org.example.service.UserService();
        org.example.model.User user = userService.getUserByTaxId(formattedCpf)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com CPF: " + cpf));
        
        // Busca os boletos do usuário
        List<Boleto> boletos = boletoDAO.findByUserId(user.getUserId());
        // Atualiza status de todos os boletos antes de retornar
        boletos.forEach(this::updateStatusBasedOnDueDate);
        return boletos;
    }
    
    /**
     * Marca um boleto como pago.
     * 
     * @param id ID do boleto
     * @return Boleto atualizado
     */
    public Boleto markAsPaid(Long id) {
        Boleto boleto = getBoletoById(id);
        boleto.setStatus(BoletoStatus.PAID);
        boleto.setPaidAt(java.time.LocalDateTime.now());
        return boletoDAO.update(boleto);
    }
    
    /**
     * Atualiza um boleto existente.
     * 
     * @param boleto Boleto atualizado
     * @return Boleto atualizado
     */
    public Boleto updateBoleto(Boleto boleto) {
        if (boleto == null) {
            throw new IllegalArgumentException("Boleto não pode ser nulo");
        }
        if (boleto.getId() == null) {
            throw new IllegalArgumentException("ID do boleto é obrigatório para atualização");
        }
        
        // Verifica se o boleto existe
        Boleto existingBoleto = getBoletoById(boleto.getId());
        
        // Atualiza status se a data de vencimento mudou
        updateStatusBasedOnDueDate(boleto);
        
        return boletoDAO.update(boleto);
    }
    
    /**
     * Remove um boleto.
     * 
     * @param id ID do boleto a ser removido
     * @throws IllegalArgumentException se o boleto não for encontrado
     */
    public void deleteBoleto(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        
        boolean deleted = boletoDAO.delete(id);
        if (!deleted) {
            throw new IllegalArgumentException("Boleto não encontrado com ID: " + id);
        }
    }
    
    /**
     * Atualiza o status do boleto baseado na data de vencimento.
     * Se o boleto já está pago, não altera o status.
     * 
     * @param boleto Boleto a ser atualizado
     */
    private void updateStatusBasedOnDueDate(Boleto boleto) {
        if (boleto.getStatus() == BoletoStatus.PAID) {
            return; // Se já está pago, não muda
        }
        
        LocalDate today = LocalDate.now();
        if (boleto.getDueDate() != null) {
            if (boleto.getDueDate().isBefore(today) || boleto.getDueDate().isEqual(today)) {
                boleto.setStatus(BoletoStatus.OVERDUE);
            } else {
                boleto.setStatus(BoletoStatus.OPEN);
            }
        }
    }
}

