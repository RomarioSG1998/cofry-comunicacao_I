package org.example.controller;

import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.model.Transaction;
import org.example.model.TransactionTypeEnum;
import org.example.service.TransactionService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servlet para gerenciar operações CRUD de transações.
 * Endpoints:
 * GET    /api/transactions       - Lista todas as transações
 * GET    /api/transactions/{id}  - Busca transação por ID
 * POST   /api/transactions       - Cria nova transação
 * PUT    /api/transactions/{id}  - Atualiza transação
 * DELETE /api/transactions/{id}  - Remove transação
 */
@WebServlet(name = "TransactionServlet", urlPatterns = {"/api/transactions", "/api/transactions/*"})
public class TransactionServlet extends HttpServlet {
    
    private TransactionService transactionService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    @Override
    public void init() throws ServletException {
        super.init();
        transactionService = new TransactionService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/transactions - Lista todas ou filtra
                String accountIdParam = request.getParameter("accountId");
                String userIdParam = request.getParameter("userId");
                String typeParam = request.getParameter("type");
                String categoryIdParam = request.getParameter("categoryId");
                String startDateParam = request.getParameter("startDate");
                String endDateParam = request.getParameter("endDate");
                
                List<Transaction> transactions;
                
                if (userIdParam != null) {
                    // Filtrar por usuário (retorna todas as transações onde o usuário está envolvido)
                    Integer userId = Integer.parseInt(userIdParam);
                    transactions = transactionService.getTransactionsByUserId(userId);
                } else if (accountIdParam != null) {
                    // Filtrar por conta
                    Integer accountId = Integer.parseInt(accountIdParam);
                    transactions = transactionService.getTransactionsBySourceAccount(accountId);
                } else if (typeParam != null) {
                    // Filtrar por tipo
                    TransactionTypeEnum type = TransactionTypeEnum.valueOf(typeParam.toUpperCase());
                    transactions = transactionService.getTransactionsByType(type);
                } else if (categoryIdParam != null) {
                    // Filtrar por categoria
                    Integer categoryId = Integer.parseInt(categoryIdParam);
                    transactions = transactionService.getTransactionsByCategory(categoryId);
                } else if (startDateParam != null && endDateParam != null) {
                    // Filtrar por período
                    LocalDate startDate = LocalDate.parse(startDateParam, DATE_FORMATTER);
                    LocalDate endDate = LocalDate.parse(endDateParam, DATE_FORMATTER);
                    transactions = transactionService.getTransactionsByDateRange(startDate, endDate);
                } else {
                    // Lista todas
                    transactions = transactionService.getAllTransactions();
                }
                
                JsonResponse.sendSuccess(response, transactions);
            } else {
                // GET /api/transactions/{id} - Busca por ID
                Integer id = RequestParser.extractIdFromPath(pathInfo);
                if (id == null) {
                    JsonResponse.sendBadRequest(response, "ID inválido");
                    return;
                }
                
                try {
                    Transaction transaction = transactionService.getTransactionById(id);
                    JsonResponse.sendSuccess(response, transaction);
                } catch (IllegalArgumentException e) {
                    JsonResponse.sendNotFound(response, e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao processar requisição: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // Lê o JSON como Map para poder tratar valores formatados
            StringBuilder json = new StringBuilder();
            try (java.io.BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
            }
            
            if (json.length() == 0) {
                JsonResponse.sendBadRequest(response, "Corpo da requisição está vazio");
                return;
            }
            
            // Parse do JSON para Map
            com.google.gson.JsonObject jsonObject = com.google.gson.JsonParser.parseString(json.toString()).getAsJsonObject();
            Transaction transaction = new Transaction();
            
            // Mapeia os campos básicos
            if (jsonObject.has("sourceAccountId")) {
                transaction.setSourceAccountId(jsonObject.get("sourceAccountId").getAsInt());
            }
            if (jsonObject.has("destinationAccountId") && !jsonObject.get("destinationAccountId").isJsonNull()) {
                transaction.setDestinationAccountId(jsonObject.get("destinationAccountId").getAsInt());
            }
            if (jsonObject.has("categoryId") && !jsonObject.get("categoryId").isJsonNull()) {
                transaction.setCategoryId(jsonObject.get("categoryId").getAsInt());
            }
            
            // Trata o valor (amount) - pode vir como número ou string formatada
            if (jsonObject.has("amount")) {
                java.math.BigDecimal amount = parseAmount(jsonObject.get("amount"));
                transaction.setAmount(amount);
            }
            
            // Tipo da transação - determinado automaticamente se não fornecido
            if (jsonObject.has("transactionType")) {
                // Se foi fornecido explicitamente, usa o valor fornecido
                String typeStr = jsonObject.get("transactionType").getAsString();
                transaction.setTransactionType(TransactionTypeEnum.valueOf(typeStr.toUpperCase()));
            } else {
                // Determina automaticamente baseado nos campos:
                // - Se tem destinationAccountId → TRANSFER
                // - Se tem isIncome = true → DEPOSIT (entrada)
                // - Se tem isIncome = false ou não fornecido → PAYMENT (saída)
                transaction.setTransactionType(determineTransactionType(jsonObject, transaction));
            }
            
            // Descrição
            if (jsonObject.has("description")) {
                transaction.setDescription(jsonObject.get("description").getAsString());
            }
            
            // Data da transação (opcional)
            if (jsonObject.has("transactionDate")) {
                String dateStr = jsonObject.get("transactionDate").getAsString();
                transaction.setTransactionDate(parseDate(dateStr));
            }
            
            // Campos opcionais
            if (jsonObject.has("isRecurring")) {
                transaction.setIsRecurring(jsonObject.get("isRecurring").getAsBoolean());
            }
            if (jsonObject.has("installmentCurrent") && !jsonObject.get("installmentCurrent").isJsonNull()) {
                transaction.setInstallmentCurrent(jsonObject.get("installmentCurrent").getAsInt());
            }
            if (jsonObject.has("installmentTotal") && !jsonObject.get("installmentTotal").isJsonNull()) {
                transaction.setInstallmentTotal(jsonObject.get("installmentTotal").getAsInt());
            }
            
            Transaction createdTransaction = transactionService.createTransaction(transaction);
            JsonResponse.sendSuccess(response, createdTransaction, HttpServletResponse.SC_CREATED);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao criar transação: " + e.getMessage());
        }
    }
    
    /**
     * Faz parse de uma data que pode vir em diferentes formatos:
     * - "2025-12-16" (apenas data)
     * - "2025-12-16T06:50:00.000Z" (ISO 8601 completo)
     * - "2025-12-16T06:50:00" (ISO sem timezone)
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Tenta primeiro o formato padrão (apenas data)
            if (dateStr.length() == 10) {
                return LocalDate.parse(dateStr, DATE_FORMATTER);
            }
            
            // Se tem 'T', é formato ISO completo - extrai apenas a parte da data
            if (dateStr.contains("T")) {
                String dateOnly = dateStr.substring(0, 10); // Pega os primeiros 10 caracteres (YYYY-MM-DD)
                return LocalDate.parse(dateOnly, DATE_FORMATTER);
            }
            
            // Tenta parse direto
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de data inválido: " + dateStr + ". Use o formato YYYY-MM-DD");
        }
    }
    
    /**
     * Determina automaticamente o tipo de transação baseado nos campos:
     * - Se tem destinationAccountId → TRANSFER
     * - Se tem isIncome = true → DEPOSIT (entrada)
     * - Se tem isIncome = false ou não fornecido → PAYMENT (saída)
     */
    private TransactionTypeEnum determineTransactionType(com.google.gson.JsonObject jsonObject, Transaction transaction) {
        // Se tem conta de destino, é uma transferência
        if (transaction.getDestinationAccountId() != null) {
            return TransactionTypeEnum.TRANSFER;
        }
        
        // Se não tem conta de destino, verifica se é entrada ou saída
        if (jsonObject.has("isIncome")) {
            boolean isIncome = jsonObject.get("isIncome").getAsBoolean();
            if (isIncome) {
                return TransactionTypeEnum.DEPOSIT; // Entrada de dinheiro
            } else {
                return TransactionTypeEnum.PAYMENT; // Saída de dinheiro
            }
        }
        
        // Por padrão, se não especificado, assume que é saída (PAYMENT)
        return TransactionTypeEnum.PAYMENT;
    }
    
    /**
     * Converte um valor monetário (pode ser número ou string formatada) para BigDecimal.
     * Aceita formatos como: 100.00, "100.00", "100,00", "R$ 100,00", "R$ 100.00"
     */
    private java.math.BigDecimal parseAmount(com.google.gson.JsonElement element) {
        if (element.isJsonNull()) {
            return null;
        }
        
        // Se for número, retorna diretamente
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return element.getAsBigDecimal();
        }
        
        // Se for string, tenta fazer parse removendo formatação
        String valueStr = element.getAsString();
        if (valueStr == null || valueStr.trim().isEmpty()) {
            return null;
        }
        
        // Remove formatação de moeda
        valueStr = valueStr.replace("R$", "")
                          .replace("$", "")
                          .replace("€", "")
                          .replace("£", "")
                          .trim();
        
        // Remove pontos (separadores de milhar)
        valueStr = valueStr.replace(".", "");
        
        // Substitui vírgula por ponto (padrão decimal brasileiro)
        valueStr = valueStr.replace(",", ".");
        
        try {
            return new java.math.BigDecimal(valueStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato de valor inválido: " + element.getAsString());
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonResponse.sendBadRequest(response, "ID é obrigatório para atualização");
                return;
            }
            
            Integer id = RequestParser.extractIdFromPath(pathInfo);
            if (id == null) {
                JsonResponse.sendBadRequest(response, "ID inválido");
                return;
            }
            
            Transaction transaction = RequestParser.parseJson(request, Transaction.class);
            transaction.setTransactionId(id);
            
            Transaction updatedTransaction = transactionService.updateTransaction(transaction);
            JsonResponse.sendSuccess(response, updatedTransaction);
        } catch (IllegalArgumentException e) {
            JsonResponse.sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao atualizar transação: " + e.getMessage());
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonResponse.sendBadRequest(response, "ID é obrigatório para remoção");
                return;
            }
            
            Integer id = RequestParser.extractIdFromPath(pathInfo);
            if (id == null) {
                JsonResponse.sendBadRequest(response, "ID inválido");
                return;
            }
            
            transactionService.deleteTransaction(id);
            JsonResponse.sendSuccess(response, "Transação removida com sucesso");
        } catch (IllegalArgumentException e) {
            JsonResponse.sendNotFound(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.sendInternalError(response, "Erro ao remover transação: " + e.getMessage());
        }
    }
}

