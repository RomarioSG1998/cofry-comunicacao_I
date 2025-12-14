package org.example.DAO;

import org.example.Model.Conta;
import java.math.BigDecimal;

public class TesteContaDAO {
    public static void main(String[] args) {

        ContaDAO dao = new ContaDAO();

        System.out.println("=== TESTE 1: Criar Conta ===");
        Conta c1 = new Conta();
        c1.setIdUsuario(1);
        c1.setSaldo(new BigDecimal("1500.50"));
        c1.setInstituicao("Banco do Brasil");

        dao.salvar(c1);
        System.out.println("✓ Conta criada com sucesso!");

        System.out.println("\n=== TESTE 2: Buscar Conta por ID ===");
        Conta c2 = dao.buscarPorId(1);
        if (c2 != null) {
            System.out.println("✓ Conta encontrada:");
            System.out.println("  - ID: " + c2.getIdConta());
            System.out.println("  - Usuário: " + c2.getIdUsuario());
            System.out.println("  - Saldo: R$ " + c2.getSaldo());
            System.out.println("  - Instituição: " + c2.getInstituicao());
        } else {
            System.out.println("✗ Conta não encontrada.");
        }

        System.out.println("\n=== TESTE 3: Listar Todas as Contas ===");
        java.util.List<Conta> todas = dao.listarTodos();
        System.out.println("✓ Total de contas encontradas: " + todas.size());
        for (Conta c : todas) {
            System.out.println("  - ID: " + c.getIdConta() + " | Usuário: " + c.getIdUsuario() + " | Saldo: R$ " + c.getSaldo() + " | Instituição: " + c.getInstituicao());
        }

        System.out.println("\n=== TESTE 4: Buscar Contas por Usuário ===");
        java.util.List<Conta> porUsuario = dao.buscarPorUsuario(1);
        System.out.println("✓ Contas do usuário 1: " + porUsuario.size());

        System.out.println("\n=== TESTE 5: Atualizar Conta ===");
        if (c2 != null) {
            c2.setSaldo(new BigDecimal("2000.75"));
            c2.setInstituicao("Itaú");
            dao.atualizar(c2);
            System.out.println("✓ Conta atualizada!");

            Conta c3 = dao.buscarPorId(c2.getIdConta());
            if (c3 != null) {
                System.out.println("  - Novo Saldo: R$ " + c3.getSaldo());
                System.out.println("  - Nova Instituição: " + c3.getInstituicao());
            }
        }

        System.out.println("\n=== TESTE 6: Deletar Conta ===");
        if (c2 != null) {
            System.out.println("⚠ Teste de deletar comentado para não perder dados.");
            System.out.println("  Descomente a linha abaixo para testar:");
            System.out.println("  dao.deletar(c2.getIdConta());");
        }

        System.out.println("\n=== ✅ TODOS OS TESTES CONCLUÍDOS ===");
    }
}

