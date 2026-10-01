--V2: Migartions para adicionar a coluna de ranking na tabela de cadastros
ALTER TABLE tb_cadastro
ADD COLUMN rank VARCHAR(50);