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

-- Seed de Usuário de Teste
INSERT INTO usuario (nome, email, senha_hash, tipo_usuario) VALUES 
('Romário Jala', 'romario@cofry.com', '123456', 'usuario'),
('Admin Cofry', 'admin@cofry.com', 'admin123', 'admin');

-- Seed de Contas para o Usuário de Teste (id_usuario = 1)
INSERT INTO conta (id_usuario, saldo, instituicao) VALUES 
(1, 5000.00, 'Banco do Brasil'),
(1, 1500.50, 'Nu Conta');
