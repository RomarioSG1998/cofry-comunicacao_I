package org.example.service;

import org.example.dao.TransactionCategoryDAO;
import org.example.model.TransactionCategory;
import java.util.List;
import java.util.Optional;

/**
 * Service para gerenciar categorias de transações.
 * Camada de serviço que encapsula a lógica de negócio relacionada às categorias.
 */
public class TransactionCategoryService {
    
    private final TransactionCategoryDAO categoryDAO;
    
    public TransactionCategoryService() {
        this.categoryDAO = new TransactionCategoryDAO();
    }
    
    /**
     * Cria uma nova categoria de transação.
     * 
     * @param category Categoria a ser criada
     * @return Categoria criada com ID gerado
     */
    public TransactionCategory createCategory(TransactionCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("Categoria não pode ser nula");
        }
        
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da categoria é obrigatório");
        }
        
        // Verifica se já existe categoria com o mesmo nome
        Optional<TransactionCategory> existing = categoryDAO.findByName(category.getName());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Já existe uma categoria com o nome: " + category.getName());
        }
        
        return categoryDAO.save(category);
    }
    
    /**
     * Busca uma categoria por ID.
     * 
     * @param id ID da categoria
     * @return Categoria encontrada
     * @throws IllegalArgumentException se a categoria não for encontrada
     */
    public TransactionCategory getCategoryById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return categoryDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada com ID: " + id));
    }
    
    /**
     * Lista todas as categorias.
     * 
     * @return Lista de todas as categorias
     */
    public List<TransactionCategory> getAllCategories() {
        return categoryDAO.findAll();
    }
    
    /**
     * Busca uma categoria pelo nome.
     * 
     * @param name Nome da categoria
     * @return Categoria encontrada ou null se não existir
     */
    public Optional<TransactionCategory> getCategoryByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode ser nulo ou vazio");
        }
        return categoryDAO.findByName(name);
    }
    
    /**
     * Atualiza uma categoria existente.
     * 
     * @param category Categoria atualizada
     * @return Categoria atualizada
     */
    public TransactionCategory updateCategory(TransactionCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("Categoria não pode ser nula");
        }
        if (category.getCategoryId() == null) {
            throw new IllegalArgumentException("ID da categoria é obrigatório para atualização");
        }
        
        // Verifica se a categoria existe
        getCategoryById(category.getCategoryId());
        
        // Se o nome foi alterado, verifica se já existe outra categoria com esse nome
        if (category.getName() != null) {
            Optional<TransactionCategory> existing = categoryDAO.findByName(category.getName());
            if (existing.isPresent() && !existing.get().getCategoryId().equals(category.getCategoryId())) {
                throw new IllegalArgumentException("Já existe uma categoria com o nome: " + category.getName());
            }
        }
        
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da categoria é obrigatório");
        }
        
        return categoryDAO.update(category);
    }
    
    /**
     * Remove uma categoria.
     * 
     * @param id ID da categoria a ser removida
     * @throws IllegalArgumentException se a categoria não for encontrada
     */
    public void deleteCategory(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        
        boolean deleted = categoryDAO.delete(id);
        if (!deleted) {
            throw new IllegalArgumentException("Categoria não encontrada com ID: " + id);
        }
    }
}

