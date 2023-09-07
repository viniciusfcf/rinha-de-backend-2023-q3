CREATE extension pg_trgm;


CREATE TEXT SEARCH CONFIGURATION BUSCA (COPY = portuguese);
ALTER TEXT SEARCH CONFIGURATION BUSCA ALTER MAPPING FOR hword, hword_part, word WITH portuguese_stem;
-- https://www.postgresql.org/docs/current/non-durability.html
create UNLOGGED table Pessoa (
    apelido varchar(32) not null UNIQUE,
    nascimento date not null,
    nome varchar(100) not null,
    publicID uuid not null,
    stack VARCHAR(800),
    --createdSeconds int NOT NULL DEFAULT EXTRACT(SECOND FROM NOW()),
    primary key (publicID)
    ,BUSCA_TRGM TEXT GENERATED ALWAYS AS (
        NOME || ' ' || APELIDO || ' ' || COALESCE(STACK, '')
    ) STORED not null
); 
-- PARTITION BY RANGE (createdSeconds);

-- CREATE TABLE p_10 PARTITION OF Pessoa
--   FOR VALUES FROM (0) TO (10);

-- CREATE TABLE p_20 PARTITION OF Pessoa
--   FOR VALUES FROM (10) TO (20);


-- CREATE TABLE p_30 PARTITION OF Pessoa
--   FOR VALUES FROM (20) TO (30);


-- CREATE TABLE p_40 PARTITION OF Pessoa
--   FOR VALUES FROM (30) TO (40);

-- CREATE TABLE p_50 PARTITION OF Pessoa
--   FOR VALUES FROM (40) TO (50);

-- CREATE TABLE p_60 PARTITION OF Pessoa
--   FOR VALUES FROM (50) TO (60);

CREATE INDEX CONCURRENTLY IF NOT EXISTS IDX_PESSOAS_BUSCA_TGRM ON PESSOA USING GIST (BUSCA_TRGM GIST_TRGM_OPS(siglen=256)) 
INCLUDE(apelido, nascimento, nome, publicID, stack);

SET log_min_duration_statement = 500;
ALTER TABLE Pessoa SET (autovacuum_enabled = false);