package org.example.DAO;

import org.example.Model.MetaPoupanca;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TesteMetaPoupancaDAO {
    public static void main(String[] args) {

        MetaPoupancaDAO dao = new MetaPoupancaDAO();

        System.out.println("=== TESTE 1: Criar Meta de Poupança ===");
        MetaPoupanca m1 = new MetaPoupanca();
        m1.setIdUsuario(1);
        m1.setValorAlvo(new BigDecimal("5000.00"));
        m1.setValorAtual(new BigDecimal("1500.00"));
        m1.setDataLimite(LocalDate.now().plusMonths(6));

        dao.salvar(m1);
        System.out.println("✓ Meta de poupança criada com sucesso!");

        System.out.println("\n=== TESTE 2: Buscar Meta de Poupança por ID ===");
        MetaPoupanca m2 = dao.buscarPorId(1);
        if (m2 != null) {
            System.out.println("✓ Meta de poupança encontrada:");
            System.out.println("  - ID: " + m2.getIdMeta());
            System.out.println("  - Usuário: " + m2.getIdUsuario());
            System.out.println("  - Valor Alvo: R$ " + m2.getValorAlvo());
            System.out.println("  - Valor Atual: R$ " + m2.getValorAtual());
            System.out.println("  - Data Limite: " + m2.getDataLimite());
        } else {
            System.out.println("✗ Meta de poupança não encontrada.");
        }

        System.out.println("\n=== TESTE 3: Listar Todas as Metas de Poupança ===");
        java.util.List<MetaPoupanca> todas = dao.listarTodos();
        System.out.println("✓ Total de metas encontradas: " + todas.size());
        for (MetaPoupanca m : todas) {
            System.out.println("  - ID: " + m.getIdMeta() + " | Usuário: " + m.getIdUsuario() + " | Valor Alvo: R$ " + m.getValorAlvo() + " | Valor Atual: R$ " + m.getValorAtual());
        }

        System.out.println("\n=== TESTE 4: Buscar Metas de Poupança por Usuário ===");
        java.util.List<MetaPoupanca> porUsuario = dao.buscarPorUsuario(1);
        System.out.println("✓ Metas do usuário 1: " + porUsuario.size());

        System.out.println("\n=== TESTE 5: Atualizar Meta de Poupança ===");
        if (m2 != null) {
            m2.setValorAtual(new BigDecimal("2000.00"));
            m2.setDataLimite(LocalDate.now().plusMonths(8));
            dao.atualizar(m2);
            System.out.println("✓ Meta de poupança atualizada!");

            MetaPoupanca m3 = dao.buscarPorId(m2.getIdMeta());
            if (m3 != null) {
                System.out.println("  - Novo Valor Atual: R$ " + m3.getValorAtual());
                System.out.println("  - Nova Data Limite: " + m3.getDataLimite());
            }
        }

        System.out.println("\n=== TESTE 6: Deletar Meta de Poupança ===");
        if (m2 != null) {
            System.out.println("⚠ Teste de deletar comentado para não perder dados.");
            System.out.println("  Descomente a linha abaixo para testar:");
            System.out.println("  dao.deletar(m2.getIdMeta());");
        }

        System.out.println("\n=== ✅ TODOS OS TESTES CONCLUÍDOS ===");
    }
}

