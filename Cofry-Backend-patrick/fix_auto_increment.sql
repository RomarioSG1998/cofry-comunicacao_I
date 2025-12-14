-- ================================================================================
-- SCRIPT PARA ADICIONAR AUTO-INCREMENT EM TODAS AS PRIMARY KEYS
-- Banco: postgres
-- Host: cofry-db.cc5w4muoa5ca.us-east-1.rds.amazonaws.com
-- ================================================================================
-- IMPORTANTE: Execute este script com cuidado. Faça backup antes!
-- ================================================================================

-- Método usado: Criar sequences e configurar como DEFAULT
-- Compatível com PostgreSQL 9.1+

-- ================================================================================
-- TABELA: assinatura
-- ================================================================================
-- Criar sequence para id_assin
CREATE SEQUENCE IF NOT EXISTS assinatura_id_assin_seq;

-- Configurar a coluna para usar a sequence
ALTER TABLE assinatura 
    ALTER COLUMN id_assin SET DEFAULT nextval('assinatura_id_assin_seq');

-- Configurar a sequence para começar do próximo valor disponível
SELECT setval('assinatura_id_assin_seq', COALESCE((SELECT MAX(id_assin) FROM assinatura), 0) + 1, false);

-- ================================================================================
-- TABELA: boleto_dda
-- ================================================================================
CREATE SEQUENCE IF NOT EXISTS boleto_dda_id_boleto_seq;
ALTER TABLE boleto_dda 
    ALTER COLUMN id_boleto SET DEFAULT nextval('boleto_dda_id_boleto_seq');
SELECT setval('boleto_dda_id_boleto_seq', COALESCE((SELECT MAX(id_boleto) FROM boleto_dda), 0) + 1, false);

-- ================================================================================
-- TABELA: cartao_credito
-- ================================================================================
CREATE SEQUENCE IF NOT EXISTS cartao_credito_id_cartao_seq;
ALTER TABLE cartao_credito 
    ALTER COLUMN id_cartao SET DEFAULT nextval('cartao_credito_id_cartao_seq');
SELECT setval('cartao_credito_id_cartao_seq', COALESCE((SELECT MAX(id_cartao) FROM cartao_credito), 0) + 1, false);

-- ================================================================================
-- TABELA: categoria
-- ================================================================================
CREATE SEQUENCE IF NOT EXISTS categoria_id_categoria_seq;
ALTER TABLE categoria 
    ALTER COLUMN id_categoria SET DEFAULT nextval('categoria_id_categoria_seq');
SELECT setval('categoria_id_categoria_seq', COALESCE((SELECT MAX(id_categoria) FROM categoria), 0) + 1, false);

-- ================================================================================
-- TABELA: conta
-- ================================================================================
CREATE SEQUENCE IF NOT EXISTS conta_id_conta_seq;
ALTER TABLE conta 
    ALTER COLUMN id_conta SET DEFAULT nextval('conta_id_conta_seq');
SELECT setval('conta_id_conta_seq', COALESCE((SELECT MAX(id_conta) FROM conta), 0) + 1, false);

-- ================================================================================
-- TABELA: investimento
-- ================================================================================
CREATE SEQUENCE IF NOT EXISTS investimento_id_invest_seq;
ALTER TABLE investimento 
    ALTER COLUMN id_invest SET DEFAULT nextval('investimento_id_invest_seq');
SELECT setval('investimento_id_invest_seq', COALESCE((SELECT MAX(id_invest) FROM investimento), 0) + 1, false);

-- ================================================================================
-- TABELA: log_auditoria
-- ================================================================================
CREATE SEQUENCE IF NOT EXISTS log_auditoria_id_log_seq;
ALTER TABLE log_auditoria 
    ALTER COLUMN id_log SET DEFAULT nextval('log_auditoria_id_log_seq');
SELECT setval('log_auditoria_id_log_seq', COALESCE((SELECT MAX(id_log) FROM log_auditoria), 0) + 1, false);

-- ================================================================================
-- TABELA: meta_poupanca
-- ================================================================================
CREATE SEQUENCE IF NOT EXISTS meta_poupanca_id_meta_seq;
ALTER TABLE meta_poupanca 
    ALTER COLUMN id_meta SET DEFAULT nextval('meta_poupanca_id_meta_seq');
SELECT setval('meta_poupanca_id_meta_seq', COALESCE((SELECT MAX(id_meta) FROM meta_poupanca), 0) + 1, false);

-- ================================================================================
-- TABELA: orcamento
-- ================================================================================
CREATE SEQUENCE IF NOT EXISTS orcamento_id_orc_seq;
ALTER TABLE orcamento 
    ALTER COLUMN id_orc SET DEFAULT nextval('orcamento_id_orc_seq');
SELECT setval('orcamento_id_orc_seq', COALESCE((SELECT MAX(id_orc) FROM orcamento), 0) + 1, false);

-- ================================================================================
-- TABELA: plano
-- ================================================================================
CREATE SEQUENCE IF NOT EXISTS plano_id_plano_seq;
ALTER TABLE plano 
    ALTER COLUMN id_plano SET DEFAULT nextval('plano_id_plano_seq');
SELECT setval('plano_id_plano_seq', COALESCE((SELECT MAX(id_plano) FROM plano), 0) + 1, false);

-- ================================================================================
-- TABELA: transacao
-- ================================================================================
CREATE SEQUENCE IF NOT EXISTS transacao_id_trans_seq;
ALTER TABLE transacao 
    ALTER COLUMN id_trans SET DEFAULT nextval('transacao_id_trans_seq');
SELECT setval('transacao_id_trans_seq', COALESCE((SELECT MAX(id_trans) FROM transacao), 0) + 1, false);

-- ================================================================================
-- TABELA: usuario
-- ================================================================================
CREATE SEQUENCE IF NOT EXISTS usuario_id_usuario_seq;
ALTER TABLE usuario 
    ALTER COLUMN id_usuario SET DEFAULT nextval('usuario_id_usuario_seq');
SELECT setval('usuario_id_usuario_seq', COALESCE((SELECT MAX(id_usuario) FROM usuario), 0) + 1, false);

-- ================================================================================
-- VERIFICAÇÃO FINAL
-- ================================================================================
-- Execute esta query para verificar se todas as sequences foram criadas:
-- SELECT sequence_name FROM information_schema.sequences 
-- WHERE sequence_name LIKE '%_seq' 
-- ORDER BY sequence_name;

-- Execute esta query para verificar se as colunas têm DEFAULT configurado:
-- SELECT table_name, column_name, column_default 
-- FROM information_schema.columns 
-- WHERE table_schema = 'public' 
--   AND column_name LIKE 'id_%' 
--   AND column_default IS NOT NULL
-- ORDER BY table_name, column_name;

-- ================================================================================
-- FIM DO SCRIPT
-- ================================================================================

