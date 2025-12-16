package org.example.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Utility class for ZipCode (CEP) validation and address lookup.
 * Uses ViaCEP API to fetch address information by zip code.
 */
public class ZipCodeValidator {
    
    private static final String VIACEP_API_URL = "https://viacep.com.br/ws/";
    private static final String VIACEP_API_FORMAT = "/json/";
    
    /**
     * Validates if a zip code has a valid format (8 digits).
     * 
     * @param zipCode Zip code to validate
     * @return true if format is valid, false otherwise
     */
    public static boolean isValidFormat(String zipCode) {
        if (zipCode == null || zipCode.trim().isEmpty()) {
            return false;
        }
        
        // Remove non-numeric characters
        String cleanZipCode = zipCode.replaceAll("[^0-9]", "");
        
        // Must have exactly 8 digits
        return cleanZipCode.length() == 8;
    }
    
    /**
     * Formats a zip code (12345678 -> 12345-678).
     * 
     * @param zipCode Zip code to format
     * @return Formatted zip code or null if invalid
     */
    public static String format(String zipCode) {
        if (!isValidFormat(zipCode)) {
            return null;
        }
        
        String cleanZipCode = zipCode.replaceAll("[^0-9]", "");
        if (cleanZipCode.length() != 8) {
            return null;
        }
        
        return cleanZipCode.substring(0, 5) + "-" + cleanZipCode.substring(5);
    }
    
    /**
     * Removes formatting from zip code (12345-678 -> 12345678).
     * 
     * @param zipCode Zip code to unformat
     * @return Zip code without formatting or null if invalid
     */
    public static String unformat(String zipCode) {
        if (zipCode == null || zipCode.trim().isEmpty()) {
            return null;
        }
        
        String cleanZipCode = zipCode.replaceAll("[^0-9]", "");
        
        if (cleanZipCode.length() != 8) {
            return null;
        }
        
        return cleanZipCode;
    }
    
    /**
     * Fetches address information from ViaCEP API based on zip code.
     * 
     * @param zipCode Zip code to lookup
     * @return ZipCodeInfo with address details or null if not found
     */
    public static ZipCodeInfo lookupAddress(String zipCode) {
        if (!isValidFormat(zipCode)) {
            System.err.println("Invalid zip code format: " + zipCode);
            return null;
        }
        
        String cleanZipCode = unformat(zipCode);
        
        try {
            String urlString = VIACEP_API_URL + cleanZipCode + VIACEP_API_FORMAT;
            System.out.println("Fetching address from ViaCEP: " + urlString);
            
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Accept", "application/json");
            
            int responseCode = connection.getResponseCode();
            System.out.println("ViaCEP response code: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8")
                );
                
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                String jsonResponse = response.toString();
                System.out.println("ViaCEP response: " + jsonResponse);
                
                ZipCodeInfo info = parseViaCepResponse(jsonResponse);
                
                if (info != null) {
                    System.out.println("Parsed address - City: " + info.getCity() + ", Street: " + info.getStreet());
                } else {
                    System.err.println("Failed to parse ViaCEP response");
                }
                
                return info;
            } else {
                System.err.println("ViaCEP returned error code: " + responseCode);
                // Tenta ler a mensagem de erro
                try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), "UTF-8"))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    System.err.println("Error response: " + errorResponse.toString());
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching zip code info: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Parses ViaCEP API JSON response using Gson.
     * 
     * @param jsonResponse JSON response from ViaCEP
     * @return ZipCodeInfo object or null if error or not found
     */
    private static ZipCodeInfo parseViaCepResponse(String jsonResponse) {
        try {
            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                System.err.println("ViaCEP response is empty");
                return null;
            }
            
            // Parse JSON usando Gson
            JsonObject jsonObject = new JsonParser().parse(jsonResponse).getAsJsonObject();
            
            // Verifica se há erro na resposta
            if (jsonObject.has("erro") && jsonObject.get("erro").getAsBoolean()) {
                System.err.println("ViaCEP returned error: CEP not found");
                return null;
            }
            
            ZipCodeInfo info = new ZipCodeInfo();
            
            // Extrai campos do JSON (trata valores null e strings vazias)
            info.setZipCode(getJsonStringValue(jsonObject, "cep"));
            info.setStreet(getJsonStringValue(jsonObject, "logradouro"));
            info.setDistrict(getJsonStringValue(jsonObject, "bairro"));
            info.setCity(getJsonStringValue(jsonObject, "localidade"));
            info.setState(getJsonStringValue(jsonObject, "uf"));
            
            // Valida se conseguiu extrair pelo menos o CEP e cidade (campos essenciais)
            if (info.getZipCode() == null && info.getCity() == null) {
                System.err.println("ViaCEP response missing essential fields");
                return null;
            }
            
            return info;
        } catch (Exception e) {
            System.err.println("Error parsing ViaCEP response: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Extrai valor string de um JsonObject, tratando null e strings vazias.
     * 
     * @param jsonObject Objeto JSON
     * @param key Chave para buscar
     * @return Valor da string ou null se não encontrado/vazio
     */
    private static String getJsonStringValue(JsonObject jsonObject, String key) {
        if (!jsonObject.has(key) || jsonObject.get(key).isJsonNull()) {
            return null;
        }
        
        String value = jsonObject.get(key).getAsString();
        
        // Retorna null se a string estiver vazia ou contiver apenas espaços
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        return value.trim();
    }
    
    /**
     * Class to hold zip code information.
     */
    public static class ZipCodeInfo {
        private String zipCode;
        private String street;
        private String district;
        private String city;
        private String state;
        
        // Getters and Setters
        public String getZipCode() {
            return zipCode;
        }
        
        public void setZipCode(String zipCode) {
            this.zipCode = zipCode;
        }
        
        public String getStreet() {
            return street;
        }
        
        public void setStreet(String street) {
            this.street = street;
        }
        
        public String getDistrict() {
            return district;
        }
        
        public void setDistrict(String district) {
            this.district = district;
        }
        
        public String getCity() {
            return city;
        }
        
        public void setCity(String city) {
            this.city = city;
        }
        
        public String getState() {
            return state;
        }
        
        public void setState(String state) {
            this.state = state;
        }
    }
}

