package org.example.DAO;

import org.example.Model.Categoria;

public class TesteCategoriaDAO {
    public static void main(String[] args) {

        CategoriaDAO dao = new CategoriaDAO();

        System.out.println("=== TESTE 1: Criar Categoria ===");
        Categoria c1 = new Categoria();
        c1.setNome("Alimentação");
        c1.setTipo("Despesa");
        c1.setIcone("🍔");

        dao.salvar(c1);
        System.out.println("✓ Categoria criada com sucesso!");

        System.out.println("\n=== TESTE 2: Buscar Categoria por ID ===");
        Categoria c2 = dao.buscarPorId(1);
        if (c2 != null) {
            System.out.println("✓ Categoria encontrada:");
            System.out.println("  - ID: " + c2.getIdCategoria());
            System.out.println("  - Nome: " + c2.getNome());
            System.out.println("  - Tipo: " + c2.getTipo());
            System.out.println("  - Ícone: " + c2.getIcone());
        } else {
            System.out.println("✗ Categoria não encontrada.");
        }

        System.out.println("\n=== TESTE 3: Listar Todas as Categorias ===");
        java.util.List<Categoria> todas = dao.listarTodos();
        System.out.println("✓ Total de categorias encontradas: " + todas.size());
        for (Categoria c : todas) {
            System.out.println("  - ID: " + c.getIdCategoria() + " | Nome: " + c.getNome() + " | Tipo: " + c.getTipo() + " | Ícone: " + c.getIcone());
        }

        System.out.println("\n=== TESTE 4: Buscar Categorias por Tipo ===");
        java.util.List<Categoria> porTipo = dao.buscarPorTipo("Despesa");
        System.out.println("✓ Categorias do tipo 'Despesa': " + porTipo.size());

        System.out.println("\n=== TESTE 5: Atualizar Categoria ===");
        if (c2 != null) {
            c2.setNome("Alimentação e Bebidas");
            c2.setIcone("🍕");
            dao.atualizar(c2);
            System.out.println("✓ Categoria atualizada!");

            Categoria c3 = dao.buscarPorId(c2.getIdCategoria());
            if (c3 != null) {
                System.out.println("  - Novo Nome: " + c3.getNome());
                System.out.println("  - Novo Ícone: " + c3.getIcone());
            }
        }

        System.out.println("\n=== TESTE 6: Deletar Categoria ===");
        if (c2 != null) {
            System.out.println("⚠ Teste de deletar comentado para não perder dados.");
            System.out.println("  Descomente a linha abaixo para testar:");
            System.out.println("  dao.deletar(c2.getIdCategoria());");
        }

        System.out.println("\n=== ✅ TODOS OS TESTES CONCLUÍDOS ===");
    }
}

