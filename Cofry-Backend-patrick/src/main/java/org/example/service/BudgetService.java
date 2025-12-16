package org.example.service;

import org.example.dao.BudgetDAO;
import org.example.dao.UserDAO;
import org.example.dao.TransactionCategoryDAO;
import org.example.model.Budget;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service para gerenciar orçamentos.
 * Camada de serviço que encapsula a lógica de negócio relacionada aos orçamentos.
 */
public class BudgetService {
    
    private final BudgetDAO budgetDAO;
    private final UserDAO userDAO;
    private final TransactionCategoryDAO categoryDAO;
    
    public BudgetService() {
        this.budgetDAO = new BudgetDAO();
        this.userDAO = new UserDAO();
        this.categoryDAO = new TransactionCategoryDAO();
    }
    
    /**
     * Cria um novo orçamento.
     * 
     * @param budget Orçamento a ser criado
     * @return Orçamento criado com ID gerado
     */
    public Budget createBudget(Budget budget) {
        if (budget == null) {
            throw new IllegalArgumentException("Orçamento não pode ser nulo");
        }
        
        validateBudget(budget);
        
        // Verifica se o usuário existe
        if (budget.getUserId() == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório");
        }
        userDAO.findById(budget.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + budget.getUserId()));
        
        // Verifica se a categoria existe
        if (budget.getCategoryId() == null) {
            throw new IllegalArgumentException("ID da categoria é obrigatório");
        }
        categoryDAO.findById(budget.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada com ID: " + budget.getCategoryId()));
        
        // Verifica se já existe orçamento para o mesmo usuário, categoria e período
        Optional<Budget> existing = budgetDAO.findByUserCategoryAndPeriod(
                budget.getUserId(),
                budget.getCategoryId(),
                budget.getPeriodMonth(),
                budget.getPeriodYear()
        );
        
        if (existing.isPresent()) {
            throw new IllegalArgumentException(
                    "Já existe um orçamento para este usuário, categoria e período"
            );
        }
        
        return budgetDAO.save(budget);
    }
    
    /**
     * Busca um orçamento por ID.
     * 
     * @param id ID do orçamento
     * @return Orçamento encontrado
     * @throws IllegalArgumentException se o orçamento não for encontrado
     */
    public Budget getBudgetById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return budgetDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Orçamento não encontrado com ID: " + id));
    }
    
    /**
     * Lista todos os orçamentos.
     * 
     * @return Lista de todos os orçamentos
     */
    public List<Budget> getAllBudgets() {
        return budgetDAO.findAll();
    }
    
    /**
     * Lista orçamentos de um usuário.
     * 
     * @param userId ID do usuário
     * @return Lista de orçamentos do usuário
     */
    public List<Budget> getBudgetsByUserId(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        return budgetDAO.findByUserId(userId);
    }
    
    /**
     * Lista orçamentos por categoria.
     * 
     * @param categoryId ID da categoria
     * @return Lista de orçamentos da categoria
     */
    public List<Budget> getBudgetsByCategory(Integer categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("ID da categoria não pode ser nulo");
        }
        return budgetDAO.findByCategoryId(categoryId);
    }
    
    /**
     * Lista orçamentos por período.
     * 
     * @param month Mês
     * @param year Ano
     * @return Lista de orçamentos do período
     */
    public List<Budget> getBudgetsByPeriod(Integer month, Integer year) {
        if (month == null || year == null) {
            throw new IllegalArgumentException("Mês e ano são obrigatórios");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Mês deve estar entre 1 e 12");
        }
        return budgetDAO.findByPeriod(month, year);
    }
    
    /**
     * Busca um orçamento específico.
     * 
     * @param userId ID do usuário
     * @param categoryId ID da categoria
     * @param month Mês
     * @param year Ano
     * @return Orçamento encontrado ou null se não existir
     */
    public Optional<Budget> getBudgetByUserCategoryAndPeriod(Integer userId, Integer categoryId, Integer month, Integer year) {
        if (userId == null || categoryId == null || month == null || year == null) {
            throw new IllegalArgumentException("Todos os parâmetros são obrigatórios");
        }
        return budgetDAO.findByUserCategoryAndPeriod(userId, categoryId, month, year);
    }
    
    /**
     * Atualiza um orçamento existente.
     * 
     * @param budget Orçamento atualizado
     * @return Orçamento atualizado
     */
    public Budget updateBudget(Budget budget) {
        if (budget == null) {
            throw new IllegalArgumentException("Orçamento não pode ser nulo");
        }
        if (budget.getBudgetId() == null) {
            throw new IllegalArgumentException("ID do orçamento é obrigatório para atualização");
        }
        
        // Verifica se o orçamento existe
        getBudgetById(budget.getBudgetId());
        
        validateBudget(budget);
        
        // Verifica se usuário e categoria existem (se foram alterados)
        if (budget.getUserId() != null) {
            userDAO.findById(budget.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + budget.getUserId()));
        }
        if (budget.getCategoryId() != null) {
            categoryDAO.findById(budget.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada com ID: " + budget.getCategoryId()));
        }
        
        return budgetDAO.update(budget);
    }
    
    /**
     * Remove um orçamento.
     * 
     * @param id ID do orçamento a ser removido
     * @throws IllegalArgumentException se o orçamento não for encontrado
     */
    public void deleteBudget(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        
        boolean deleted = budgetDAO.delete(id);
        if (!deleted) {
            throw new IllegalArgumentException("Orçamento não encontrado com ID: " + id);
        }
    }
    
    /**
     * Valida os dados básicos de um orçamento.
     * 
     * @param budget Orçamento a ser validado
     */
    private void validateBudget(Budget budget) {
        if (budget.getAmountLimit() == null || budget.getAmountLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Limite do orçamento deve ser maior que zero");
        }
        if (budget.getPeriodMonth() == null || budget.getPeriodMonth() < 1 || budget.getPeriodMonth() > 12) {
            throw new IllegalArgumentException("Mês deve estar entre 1 e 12");
        }
        if (budget.getPeriodYear() == null || budget.getPeriodYear() < 1900) {
            throw new IllegalArgumentException("Ano inválido");
        }
    }
}

