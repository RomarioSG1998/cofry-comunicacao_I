package org.example.service;

import org.example.dao.SavingsGoalDAO;
import org.example.dao.UserDAO;
import org.example.model.SavingsGoal;
import org.example.model.GoalStatusEnum;
import java.math.BigDecimal;
import java.util.List;

/**
 * Service para gerenciar metas de poupança.
 * Camada de serviço que encapsula a lógica de negócio relacionada às metas.
 */
public class SavingsGoalService {
    
    private final SavingsGoalDAO goalDAO;
    private final UserDAO userDAO;
    
    public SavingsGoalService() {
        this.goalDAO = new SavingsGoalDAO();
        this.userDAO = new UserDAO();
    }
    
    /**
     * Cria uma nova meta de poupança.
     * 
     * @param goal Meta a ser criada
     * @return Meta criada com ID gerado
     */
    public SavingsGoal createGoal(SavingsGoal goal) {
        if (goal == null) {
            throw new IllegalArgumentException("Meta não pode ser nula");
        }
        
        validateGoal(goal);
        
        // Verifica se o usuário existe
        if (goal.getUserId() == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório");
        }
        userDAO.findById(goal.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + goal.getUserId()));
        
        // Define valores padrão
        if (goal.getCurrentAmount() == null) {
            goal.setCurrentAmount(BigDecimal.ZERO);
        }
        if (goal.getStatus() == null) {
            goal.setStatus(GoalStatusEnum.IN_PROGRESS);
        }
        
        return goalDAO.save(goal);
    }
    
    /**
     * Busca uma meta por ID.
     * 
     * @param id ID da meta
     * @return Meta encontrada
     * @throws IllegalArgumentException se a meta não for encontrada
     */
    public SavingsGoal getGoalById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return goalDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Meta não encontrada com ID: " + id));
    }
    
    /**
     * Lista todas as metas.
     * 
     * @return Lista de todas as metas
     */
    public List<SavingsGoal> getAllGoals() {
        return goalDAO.findAll();
    }
    
    /**
     * Lista metas de um usuário.
     * 
     * @param userId ID do usuário
     * @return Lista de metas do usuário
     */
    public List<SavingsGoal> getGoalsByUserId(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        return goalDAO.findByUserId(userId);
    }
    
    /**
     * Lista metas por status.
     * 
     * @param status Status da meta
     * @return Lista de metas com o status especificado
     */
    public List<SavingsGoal> getGoalsByStatus(GoalStatusEnum status) {
        if (status == null) {
            throw new IllegalArgumentException("Status não pode ser nulo");
        }
        return goalDAO.findByStatus(status);
    }
    
    /**
     * Lista metas de um usuário por status.
     * 
     * @param userId ID do usuário
     * @param status Status da meta
     * @return Lista de metas do usuário com o status especificado
     */
    public List<SavingsGoal> getGoalsByUserIdAndStatus(Integer userId, GoalStatusEnum status) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status não pode ser nulo");
        }
        return goalDAO.findByUserIdAndStatus(userId, status);
    }
    
    /**
     * Atualiza uma meta existente.
     * 
     * @param goal Meta atualizada
     * @return Meta atualizada
     */
    public SavingsGoal updateGoal(SavingsGoal goal) {
        if (goal == null) {
            throw new IllegalArgumentException("Meta não pode ser nula");
        }
        if (goal.getGoalId() == null) {
            throw new IllegalArgumentException("ID da meta é obrigatório para atualização");
        }
        
        // Verifica se a meta existe
        getGoalById(goal.getGoalId());
        
        validateGoal(goal);
        
        // Verifica se o usuário existe (se foi alterado)
        if (goal.getUserId() != null) {
            userDAO.findById(goal.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + goal.getUserId()));
        }
        
        // Valida se currentAmount não excede targetAmount
        if (goal.getCurrentAmount() != null && goal.getTargetAmount() != null) {
            if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) > 0) {
                // Se exceder, atualiza o status para COMPLETED
                goal.setStatus(GoalStatusEnum.COMPLETED);
                goal.setCurrentAmount(goal.getTargetAmount());
            } else if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) == 0) {
                goal.setStatus(GoalStatusEnum.COMPLETED);
            }
        }
        
        return goalDAO.update(goal);
    }
    
    /**
     * Adiciona valor à meta (depósito).
     * 
     * @param goalId ID da meta
     * @param amount Valor a ser adicionado
     * @return Meta atualizada
     */
    public SavingsGoal addAmountToGoal(Integer goalId, BigDecimal amount) {
        if (goalId == null) {
            throw new IllegalArgumentException("ID da meta não pode ser nulo");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor deve ser maior que zero");
        }
        
        SavingsGoal goal = getGoalById(goalId);
        
        if (goal.getStatus() != GoalStatusEnum.IN_PROGRESS) {
            throw new IllegalArgumentException("Não é possível adicionar valor a uma meta que não está em progresso");
        }
        
        BigDecimal newAmount = goal.getCurrentAmount().add(amount);
        goal.setCurrentAmount(newAmount);
        
        // Verifica se a meta foi alcançada
        if (newAmount.compareTo(goal.getTargetAmount()) >= 0) {
            goal.setCurrentAmount(goal.getTargetAmount());
            goal.setStatus(GoalStatusEnum.COMPLETED);
        }
        
        return goalDAO.update(goal);
    }
    
    /**
     * Remove uma meta.
     * 
     * @param id ID da meta a ser removida
     * @throws IllegalArgumentException se a meta não for encontrada
     */
    public void deleteGoal(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        
        boolean deleted = goalDAO.delete(id);
        if (!deleted) {
            throw new IllegalArgumentException("Meta não encontrada com ID: " + id);
        }
    }
    
    /**
     * Valida os dados básicos de uma meta.
     * 
     * @param goal Meta a ser validada
     */
    private void validateGoal(SavingsGoal goal) {
        if (goal.getName() == null || goal.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da meta é obrigatório");
        }
        if (goal.getTargetAmount() == null || goal.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor alvo deve ser maior que zero");
        }
        if (goal.getCurrentAmount() != null && goal.getCurrentAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor atual não pode ser negativo");
        }
    }
}

