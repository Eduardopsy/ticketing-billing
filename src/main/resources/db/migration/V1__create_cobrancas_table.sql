CREATE TABLE cobrancas (
                           id                  BIGSERIAL PRIMARY KEY,
                           id_usuario          VARCHAR(100)   NOT NULL,
                           nome_solicitante    VARCHAR(255)   NOT NULL,
                           tipo                VARCHAR(50)    NOT NULL,
                           metodo              VARCHAR(50)    NOT NULL,
                           status              VARCHAR(50)    NOT NULL,
                           valor_solicitacao   NUMERIC(10,2)  NOT NULL,
                           valor_pago          NUMERIC(10,2),
                           txid                VARCHAR(255),
                           copia_ecola         TEXT,
                           transaction_id      VARCHAR(255),
                           acs_url             VARCHAR(500),
                           three_ds_payload    TEXT,
                           data_criacao        TIMESTAMP      NOT NULL,
                           data_expiracao      TIMESTAMP,
                           data_finalizada     TIMESTAMP,
                           cobranca_pai_id     BIGINT REFERENCES cobrancas(id)
);

CREATE INDEX idx_cobrancas_txid        ON cobrancas (txid);
CREATE INDEX idx_cobrancas_transaction ON cobrancas (transaction_id);
CREATE INDEX idx_cobrancas_usuario     ON cobrancas (id_usuario);
CREATE INDEX idx_cobrancas_status      ON cobrancas (status);