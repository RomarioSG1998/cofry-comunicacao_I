package org.example.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for Brazilian states management.
 */
public class BrazilStates {
    
    private static final String[][] STATES = {
        {"AC", "Acre"},
        {"AL", "Alagoas"},
        {"AP", "Amapá"},
        {"AM", "Amazonas"},
        {"BA", "Bahia"},
        {"CE", "Ceará"},
        {"DF", "Distrito Federal"},
        {"ES", "Espírito Santo"},
        {"GO", "Goiás"},
        {"MA", "Maranhão"},
        {"MT", "Mato Grosso"},
        {"MS", "Mato Grosso do Sul"},
        {"MG", "Minas Gerais"},
        {"PA", "Pará"},
        {"PB", "Paraíba"},
        {"PR", "Paraná"},
        {"PE", "Pernambuco"},
        {"PI", "Piauí"},
        {"RJ", "Rio de Janeiro"},
        {"RN", "Rio Grande do Norte"},
        {"RS", "Rio Grande do Sul"},
        {"RO", "Rondônia"},
        {"RR", "Roraima"},
        {"SC", "Santa Catarina"},
        {"SP", "São Paulo"},
        {"SE", "Sergipe"},
        {"TO", "Tocantins"}
    };
    
    /**
     * Gets list of all Brazilian states.
     * 
     * @return List of state objects with code and name
     */
    public static List<StateInfo> getAllStates() {
        List<StateInfo> states = new ArrayList<>();
        for (String[] state : STATES) {
            states.add(new StateInfo(state[0], state[1]));
        }
        return states;
    }
    
    /**
     * Gets list of state codes only.
     * 
     * @return List of state codes (UF)
     */
    public static List<String> getStateCodes() {
        List<String> codes = new ArrayList<>();
        for (String[] state : STATES) {
            codes.add(state[0]);
        }
        return codes;
    }
    
    /**
     * Gets state name by code.
     * 
     * @param code State code (UF)
     * @return State name or null if not found
     */
    public static String getStateName(String code) {
        if (code == null) {
            return null;
        }
        
        String upperCode = code.toUpperCase();
        for (String[] state : STATES) {
            if (state[0].equals(upperCode)) {
                return state[1];
            }
        }
        return null;
    }
    
    /**
     * Checks if a state code is valid.
     * 
     * @param code State code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidStateCode(String code) {
        return getStateName(code) != null;
    }
    
    /**
     * Class to represent state information.
     */
    public static class StateInfo {
        private String code;
        private String name;
        
        public StateInfo(String code, String name) {
            this.code = code;
            this.name = name;
        }
        
        public String getCode() {
            return code;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
    }
}

