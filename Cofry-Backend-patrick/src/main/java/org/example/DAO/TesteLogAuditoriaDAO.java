package org.example.DAO;

import org.example.Model.LogAuditoria;
import java.time.LocalDate;

public class TesteLogAuditoriaDAO {
    public static void main(String[] args) {

        LogAuditoriaDAO dao = new LogAuditoriaDAO();

        System.out.println("=== TESTE 1: Criar Log de Auditoria ===");
        LogAuditoria l1 = new LogAuditoria();
        l1.setIdAdmin(1);
        l1.setAcao("Criação de novo usuário");
        l1.setDataHora(LocalDate.now());

        dao.salvar(l1);
        System.out.println("✓ Log de auditoria criado com sucesso!");

        System.out.println("\n=== TESTE 2: Buscar Log de Auditoria por ID ===");
        LogAuditoria l2 = dao.buscarPorId(1);
        if (l2 != null) {
            System.out.println("✓ Log de auditoria encontrado:");
            System.out.println("  - ID: " + l2.getIdLog());
            System.out.println("  - Admin: " + l2.getIdAdmin());
            System.out.println("  - Ação: " + l2.getAcao());
            System.out.println("  - Data/Hora: " + l2.getDataHora());
        } else {
            System.out.println("✗ Log de auditoria não encontrado.");
        }

        System.out.println("\n=== TESTE 3: Listar Todos os Logs de Auditoria ===");
        java.util.List<LogAuditoria> todos = dao.listarTodos();
        System.out.println("✓ Total de logs encontrados: " + todos.size());
        for (LogAuditoria l : todos) {
            System.out.println("  - ID: " + l.getIdLog() + " | Admin: " + l.getIdAdmin() + " | Ação: " + l.getAcao() + " | Data: " + l.getDataHora());
        }

        System.out.println("\n=== TESTE 4: Buscar Logs de Auditoria por Admin ===");
        java.util.List<LogAuditoria> porAdmin = dao.buscarPorAdmin(1);
        System.out.println("✓ Logs do admin 1: " + porAdmin.size());

        System.out.println("\n=== TESTE 5: Atualizar Log de Auditoria ===");
        if (l2 != null) {
            l2.setAcao("Atualização de usuário existente");
            l2.setDataHora(LocalDate.now());
            dao.atualizar(l2);
            System.out.println("✓ Log de auditoria atualizado!");

            LogAuditoria l3 = dao.buscarPorId(l2.getIdLog());
            if (l3 != null) {
                System.out.println("  - Nova Ação: " + l3.getAcao());
                System.out.println("  - Nova Data/Hora: " + l3.getDataHora());
            }
        }

        System.out.println("\n=== TESTE 6: Deletar Log de Auditoria ===");
        if (l2 != null) {
            System.out.println("⚠ Teste de deletar comentado para não perder dados.");
            System.out.println("  Descomente a linha abaixo para testar:");
            System.out.println("  dao.deletar(l2.getIdLog());");
        }

        System.out.println("\n=== ✅ TODOS OS TESTES CONCLUÍDOS ===");
    }
}

