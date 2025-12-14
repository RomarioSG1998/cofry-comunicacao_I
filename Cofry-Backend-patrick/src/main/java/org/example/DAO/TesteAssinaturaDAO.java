package org.example.DAO;

import org.example.Model.Assinatura;
import java.time.LocalDate;

public class TesteAssinaturaDAO {
    public static void main(String[] args) {

        AssinaturaDAO dao = new AssinaturaDAO();

        System.out.println("=== TESTE 1: Criar Assinatura ===");
        Assinatura a1 = new Assinatura();
        a1.setIdUsuario(1);
        a1.setIdPlano(1);
        a1.setStatus("ativa");
        a1.setDataFim(LocalDate.now().plusMonths(1));

        dao.salvar(a1);
        System.out.println("✓ Assinatura criada com sucesso!");

        System.out.println("\n=== TESTE 2: Buscar Assinatura por ID ===");
        Assinatura a2 = dao.buscarPorId(1);
        if (a2 != null) {
            System.out.println("✓ Assinatura encontrada:");
            System.out.println("  - ID: " + a2.getIdAssin());
            System.out.println("  - Usuário: " + a2.getIdUsuario());
            System.out.println("  - Plano: " + a2.getIdPlano());
            System.out.println("  - Status: " + a2.getStatus());
            System.out.println("  - Data Fim: " + a2.getDataFim());
        } else {
            System.out.println("✗ Assinatura não encontrada.");
        }

        System.out.println("\n=== TESTE 3: Listar Todas as Assinaturas ===");
        java.util.List<Assinatura> todas = dao.listarTodos();
        System.out.println("✓ Total de assinaturas encontradas: " + todas.size());
        for (Assinatura a : todas) {
            System.out.println("  - ID: " + a.getIdAssin() + " | Usuário: " + a.getIdUsuario() + " | Status: " + a.getStatus());
        }

        System.out.println("\n=== TESTE 4: Buscar Assinaturas por Usuário ===");
        java.util.List<Assinatura> porUsuario = dao.buscarPorUsuario(1);
        System.out.println("✓ Assinaturas do usuário 1: " + porUsuario.size());

        System.out.println("\n=== TESTE 5: Atualizar Assinatura ===");
        if (a2 != null) {
            a2.setStatus("cancelada");
            a2.setDataFim(LocalDate.now().plusDays(5));
            dao.atualizar(a2);
            System.out.println("✓ Assinatura atualizada!");

            Assinatura a3 = dao.buscarPorId(a2.getIdAssin());
            if (a3 != null) {
                System.out.println("  - Novo Status: " + a3.getStatus());
                System.out.println("  - Nova Data Fim: " + a3.getDataFim());
            }
        }

        System.out.println("\n=== TESTE 6: Deletar Assinatura ===");
        if (a2 != null) {
            System.out.println("⚠ Teste de deletar comentado para não perder dados.");
            System.out.println("  Descomente a linha abaixo para testar:");
            System.out.println("  dao.deletar(a2.getIdAssin());");
        }

        System.out.println("\n=== ✅ TODOS OS TESTES CONCLUÍDOS ===");
    }
}

