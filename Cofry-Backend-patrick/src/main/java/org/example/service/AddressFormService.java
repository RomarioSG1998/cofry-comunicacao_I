package org.example.service;

import org.example.dao.UserDAO;
import org.example.dto.AddressRequestDTO;
import org.example.dto.AddressResponseDTO;
import org.example.model.Address;
import org.example.model.User;
import org.example.utils.ZipCodeValidator;

/**
 * Service for handling address form submissions from front-end.
 * Handles zip code lookup and automatic address filling.
 */
public class AddressFormService {
    
    private final AddressService addressService;
    private final UserDAO userDAO;
    
    public AddressFormService() {
        this.addressService = new AddressService();
        this.userDAO = new UserDAO();
    }
    
    /**
     * Creates a new address from form DTO.
     * Looks up zip code via ViaCEP API and fills address fields automatically with the data returned.
     * 
     * @param addressDTO Address request DTO from front-end
     * @return Address response DTO
     */
    public AddressResponseDTO createAddressFromForm(AddressRequestDTO addressDTO) {
        if (addressDTO == null) {
            throw new IllegalArgumentException("Address data cannot be null");
        }
        
        // Validate user exists
        if (addressDTO.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        userDAO.findById(addressDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + addressDTO.getUserId()));
        
        // Validate and process zip code
        if (addressDTO.getZipCode() == null || addressDTO.getZipCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Zip code is required");
        }
        
        if (!ZipCodeValidator.isValidFormat(addressDTO.getZipCode())) {
            throw new IllegalArgumentException("Invalid zip code format");
        }
        
        String formattedZipCode = ZipCodeValidator.format(addressDTO.getZipCode());
        
        // Lookup address by zip code via ViaCEP API
        ZipCodeValidator.ZipCodeInfo zipCodeInfo = ZipCodeValidator.lookupAddress(addressDTO.getZipCode());
        
        // Create Address entity
        Address address = new Address();
        address.setUserId(addressDTO.getUserId());
        address.setZipCode(formattedZipCode);
        // Use house number from DTO, or default to "S/N" (sem número) if not provided
        String houseNumber = (addressDTO.getHouseNumber() != null && !addressDTO.getHouseNumber().trim().isEmpty()) 
            ? addressDTO.getHouseNumber().trim() 
            : "S/N";
        address.setNumber(houseNumber);
        address.setCountry("Brazil");
        
        // Prioritize data from ViaCEP API when available (more reliable)
        if (zipCodeInfo != null) {
            // Always use data from API if available
            address.setStreet(zipCodeInfo.getStreet() != null ? zipCodeInfo.getStreet() : addressDTO.getStreet());
            address.setNeighborhood(zipCodeInfo.getDistrict() != null ? zipCodeInfo.getDistrict() : addressDTO.getDistrict());
            address.setCity(zipCodeInfo.getCity() != null ? zipCodeInfo.getCity() : addressDTO.getCity());
            address.setState(zipCodeInfo.getState() != null ? zipCodeInfo.getState() : addressDTO.getState());
        } else {
            // Fallback: use values provided by front-end if zip code lookup failed
            address.setStreet(addressDTO.getStreet());
            address.setNeighborhood(addressDTO.getDistrict());
            address.setCity(addressDTO.getCity());
            address.setState(addressDTO.getState());
        }
        
        // Validate required fields
        if (address.getStreet() == null || address.getStreet().trim().isEmpty()) {
            throw new IllegalArgumentException("Street is required");
        }
        // Number is always set (defaults to "S/N" if not provided), so no need to validate
        if (address.getCity() == null || address.getCity().trim().isEmpty()) {
            throw new IllegalArgumentException("City is required");
        }
        if (address.getState() == null || address.getState().trim().isEmpty()) {
            throw new IllegalArgumentException("State is required");
        }
        
        address.setComplement(addressDTO.getComplement());
        
        // Save address
        Address savedAddress = addressService.createAddress(address);
        
        // Convert to response DTO
        return convertToResponseDTO(savedAddress);
    }
    
    /**
     * Looks up address information by zip code.
     * 
     * @param zipCode Zip code to lookup
     * @return Address information or null if not found
     */
    public ZipCodeValidator.ZipCodeInfo lookupZipCode(String zipCode) {
        if (zipCode == null || zipCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Zip code cannot be empty");
        }
        
        if (!ZipCodeValidator.isValidFormat(zipCode)) {
            throw new IllegalArgumentException("Invalid zip code format");
        }
        
        return ZipCodeValidator.lookupAddress(zipCode);
    }
    
    /**
     * Converts Address entity to AddressResponseDTO.
     * 
     * @param address Address entity
     * @return AddressResponseDTO
     */
    private AddressResponseDTO convertToResponseDTO(Address address) {
        AddressResponseDTO dto = new AddressResponseDTO();
        dto.setAddressId(address.getAddressId());
        dto.setUserId(address.getUserId());
        
        // Get phone number from user
        User user = userDAO.findById(address.getUserId()).orElse(null);
        if (user != null) {
            dto.setPhoneNumber(user.getPhoneNumber());
        }
        
        dto.setZipCode(address.getZipCode());
        dto.setHouseNumber(address.getNumber());
        dto.setStreet(address.getStreet());
        dto.setDistrict(address.getNeighborhood());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setComplement(address.getComplement());
        dto.setCountry(address.getCountry());
        dto.setCreatedAt(address.getCreatedAt());
        return dto;
    }
}

