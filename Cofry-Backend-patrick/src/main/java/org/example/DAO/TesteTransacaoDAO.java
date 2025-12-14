package org.example.DAO;

import org.example.Model.Transacao;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TesteTransacaoDAO {
    public static void main(String[] args) {

        TransacaoDAO dao = new TransacaoDAO();

        System.out.println("=== TESTE 1: Criar Transação ===");
        Transacao t1 = new Transacao();
        t1.setIdUsuario(1);
        t1.setValor(new BigDecimal("150.75"));
        t1.setData(LocalDate.now());
        t1.setComprovanteUrl("https://example.com/comprovante1.pdf");
        t1.setIdCategoria(1);
        t1.setIdConta(1);
        t1.setIdCartao(null);

        dao.salvar(t1);
        System.out.println("✓ Transação criada com sucesso!");

        System.out.println("\n=== TESTE 2: Buscar Transação por ID ===");
        Transacao t2 = dao.buscarPorId(1);
        if (t2 != null) {
            System.out.println("✓ Transação encontrada:");
            System.out.println("  - ID: " + t2.getIdTrans());
            System.out.println("  - Usuário: " + t2.getIdUsuario());
            System.out.println("  - Valor: R$ " + t2.getValor());
            System.out.println("  - Data: " + t2.getData());
            System.out.println("  - Comprovante URL: " + t2.getComprovanteUrl());
            System.out.println("  - Categoria: " + t2.getIdCategoria());
            System.out.println("  - Conta: " + t2.getIdConta());
            System.out.println("  - Cartão: " + t2.getIdCartao());
        } else {
            System.out.println("✗ Transação não encontrada.");
        }

        System.out.println("\n=== TESTE 3: Listar Todas as Transações ===");
        java.util.List<Transacao> todas = dao.listarTodos();
        System.out.println("✓ Total de transações encontradas: " + todas.size());
        for (Transacao t : todas) {
            System.out.println("  - ID: " + t.getIdTrans() + " | Usuário: " + t.getIdUsuario() + " | Valor: R$ " + t.getValor() + " | Data: " + t.getData());
        }

        System.out.println("\n=== TESTE 4: Buscar Transações por Usuário ===");
        java.util.List<Transacao> porUsuario = dao.buscarPorUsuario(1);
        System.out.println("✓ Transações do usuário 1: " + porUsuario.size());

        System.out.println("\n=== TESTE 5: Buscar Transações por Categoria ===");
        java.util.List<Transacao> porCategoria = dao.buscarPorCategoria(1);
        System.out.println("✓ Transações da categoria 1: " + porCategoria.size());

        System.out.println("\n=== TESTE 6: Atualizar Transação ===");
        if (t2 != null) {
            t2.setValor(new BigDecimal("200.50"));
            t2.setData(LocalDate.now().plusDays(1));
            t2.setComprovanteUrl("https://example.com/comprovante2.pdf");
            dao.atualizar(t2);
            System.out.println("✓ Transação atualizada!");

            Transacao t3 = dao.buscarPorId(t2.getIdTrans());
            if (t3 != null) {
                System.out.println("  - Novo Valor: R$ " + t3.getValor());
                System.out.println("  - Nova Data: " + t3.getData());
                System.out.println("  - Novo Comprovante URL: " + t3.getComprovanteUrl());
            }
        }

        System.out.println("\n=== TESTE 7: Deletar Transação ===");
        if (t2 != null) {
            System.out.println("⚠ Teste de deletar comentado para não perder dados.");
            System.out.println("  Descomente a linha abaixo para testar:");
            System.out.println("  dao.deletar(t2.getIdTrans());");
        }

        System.out.println("\n=== ✅ TODOS OS TESTES CONCLUÍDOS ===");
    }
}

