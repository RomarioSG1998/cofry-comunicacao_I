-- Flyway Migration V1: Database Schema

CREATE TABLE assinatura (
    id_assin SERIAL PRIMARY KEY,
    id_usuario INTEGER,
    id_plano INTEGER,
    status VARCHAR(20),
    data_fim DATE
);

CREATE TABLE boleto_dda (
    id_boleto SERIAL PRIMARY KEY,
    id_usuario INTEGER,
    cod_barras VARCHAR(255),
    vencimento DATE,
    status VARCHAR(20)
);

CREATE TABLE cartao_credito (
    id_cartao SERIAL PRIMARY KEY,
    id_usuario INTEGER,
    limite NUMERIC,
    dia_vencimento INTEGER
);

CREATE TABLE categoria (
    id_categoria SERIAL PRIMARY KEY,
    nome VARCHAR(50),
    tipo VARCHAR(20),
    icone VARCHAR(50)
);

CREATE TABLE conta (
    id_conta SERIAL PRIMARY KEY,
    id_usuario INTEGER,
    saldo NUMERIC,
    instituicao VARCHAR(100)
);

CREATE TABLE investimento (
    id_invest SERIAL PRIMARY KEY,
    id_usuario INTEGER,
    tipo_ativo VARCHAR(50),
    valor_aplicado NUMERIC,
    roi_atual NUMERIC
);

CREATE TABLE log_auditoria (
    id_log SERIAL PRIMARY KEY,
    id_admin INTEGER,
    acao VARCHAR(255),
    data_hora DATE
);

CREATE TABLE meta_poupanca (
    id_meta SERIAL PRIMARY KEY,
    id_usuario INTEGER,
    valor_alvo NUMERIC,
    valor_atual NUMERIC,
    data_limite DATE
);

CREATE TABLE orcamento (
    id_orc SERIAL PRIMARY KEY,
    id_usuario INTEGER,
    id_categoria INTEGER,
    valor_limite NUMERIC,
    mes_ano VARCHAR(7)
);

CREATE TABLE plano (
    id_plano SERIAL PRIMARY KEY,
    nome VARCHAR(50),
    preco NUMERIC,
    recursos TEXT
);

CREATE TABLE transacao (
    id_trans SERIAL PRIMARY KEY,
    id_usuario INTEGER,
    valor NUMERIC,
    data DATE,
    comprovante_url VARCHAR(255),
    id_categoria INTEGER,
    id_conta INTEGER,
    id_cartao INTEGER
);

CREATE TABLE usuario (
    id_usuario SERIAL PRIMARY KEY,
    nome VARCHAR(100),
    email VARCHAR(100),
    senha_hash VARCHAR(255),
    tipo_usuario VARCHAR(20)
);
