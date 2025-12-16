package org.example.service;

import org.example.dao.SubscriptionPlanDAO;
import org.example.model.SubscriptionPlan;
import java.util.List;
import java.util.Optional;

/**
 * Service para gerenciar planos de assinatura.
 * Camada de serviço que encapsula a lógica de negócio relacionada aos planos.
 */
public class SubscriptionPlanService {
    
    private final SubscriptionPlanDAO planDAO;
    
    public SubscriptionPlanService() {
        this.planDAO = new SubscriptionPlanDAO();
    }
    
    /**
     * Cria um novo plano de assinatura.
     * 
     * @param plan Plano a ser criado
     * @return Plano criado com ID gerado
     */
    public SubscriptionPlan createPlan(SubscriptionPlan plan) {
        if (plan == null) {
            throw new IllegalArgumentException("Plano não pode ser nulo");
        }
        if (plan.getName() == null || plan.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do plano é obrigatório");
        }
        
        // Verifica se já existe um plano com o mesmo nome
        Optional<SubscriptionPlan> existing = planDAO.findByName(plan.getName());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Já existe um plano com o nome: " + plan.getName());
        }
        
        return planDAO.save(plan);
    }
    
    /**
     * Busca um plano por ID.
     * 
     * @param id ID do plano
     * @return Plano encontrado
     * @throws IllegalArgumentException se o plano não for encontrado
     */
    public SubscriptionPlan getPlanById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return planDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plano não encontrado com ID: " + id));
    }
    
    /**
     * Lista todos os planos de assinatura.
     * 
     * @return Lista de todos os planos
     */
    public List<SubscriptionPlan> getAllPlans() {
        return planDAO.findAll();
    }
    
    /**
     * Busca um plano pelo nome.
     * 
     * @param name Nome do plano
     * @return Plano encontrado ou null se não existir
     */
    public Optional<SubscriptionPlan> getPlanByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode ser nulo ou vazio");
        }
        return planDAO.findByName(name);
    }
    
    /**
     * Atualiza um plano de assinatura existente.
     * 
     * @param plan Plano atualizado
     * @return Plano atualizado
     */
    public SubscriptionPlan updatePlan(SubscriptionPlan plan) {
        if (plan == null) {
            throw new IllegalArgumentException("Plano não pode ser nulo");
        }
        if (plan.getPlanId() == null) {
            throw new IllegalArgumentException("ID do plano é obrigatório para atualização");
        }
        
        // Verifica se o plano existe
        getPlanById(plan.getPlanId());
        
        return planDAO.update(plan);
    }
    
    /**
     * Remove um plano de assinatura.
     * 
     * @param id ID do plano a ser removido
     * @throws IllegalArgumentException se o plano não for encontrado
     */
    public void deletePlan(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        
        boolean deleted = planDAO.delete(id);
        if (!deleted) {
            throw new IllegalArgumentException("Plano não encontrado com ID: " + id);
        }
    }
}

