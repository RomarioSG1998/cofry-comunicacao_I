package org.example.DAO;

import org.example.Model.BoletoDDA;
import java.time.LocalDate;

public class TesteBoletoDDADAO {
    public static void main(String[] args) {

        BoletoDDADAO dao = new BoletoDDADAO();

        System.out.println("=== TESTE 1: Criar Boleto DDA ===");
        BoletoDDA b1 = new BoletoDDA();
        b1.setIdUsuario(1);
        b1.setCodBarras("34191090000000000001234567890123456789012345");
        b1.setVencimento(LocalDate.now().plusDays(30));
        b1.setStatus("Pendente");

        dao.salvar(b1);
        System.out.println("✓ Boleto DDA criado com sucesso!");

        System.out.println("\n=== TESTE 2: Buscar Boleto DDA por ID ===");
        BoletoDDA b2 = dao.buscarPorId(1);
        if (b2 != null) {
            System.out.println("✓ Boleto DDA encontrado:");
            System.out.println("  - ID: " + b2.getIdBoleto());
            System.out.println("  - Usuário: " + b2.getIdUsuario());
            System.out.println("  - Código de Barras: " + b2.getCodBarras());
            System.out.println("  - Vencimento: " + b2.getVencimento());
            System.out.println("  - Status: " + b2.getStatus());
        } else {
            System.out.println("✗ Boleto DDA não encontrado.");
        }

        System.out.println("\n=== TESTE 3: Listar Todos os Boletos DDA ===");
        java.util.List<BoletoDDA> todos = dao.listarTodos();
        System.out.println("✓ Total de boletos encontrados: " + todos.size());
        for (BoletoDDA b : todos) {
            System.out.println("  - ID: " + b.getIdBoleto() + " | Usuário: " + b.getIdUsuario() + " | Vencimento: " + b.getVencimento() + " | Status: " + b.getStatus());
        }

        System.out.println("\n=== TESTE 4: Buscar Boletos DDA por Usuário ===");
        java.util.List<BoletoDDA> porUsuario = dao.buscarPorUsuario(1);
        System.out.println("✓ Boletos do usuário 1: " + porUsuario.size());

        System.out.println("\n=== TESTE 5: Buscar Boletos DDA por Status ===");
        java.util.List<BoletoDDA> porStatus = dao.buscarPorStatus("Pendente");
        System.out.println("✓ Boletos com status 'Pendente': " + porStatus.size());

        System.out.println("\n=== TESTE 6: Atualizar Boleto DDA ===");
        if (b2 != null) {
            b2.setStatus("Pago");
            b2.setVencimento(LocalDate.now().plusDays(60));
            dao.atualizar(b2);
            System.out.println("✓ Boleto DDA atualizado!");

            BoletoDDA b3 = dao.buscarPorId(b2.getIdBoleto());
            if (b3 != null) {
                System.out.println("  - Novo Status: " + b3.getStatus());
                System.out.println("  - Novo Vencimento: " + b3.getVencimento());
            }
        }

        System.out.println("\n=== TESTE 7: Deletar Boleto DDA ===");
        if (b2 != null) {
            System.out.println("⚠ Teste de deletar comentado para não perder dados.");
            System.out.println("  Descomente a linha abaixo para testar:");
            System.out.println("  dao.deletar(b2.getIdBoleto());");
        }

        System.out.println("\n=== ✅ TODOS OS TESTES CONCLUÍDOS ===");
    }
}

