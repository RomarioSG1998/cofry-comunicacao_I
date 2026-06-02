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

-- Seed de Usuário de Teste (Senha: '123456' em hash BCrypt)
INSERT INTO usuario (nome, email, senha_hash, tipo_usuario) VALUES 
('Romário Jala', 'romario@cofry.com', '$2a$10$tM6n.iZ1bU1h6gA33mO52OxzI5jZ7a2xL09hQ4xKjW2/ZtJ6/3H6O', 'usuario'),
('Admin Cofry', 'admin@cofry.com', '$2a$10$tM6n.iZ1bU1h6gA33mO52OxzI5jZ7a2xL09hQ4xKjW2/ZtJ6/3H6O', 'admin');

-- Seed de Contas para o Usuário de Teste (id_usuario = 1)
INSERT INTO conta (id_usuario, saldo, instituicao) VALUES 
(1, 5000.00, 'Banco do Brasil'),
(1, 1500.50, 'Nu Conta');

-- Seed de Assinaturas (id_usuario = 1 assina plano Premium id_plano = 2)
INSERT INTO assinatura (id_usuario, id_plano, status, data_fim) VALUES
(1, 2, 'ativo', '2026-12-31');

-- Seed de Investimentos (id_usuario = 1)
INSERT INTO investimento (id_usuario, tipo_ativo, valor_aplicado, roi_atual) VALUES
(1, 'Renda Fixa', 15000.00, 10.5),
(1, 'Ações', 8500.00, 18.2),
(1, 'Cripto', 3200.00, 45.0);

-- Seed de Cartões de Crédito (id_usuario = 1)
INSERT INTO cartao_credito (id_usuario, limite, dia_vencimento) VALUES
(1, 10000.00, 10),
(1, 5000.00, 20);

-- Seed de Boletos DDA (id_usuario = 1)
INSERT INTO boleto_dda (id_usuario, cod_barras, vencimento, status) VALUES
(1, '34191.79001 01043.513184 91020.150008 7 90020000035000', '2026-06-10', 'pendente'),
(1, '23791.79001 01043.513184 91020.150008 7 90020000012000', '2026-06-15', 'pago');

-- Seed de Chaves Pix (id_usuario = 1, id_conta = 2)
INSERT INTO chave_pix (id_usuario, tipo_chave, valor_chave, id_conta) VALUES
(1, 'CPF', '123.456.789-00', 2),
(1, 'Aleatória', 'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 2);

-- Seed de Transações (id_usuario = 1)
INSERT INTO transacao (id_usuario, valor, data, comprovante_url, id_categoria, id_conta, id_cartao) VALUES
(1, 5000.00, '2026-06-01', NULL, 2, 1, NULL),  -- Salário recebido na Conta 1 (BB)
(1, -120.50, '2026-06-02', NULL, 1, 1, NULL),  -- Alimentação paga pela Conta 1
(1, -50.00, '2026-06-02', NULL, 3, 2, NULL),   -- Transporte pago pela Conta 2 (Nu)
(1, -45.00, '2026-06-02', NULL, 4, NULL, 1);   -- Lazer pago pelo Cartão 1
