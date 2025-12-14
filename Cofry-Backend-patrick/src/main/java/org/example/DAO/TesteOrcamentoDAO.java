package org.example.DAO;

import org.example.Model.Orcamento;
import java.math.BigDecimal;

public class TesteOrcamentoDAO {
    public static void main(String[] args) {

        OrcamentoDAO dao = new OrcamentoDAO();

        System.out.println("=== TESTE 1: Criar Orçamento ===");
        Orcamento o1 = new Orcamento();
        o1.setIdUsuario(1);
        o1.setIdCategoria(1);
        o1.setValorLimite(new BigDecimal("500.00"));
        o1.setMesAno("2025-12");

        dao.salvar(o1);
        System.out.println("✓ Orçamento criado com sucesso!");

        System.out.println("\n=== TESTE 2: Buscar Orçamento por ID ===");
        Orcamento o2 = dao.buscarPorId(1);
        if (o2 != null) {
            System.out.println("✓ Orçamento encontrado:");
            System.out.println("  - ID: " + o2.getIdOrc());
            System.out.println("  - Usuário: " + o2.getIdUsuario());
            System.out.println("  - Categoria: " + o2.getIdCategoria());
            System.out.println("  - Valor Limite: R$ " + o2.getValorLimite());
            System.out.println("  - Mês/Ano: " + o2.getMesAno());
        } else {
            System.out.println("✗ Orçamento não encontrado.");
        }

        System.out.println("\n=== TESTE 3: Listar Todos os Orçamentos ===");
        java.util.List<Orcamento> todos = dao.listarTodos();
        System.out.println("✓ Total de orçamentos encontrados: " + todos.size());
        for (Orcamento o : todos) {
            System.out.println("  - ID: " + o.getIdOrc() + " | Usuário: " + o.getIdUsuario() + " | Categoria: " + o.getIdCategoria() + " | Valor: R$ " + o.getValorLimite() + " | Mês/Ano: " + o.getMesAno());
        }

        System.out.println("\n=== TESTE 4: Buscar Orçamentos por Usuário ===");
        java.util.List<Orcamento> porUsuario = dao.buscarPorUsuario(1);
        System.out.println("✓ Orçamentos do usuário 1: " + porUsuario.size());

        System.out.println("\n=== TESTE 5: Buscar Orçamentos por Categoria ===");
        java.util.List<Orcamento> porCategoria = dao.buscarPorCategoria(1);
        System.out.println("✓ Orçamentos da categoria 1: " + porCategoria.size());

        System.out.println("\n=== TESTE 6: Atualizar Orçamento ===");
        if (o2 != null) {
            o2.setValorLimite(new BigDecimal("750.00"));
            o2.setMesAno("2026-01");
            dao.atualizar(o2);
            System.out.println("✓ Orçamento atualizado!");

            Orcamento o3 = dao.buscarPorId(o2.getIdOrc());
            if (o3 != null) {
                System.out.println("  - Novo Valor Limite: R$ " + o3.getValorLimite());
                System.out.println("  - Novo Mês/Ano: " + o3.getMesAno());
            }
        }

        System.out.println("\n=== TESTE 7: Deletar Orçamento ===");
        if (o2 != null) {
            System.out.println("⚠ Teste de deletar comentado para não perder dados.");
            System.out.println("  Descomente a linha abaixo para testar:");
            System.out.println("  dao.deletar(o2.getIdOrc());");
        }

        System.out.println("\n=== ✅ TODOS OS TESTES CONCLUÍDOS ===");
    }
}

