-- Flyway Migration V3: Pix Keys Schema

CREATE TABLE chave_pix (
    id_chave SERIAL PRIMARY KEY,
    id_usuario INTEGER NOT NULL,
    tipo_chave VARCHAR(50) NOT NULL,
    valor_chave VARCHAR(255) NOT NULL UNIQUE,
    id_conta INTEGER NOT NULL,
    CONSTRAINT fk_chave_pix_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_chave_pix_conta FOREIGN KEY (id_conta) REFERENCES conta(id_conta) ON DELETE CASCADE
);
