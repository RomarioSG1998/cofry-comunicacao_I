package org.example.service;

import org.example.dao.AddressDAO;
import org.example.dao.UserDAO;
import org.example.model.Address;
import java.util.List;

/**
 * Service para gerenciar endereços.
 * Camada de serviço que encapsula a lógica de negócio relacionada aos endereços.
 */
public class AddressService {
    
    private final AddressDAO addressDAO;
    private final UserDAO userDAO;
    
    public AddressService() {
        this.addressDAO = new AddressDAO();
        this.userDAO = new UserDAO();
    }
    
    /**
     * Cria um novo endereço.
     * 
     * @param address Endereço a ser criado
     * @return Endereço criado com ID gerado
     */
    public Address createAddress(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Endereço não pode ser nulo");
        }
        
        validateAddress(address);
        
        // Verifica se o usuário existe
        if (address.getUserId() == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório");
        }
        userDAO.findById(address.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + address.getUserId()));
        
        // Define país padrão se não especificado
        if (address.getCountry() == null || address.getCountry().trim().isEmpty()) {
            address.setCountry("Brazil");
        }
        
        return addressDAO.save(address);
    }
    
    /**
     * Busca um endereço por ID.
     * 
     * @param id ID do endereço
     * @return Endereço encontrado
     * @throws IllegalArgumentException se o endereço não for encontrado
     */
    public Address getAddressById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return addressDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Endereço não encontrado com ID: " + id));
    }
    
    /**
     * Busca um endereço por ID e verifica se pertence ao usuário especificado.
     * 
     * @param id ID do endereço
     * @param userId ID do usuário
     * @return Endereço encontrado
     * @throws IllegalArgumentException se o endereço não for encontrado ou não pertencer ao usuário
     */
    public Address getAddressByIdAndUserId(Integer id, Integer userId) {
        if (id == null) {
            throw new IllegalArgumentException("ID do endereço não pode ser nulo");
        }
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        
        Address address = getAddressById(id);
        
        if (!address.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Endereço não encontrado ou não pertence ao usuário especificado");
        }
        
        return address;
    }
    
    /**
     * Lista todos os endereços.
     * 
     * @return Lista de todos os endereços
     */
    public List<Address> getAllAddresses() {
        return addressDAO.findAll();
    }
    
    /**
     * Lista todos os endereços de um usuário.
     * 
     * @param userId ID do usuário
     * @return Lista de endereços do usuário
     */
    public List<Address> getAddressesByUserId(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo");
        }
        return addressDAO.findByUserId(userId);
    }
    
    /**
     * Lista endereços por cidade.
     * 
     * @param city Nome da cidade
     * @return Lista de endereços na cidade especificada
     */
    public List<Address> getAddressesByCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("Cidade não pode ser nula ou vazia");
        }
        return addressDAO.findByCity(city);
    }
    
    /**
     * Lista endereços por estado.
     * 
     * @param state Nome do estado
     * @return Lista de endereços no estado especificado
     */
    public List<Address> getAddressesByState(String state) {
        if (state == null || state.trim().isEmpty()) {
            throw new IllegalArgumentException("Estado não pode ser nulo ou vazio");
        }
        return addressDAO.findByState(state);
    }
    
    /**
     * Atualiza um endereço existente.
     * 
     * @param address Endereço atualizado
     * @return Endereço atualizado
     */
    public Address updateAddress(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Endereço não pode ser nulo");
        }
        if (address.getAddressId() == null) {
            throw new IllegalArgumentException("ID do endereço é obrigatório para atualização");
        }
        
        // Verifica se o endereço existe
        getAddressById(address.getAddressId());
        
        // Se o userId foi alterado, verifica se o novo usuário existe
        if (address.getUserId() != null) {
            userDAO.findById(address.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + address.getUserId()));
        }
        
        validateAddress(address);
        
        return addressDAO.update(address);
    }
    
    /**
     * Remove um endereço.
     * 
     * @param id ID do endereço a ser removido
     * @throws IllegalArgumentException se o endereço não for encontrado
     */
    public void deleteAddress(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        
        boolean deleted = addressDAO.delete(id);
        if (!deleted) {
            throw new IllegalArgumentException("Endereço não encontrado com ID: " + id);
        }
    }
    
    /**
     * Valida os dados básicos de um endereço.
     * 
     * @param address Endereço a ser validado
     */
    private void validateAddress(Address address) {
        if (address.getStreet() == null || address.getStreet().trim().isEmpty()) {
            throw new IllegalArgumentException("Rua é obrigatória");
        }
        if (address.getNumber() == null || address.getNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Número é obrigatório");
        }
        if (address.getCity() == null || address.getCity().trim().isEmpty()) {
            throw new IllegalArgumentException("Cidade é obrigatória");
        }
        if (address.getState() == null || address.getState().trim().isEmpty()) {
            throw new IllegalArgumentException("Estado é obrigatório");
        }
        if (address.getZipCode() == null || address.getZipCode().trim().isEmpty()) {
            throw new IllegalArgumentException("CEP é obrigatório");
        }
    }
}

