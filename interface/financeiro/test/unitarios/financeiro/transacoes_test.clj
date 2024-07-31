(ns financeiro.transacoes-test
  (:require [midje.sweet :refer :all]
            [financeiro.transacoes :refer :all]))

(fact "Uma transacao sem valor nao e valida"
      (valida? {:tipo "receita"}) => false)

(fact "Uma transacao com valor negativo nao e valida"
      (valida? {:valor -10 :tipo "receita"}) => false)

(fact "Uma transacao com valor nao numerico nao e valida"
      (valida? {:valor "mil" :tipo "receita"}) => false)

(fact "Uma transacao sem tipo nao e valida"
      (valida? {:valor 90}) => false)

(fact "Uma transacao com tipo desconhecido nao e valida"
      (valida? {:valor 8 :tipo "investimento"}) => false)

(fact "Uma transacao com valor numerico positivo e com tipo conhecido e valida"
      (valida? {:valor 230 :tipo "receita"}) => true)
