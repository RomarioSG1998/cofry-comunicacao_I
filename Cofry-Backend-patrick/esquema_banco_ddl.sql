-- ================================================================================
-- DDL EXTRAÍDO DO BANCO DE DADOS
-- ================================================================================
-- Banco: postgres
-- Host: cofry-db.cc5w4muoa5ca.us-east-1.rds.amazonaws.com
-- Data: Wed Dec 03 14:23:32 BRT 2025
-- ================================================================================

-- ================================================================================
-- TABELA: assinatura
-- ================================================================================

CREATE TABLE assinatura (
    id_assin INTEGER PRIMARY KEY,
    id_usuario INTEGER,
    id_plano INTEGER,
    status VARCHAR(20),
    data_fim DATE
);

-- ================================================================================
-- TABELA: boleto_dda
-- ================================================================================

CREATE TABLE boleto_dda (
    id_boleto INTEGER PRIMARY KEY,
    id_usuario INTEGER,
    cod_barras VARCHAR(255),
    vencimento DATE,
    status VARCHAR(20)
);

-- ================================================================================
-- TABELA: cartao_credito
-- ================================================================================

CREATE TABLE cartao_credito (
    id_cartao INTEGER PRIMARY KEY,
    id_usuario INTEGER,
    limite NUMERIC,
    dia_vencimento INTEGER
);

-- ================================================================================
-- TABELA: categoria
-- ================================================================================

CREATE TABLE categoria (
    id_categoria INTEGER PRIMARY KEY,
    nome VARCHAR(50),
    tipo VARCHAR(20),
    icone VARCHAR(50)
);

-- ================================================================================
-- TABELA: conta
-- ================================================================================

CREATE TABLE conta (
    id_conta INTEGER PRIMARY KEY,
    id_usuario INTEGER,
    saldo NUMERIC,
    instituicao VARCHAR(100)
);

-- ================================================================================
-- TABELA: investimento
-- ================================================================================

CREATE TABLE investimento (
    id_invest INTEGER PRIMARY KEY,
    id_usuario INTEGER,
    tipo_ativo VARCHAR(50),
    valor_aplicado NUMERIC,
    roi_atual NUMERIC
);

-- ================================================================================
-- TABELA: log_auditoria
-- ================================================================================

CREATE TABLE log_auditoria (
    id_log INTEGER PRIMARY KEY,
    id_admin INTEGER,
    acao VARCHAR(255),
    data_hora DATE
);

-- ================================================================================
-- TABELA: meta_poupanca
-- ================================================================================

CREATE TABLE meta_poupanca (
    id_meta INTEGER PRIMARY KEY,
    id_usuario INTEGER,
    valor_alvo NUMERIC,
    valor_atual NUMERIC,
    data_limite DATE
);

-- ================================================================================
-- TABELA: orcamento
-- ================================================================================

CREATE TABLE orcamento (
    id_orc INTEGER PRIMARY KEY,
    id_usuario INTEGER,
    id_categoria INTEGER,
    valor_limite NUMERIC,
    mes_ano VARCHAR(7)
);

-- ================================================================================
-- TABELA: plano
-- ================================================================================

CREATE TABLE plano (
    id_plano INTEGER PRIMARY KEY,
    nome VARCHAR(50),
    preco NUMERIC,
    recursos TEXT
);

-- ================================================================================
-- TABELA: transacao
-- ================================================================================

CREATE TABLE transacao (
    id_trans INTEGER PRIMARY KEY,
    id_usuario INTEGER,
    valor NUMERIC,
    data DATE,
    comprovante_url VARCHAR(255),
    id_categoria INTEGER,
    id_conta INTEGER,
    id_cartao INTEGER
);

-- ================================================================================
-- TABELA: usuario
-- ================================================================================

CREATE TABLE usuario (
    id_usuario INTEGER PRIMARY KEY,
    nome VARCHAR(100),
    email VARCHAR(100),
    senha_hash VARCHAR(255),
    tipo_usuario VARCHAR(20)
);

