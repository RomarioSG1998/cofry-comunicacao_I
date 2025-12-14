package org.example.DAO;

import org.example.Model.Usuario;

public class TesteDAO {
    public static void main(String[] args) {

        UserDAO dao = new UserDAO();

        System.out.println("=== TESTE 1: Criar Usuário ===");
        Usuario u1 = new Usuario();
        u1.setName("Patrick");
        u1.setEmail("patrick@email.com");
        u1.setPassword("1234");
        u1.setTipoUser("cliente");

        dao.salvar(u1);
        System.out.println("✓ Usuário criado com sucesso!");

        System.out.println("\n=== TESTE 2: Buscar Usuário por ID ===");
        Usuario u2 = dao.buscarPorId(1L);
        if (u2 != null) {
            System.out.println("✓ Usuário encontrado:");
            System.out.println("  - ID: " + u2.getIdUsuario());
            System.out.println("  - Nome: " + u2.getName());
            System.out.println("  - Email: " + u2.getEmail());
            System.out.println("  - Tipo: " + u2.getTipoUser());
        } else {
            System.out.println("✗ Usuário não encontrado.");
        }

        System.out.println("\n=== TESTE 3: Listar Todos os Usuários ===");
        java.util.List<Usuario> todos = dao.listarTodos();
        System.out.println("✓ Total de usuários encontrados: " + todos.size());
        for (Usuario u : todos) {
            System.out.println("  - ID: " + u.getIdUsuario() + " | Nome: " + u.getName() + " | Email: " + u.getEmail() + " | Tipo: " + u.getTipoUser());
        }

        System.out.println("\n=== TESTE 4: Buscar Usuário por Email ===");
        Usuario u3 = dao.buscarPorEmail("patrick@email.com");
        if (u3 != null) {
            System.out.println("✓ Usuário encontrado por email:");
            System.out.println("  - ID: " + u3.getIdUsuario());
            System.out.println("  - Nome: " + u3.getName());
            System.out.println("  - Email: " + u3.getEmail());
            System.out.println("  - Tipo: " + u3.getTipoUser());
        } else {
            System.out.println("✗ Usuário não encontrado por email.");
        }

        System.out.println("\n=== TESTE 5: Atualizar Usuário ===");
        if (u2 != null) {
            u2.setName("Patrick Atualizado");
            u2.setEmail("patrick.novo@email.com");
            u2.setTipoUser("admin");
            dao.atualizar(u2);
            System.out.println("✓ Usuário atualizado!");

            Usuario u4 = dao.buscarPorId(u2.getIdUsuario());
            if (u4 != null) {
                System.out.println("  - Novo Nome: " + u4.getName());
                System.out.println("  - Novo Email: " + u4.getEmail());
                System.out.println("  - Novo Tipo: " + u4.getTipoUser());
            }
        }

        System.out.println("\n=== TESTE 6: Deletar Usuário ===");
        if (u2 != null) {
            System.out.println("⚠ Teste de deletar comentado para não perder dados.");
            System.out.println("  Descomente a linha abaixo para testar:");
            System.out.println("  dao.deletar(u2.getIdUsuario());");
        }

        System.out.println("\n=== ✅ TODOS OS TESTES CONCLUÍDOS ===");
    }
}
