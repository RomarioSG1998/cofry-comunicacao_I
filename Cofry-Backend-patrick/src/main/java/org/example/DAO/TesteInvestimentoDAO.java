package org.example.DAO;

import org.example.Model.Investimento;
import java.math.BigDecimal;

public class TesteInvestimentoDAO {
    public static void main(String[] args) {

        InvestimentoDAO dao = new InvestimentoDAO();

        System.out.println("=== TESTE 1: Criar Investimento ===");
        Investimento i1 = new Investimento();
        i1.setIdUsuario(1);
        i1.setTipoAtivo("Ações");
        i1.setValorAplicado(new BigDecimal("10000.00"));
        i1.setRoiAtual(new BigDecimal("5.5"));

        dao.salvar(i1);
        System.out.println("✓ Investimento criado com sucesso!");

        System.out.println("\n=== TESTE 2: Buscar Investimento por ID ===");
        Investimento i2 = dao.buscarPorId(1);
        if (i2 != null) {
            System.out.println("✓ Investimento encontrado:");
            System.out.println("  - ID: " + i2.getIdInvest());
            System.out.println("  - Usuário: " + i2.getIdUsuario());
            System.out.println("  - Tipo de Ativo: " + i2.getTipoAtivo());
            System.out.println("  - Valor Aplicado: R$ " + i2.getValorAplicado());
            System.out.println("  - ROI Atual: " + i2.getRoiAtual() + "%");
        } else {
            System.out.println("✗ Investimento não encontrado.");
        }

        System.out.println("\n=== TESTE 3: Listar Todos os Investimentos ===");
        java.util.List<Investimento> todos = dao.listarTodos();
        System.out.println("✓ Total de investimentos encontrados: " + todos.size());
        for (Investimento i : todos) {
            System.out.println("  - ID: " + i.getIdInvest() + " | Usuário: " + i.getIdUsuario() + " | Tipo: " + i.getTipoAtivo() + " | Valor: R$ " + i.getValorAplicado() + " | ROI: " + i.getRoiAtual() + "%");
        }

        System.out.println("\n=== TESTE 4: Buscar Investimentos por Usuário ===");
        java.util.List<Investimento> porUsuario = dao.buscarPorUsuario(1);
        System.out.println("✓ Investimentos do usuário 1: " + porUsuario.size());

        System.out.println("\n=== TESTE 5: Buscar Investimentos por Tipo de Ativo ===");
        java.util.List<Investimento> porTipo = dao.buscarPorTipoAtivo("Ações");
        System.out.println("✓ Investimentos do tipo 'Ações': " + porTipo.size());

        System.out.println("\n=== TESTE 6: Atualizar Investimento ===");
        if (i2 != null) {
            i2.setRoiAtual(new BigDecimal("7.2"));
            i2.setValorAplicado(new BigDecimal("12000.00"));
            dao.atualizar(i2);
            System.out.println("✓ Investimento atualizado!");

            Investimento i3 = dao.buscarPorId(i2.getIdInvest());
            if (i3 != null) {
                System.out.println("  - Novo Valor Aplicado: R$ " + i3.getValorAplicado());
                System.out.println("  - Novo ROI Atual: " + i3.getRoiAtual() + "%");
            }
        }

        System.out.println("\n=== TESTE 7: Deletar Investimento ===");
        if (i2 != null) {
            System.out.println("⚠ Teste de deletar comentado para não perder dados.");
            System.out.println("  Descomente a linha abaixo para testar:");
            System.out.println("  dao.deletar(i2.getIdInvest());");
        }

        System.out.println("\n=== ✅ TODOS OS TESTES CONCLUÍDOS ===");
    }
}

