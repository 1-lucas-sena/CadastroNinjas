-- Atualiza os status antigos para a nova nomenclatura

UPDATE tb_missoes
SET status = 'DISPONIVEL'
WHERE status = 'ATIVA';

UPDATE tb_missoes
SET status = 'EM_ANDAMENTO'
WHERE status = 'EM_CURSO';
