package org.example.DAO;

import org.example.Model.Plano;
import java.math.BigDecimal;

public class TestePlanoDAO {
    public static void main(String[] args) {

        PlanoDAO dao = new PlanoDAO();

        System.out.println("=== TESTE 1: Criar Plano ===");
        Plano p1 = new Plano();
        p1.setNome("Plano Básico");
        p1.setPreco(new BigDecimal("29.90"));
        p1.setRecursos("Gestão de contas, Relatórios básicos");

        dao.salvar(p1);
        System.out.println("✓ Plano criado com sucesso!");

        System.out.println("\n=== TESTE 2: Buscar Plano por ID ===");
        Plano p2 = dao.buscarPorId(1);
        if (p2 != null) {
            System.out.println("✓ Plano encontrado:");
            System.out.println("  - ID: " + p2.getIdPlano());
            System.out.println("  - Nome: " + p2.getNome());
            System.out.println("  - Preço: R$ " + p2.getPreco());
            System.out.println("  - Recursos: " + p2.getRecursos());
        } else {
            System.out.println("✗ Plano não encontrado.");
        }

        System.out.println("\n=== TESTE 3: Listar Todos os Planos ===");
        java.util.List<Plano> todos = dao.listarTodos();
        System.out.println("✓ Total de planos encontrados: " + todos.size());
        for (Plano p : todos) {
            System.out.println("  - ID: " + p.getIdPlano() + " | Nome: " + p.getNome() + " | Preço: R$ " + p.getPreco());
        }

        System.out.println("\n=== TESTE 4: Atualizar Plano ===");
        if (p2 != null) {
            p2.setNome("Plano Premium");
            p2.setPreco(new BigDecimal("99.90"));
            p2.setRecursos("Gestão completa, Relatórios avançados, Suporte prioritário");
            dao.atualizar(p2);
            System.out.println("✓ Plano atualizado!");

            Plano p3 = dao.buscarPorId(p2.getIdPlano());
            if (p3 != null) {
                System.out.println("  - Novo Nome: " + p3.getNome());
                System.out.println("  - Novo Preço: R$ " + p3.getPreco());
                System.out.println("  - Novos Recursos: " + p3.getRecursos());
            }
        }

        System.out.println("\n=== TESTE 5: Deletar Plano ===");
        if (p2 != null) {
            System.out.println("⚠ Teste de deletar comentado para não perder dados.");
            System.out.println("  Descomente a linha abaixo para testar:");
            System.out.println("  dao.deletar(p2.getIdPlano());");
        }

        System.out.println("\n=== ✅ TODOS OS TESTES CONCLUÍDOS ===");
    }
}

