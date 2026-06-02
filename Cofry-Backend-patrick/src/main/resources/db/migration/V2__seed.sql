-- Flyway Migration V2: Database Seeds

-- Seed de Planos
INSERT INTO plano (nome, preco, recursos) VALUES 
('Gratuito', 0.00, 'Acesso básico a controle de transações, 1 conta bancária'),
('Premium', 19.90, 'Acesso ilimitado a contas, cartões de crédito, relatórios avançados e exportação de PDF'),
('Familiar', 39.90, 'Acesso ilimitado para até 4 membros da família, orçamentos compartilhados');

-- Seed de Categorias de Transação
INSERT INTO categoria (nome, tipo, icone) VALUES 
('Alimentação', 'despesa', 'utensils'),
('Salário', 'receita', 'briefcase'),
('Transporte', 'despesa', 'car'),
('Lazer', 'despesa', 'film'),
('Saúde', 'despesa', 'heartbeat'),
('Educação', 'despesa', 'graduation-cap'),
('Investimentos', 'receita', 'chart-line'),
('Moradia', 'despesa', 'home');

-- Seed de Usuário de Teste Único (Senha: '123456' em hash BCrypt)
INSERT INTO usuario (nome, email, senha_hash, tipo_usuario) VALUES 
('Romário Jala', 'romario@cofry.com', '$2a$10$tM6n.iZ1bU1h6gA33mO52OxzI5jZ7a2xL09hQ4xKjW2/ZtJ6/3H6O', 'usuario');

-- Seed de Contas para o Usuário de Teste (id_usuario = 1)
INSERT INTO conta (id_usuario, saldo, instituicao) VALUES 
(1, 5420.50, 'Banco do Brasil'),
(1, 1500.80, 'Nu Conta'),
(1, 350.00, 'C6 Bank'),
(1, 12000.00, 'Itaú Unibanco'),
(1, 2450.00, 'Santander'),
(1, 89.90, 'Bradesco');

-- Seed de Assinaturas (id_usuario = 1 assina plano Premium id_plano = 2)
INSERT INTO assinatura (id_usuario, id_plano, status, data_fim) VALUES
(1, 2, 'ativo', '2026-12-31');

-- Seed de Investimentos (id_usuario = 1)
INSERT INTO investimento (id_usuario, tipo_ativo, valor_aplicado, roi_atual) VALUES
(1, 'Tesouro Direto', 15000.00, 10.50),
(1, 'CDB Nu invest', 8500.00, 11.20),
(1, 'Ações (VALE3)', 12400.00, 15.60),
(1, 'Ações (PETR4)', 9800.00, 24.30),
(1, 'FIIs (HGLG11)', 6200.00, 9.80),
(1, 'FIIs (MXRF11)', 4300.00, 10.15),
(1, 'Bitcoin (BTC)', 5200.00, 68.40),
(1, 'Ethereum (ETH)', 2800.00, 42.10),
(1, 'LCI/LCA Banco do Brasil', 22000.00, 9.50);

-- Seed de Cartões de Crédito (id_usuario = 1)
INSERT INTO cartao_credito (id_usuario, limite, dia_vencimento) VALUES
(1, 12000.00, 10), -- Cartão BB Ourocard
(1, 6500.00, 15),  -- Cartão Nubank
(1, 4000.00, 20),  -- Cartão C6 Carbon
(1, 25000.00, 5);   -- Cartão Itaú Personalité

-- Seed de Boletos DDA (id_usuario = 1)
INSERT INTO boleto_dda (id_usuario, cod_barras, vencimento, status) VALUES
(1, '34191.79001 01043.513184 91020.150008 7 90020000035000', '2026-06-10', 'pendente'),
(1, '23791.79001 01043.513184 91020.150008 7 90020000012000', '2026-06-15', 'pago'),
(1, '00190.00009 02345.678901 12345.678902 3 95400000028000', '2026-06-20', 'pendente'),
(1, '10492.34567 89012.345678 90123.456789 1 95450000095000', '2026-06-25', 'pendente'),
(1, '34191.00000 12345.678900 12345.678900 9 95200000008990', '2026-05-10', 'pago'),
(1, '03399.12345 67890.123456 78901.234567 4 95300000015000', '2026-05-28', 'pago');

-- Seed de Chaves Pix (id_usuario = 1, id_conta = 2)
INSERT INTO chave_pix (id_usuario, tipo_chave, valor_chave, id_conta) VALUES
(1, 'CPF', '123.456.789-00', 2),
(1, 'E-mail', 'romario@cofry.com', 2),
(1, 'Telefone', '+55 (85) 99999-9999', 4),
(1, 'Aleatória', 'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 1);

-- Seed de Transações (id_usuario = 1)
INSERT INTO transacao (id_usuario, valor, data, comprovante_url, id_categoria, id_conta, id_cartao) VALUES
-- Maio de 2026
(1, 6500.00, '2026-05-01', NULL, 2, 4, NULL),   -- Salário Itaú
(1, -2100.00, '2026-05-05', NULL, 8, 4, NULL),  -- Aluguel/Moradia Itaú
(1, -150.00, '2026-05-06', NULL, 3, 2, NULL),   -- Combustível Nu
(1, -450.30, '2026-05-10', NULL, 1, 1, NULL),   -- Supermercado BB
(1, -89.90, '2026-05-12', NULL, 4, NULL, 2),    -- Streaming Netflix/Spotify Nubank Card
(1, 120.00, '2026-05-15', NULL, 7, 2, NULL),    -- Rendimento CDB Nu
(1, -300.00, '2026-05-18', NULL, 5, 5, NULL),   -- Consulta Médica Santander
(1, -120.00, '2026-05-20', NULL, 4, NULL, 1),    -- Cinema e Jantar Ourocard BB
(1, -85.50, '2026-05-22', NULL, 3, 2, NULL),    -- Uber Nu
(1, -1100.00, '2026-05-25', NULL, 6, 1, NULL),  -- Curso Online BB
(1, 250.00, '2026-05-28', NULL, 2, 2, NULL),    -- Freelance de Design Nu
(1, -320.00, '2026-05-30', NULL, 1, NULL, 3),   -- Restaurante C6 Card

-- Junho de 2026
(1, 7200.00, '2026-06-01', NULL, 2, 4, NULL),   -- Salário Itaú (Promovido)
(1, -2100.00, '2026-06-02', NULL, 8, 4, NULL),  -- Aluguel/Moradia Itaú
(1, -55.00, '2026-06-02', NULL, 3, 2, NULL),    -- Recarga de Transporte Nu
(1, -180.00, '2026-06-03', NULL, 1, NULL, 1),   -- Jantar Japonês Ourocard BB
(1, -34.90, '2026-06-04', NULL, 5, 2, NULL),    -- Farmácia Nu
(1, -89.90, '2026-06-05', NULL, 4, NULL, 2),    -- Assinaturas Nubank Card
(1, 350.00, '2026-06-06', NULL, 7, 1, NULL),    -- Rendimentos LCI Banco do Brasil
(1, -580.00, '2026-06-08', NULL, 1, 1, NULL),   -- Rancho Mensal BB
(1, -120.00, '2026-06-09', NULL, 3, NULL, 3);   -- Combustível C6 Card

-- Seed de Orçamentos (Orcamento por Categoria para id_usuario = 1)
INSERT INTO orcamento (id_usuario, id_categoria, valor_limite, mes_ano) VALUES
(1, 1, 1200.00, '2026-06'), -- Limite para Alimentação em Junho/2026
(1, 3, 400.00, '2026-06'),  -- Limite para Transporte em Junho/2026
(1, 4, 800.00, '2026-06');  -- Limite para Lazer em Junho/2026

-- Seed de Metas de Poupança (id_usuario = 1)
INSERT INTO meta_poupanca (id_usuario, valor_alvo, valor_atual, data_limite) VALUES
(1, 50000.00, 15000.00, '2027-12-31'), -- Reserva de Emergência
(1, 15000.00, 3200.00, '2026-11-30'),  -- Viagem de Férias
(1, 8000.00, 0.00, '2027-06-30');      -- Trocar de Computador

-- Seed de Logs de Auditoria (id_admin = 1 referenciando o usuário único Romário)
INSERT INTO log_auditoria (id_admin, acao, data_hora) VALUES
(1, 'Criação de novos planos do Cofry', '2026-06-01'),
(1, 'Homologação do ambiente de testes do usuário Romário', '2026-06-02');
