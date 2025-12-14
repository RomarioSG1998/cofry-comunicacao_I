package org.example.DAO;

import org.example.Model.CartaoCredito;
import java.math.BigDecimal;

public class TesteCartaoCreditoDAO {
    public static void main(String[] args) {

        CartaoCreditoDAO dao = new CartaoCreditoDAO();

        System.out.println("=== TESTE 1: Criar Cartão de Crédito ===");
        CartaoCredito cc1 = new CartaoCredito();
        cc1.setIdUsuario(1);
        cc1.setLimite(new BigDecimal("5000.00"));
        cc1.setDiaVencimento(15);

        dao.salvar(cc1);
        System.out.println("✓ Cartão de crédito criado com sucesso!");

        System.out.println("\n=== TESTE 2: Buscar Cartão de Crédito por ID ===");
        CartaoCredito cc2 = dao.buscarPorId(1);
        if (cc2 != null) {
            System.out.println("✓ Cartão de crédito encontrado:");
            System.out.println("  - ID: " + cc2.getIdCartao());
            System.out.println("  - Usuário: " + cc2.getIdUsuario());
            System.out.println("  - Limite: R$ " + cc2.getLimite());
            System.out.println("  - Dia de Vencimento: " + cc2.getDiaVencimento());
        } else {
            System.out.println("✗ Cartão de crédito não encontrado.");
        }

        System.out.println("\n=== TESTE 3: Listar Todos os Cartões de Crédito ===");
        java.util.List<CartaoCredito> todos = dao.listarTodos();
        System.out.println("✓ Total de cartões encontrados: " + todos.size());
        for (CartaoCredito cc : todos) {
            System.out.println("  - ID: " + cc.getIdCartao() + " | Usuário: " + cc.getIdUsuario() + " | Limite: R$ " + cc.getLimite() + " | Dia Vencimento: " + cc.getDiaVencimento());
        }

        System.out.println("\n=== TESTE 4: Buscar Cartões de Crédito por Usuário ===");
        java.util.List<CartaoCredito> porUsuario = dao.buscarPorUsuario(1);
        System.out.println("✓ Cartões do usuário 1: " + porUsuario.size());

        System.out.println("\n=== TESTE 5: Atualizar Cartão de Crédito ===");
        if (cc2 != null) {
            cc2.setLimite(new BigDecimal("8000.00"));
            cc2.setDiaVencimento(20);
            dao.atualizar(cc2);
            System.out.println("✓ Cartão de crédito atualizado!");

            CartaoCredito cc3 = dao.buscarPorId(cc2.getIdCartao());
            if (cc3 != null) {
                System.out.println("  - Novo Limite: R$ " + cc3.getLimite());
                System.out.println("  - Novo Dia de Vencimento: " + cc3.getDiaVencimento());
            }
        }

        System.out.println("\n=== TESTE 6: Deletar Cartão de Crédito ===");
        if (cc2 != null) {
            System.out.println("⚠ Teste de deletar comentado para não perder dados.");
            System.out.println("  Descomente a linha abaixo para testar:");
            System.out.println("  dao.deletar(cc2.getIdCartao());");
        }

        System.out.println("\n=== ✅ TODOS OS TESTES CONCLUÍDOS ===");
    }
}

