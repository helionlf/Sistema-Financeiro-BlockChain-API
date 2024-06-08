(ns financeiro.filtros-aceitacao-test
  (:require [midje.sweet :refer :all]
            [cheshire.core :as json]
            [financeiro.auxiliares :refer :all]
            [clj-http.client :as http]
            [financeiro.db :as db]))

(def transacoes-aleatorias
  '({:valor 7.0M :tipo "despesa"
     :rotulos ["sorvete" "entretenimento"]}
    {:valor 88.0M :tipo "despesa"
     :rotulos ["livro" "educacao"]}
    {:valor 106.0M :tipo "despesa"
     :rotulos ["curso" "educacao"]}
    {:valor 8000.0M :tipo "receita"
     :rotulos ["salario"]}))


(against-background [(before :facts
                             [(iniciar-servidor porta-padrao)
                              (db/limpar)])
                     (after :facts (parar-servidor))]

  ;; Fatos iniciais verificando que nao existem transacoes
                    (fact "Nao existem receitas" :aceitacao
                          (json/parse-string (conteudo "/receitas") true)
                          => {:transacoes '()})

                    (fact "Nao existem despesas" :aceitacao
                          (json/parse-string (conteudo "/despesas") true)
                          => {:transacoes '()})

                    (fact "Nao existem transacoes" :aceitacao
                          (json/parse-string (conteudo "/transacoes") true)
                          => {:transacoes '()})

  ;; Antes destes fatos comecarem, limpamos os registros e cadastramos algumas transacoes
                    (against-background
                     [(before :facts (doseq [transacao transacoes-aleatorias]
                                       (db/registrar transacao)))
                      (after :facts (db/limpar))]

    ;; Fatos verificando as transacoes cadastradas
                     (fact "Existem 3 despesas" :aceitacao
                           (count (:transacoes (json/parse-string
                                                (conteudo "/despesas") true))) => 3)

                     (fact "Existe 1 receita" :aceitacao
                           (count (:transacoes (json/parse-string
                                                (conteudo "/receitas") true))) => 1)

                     (fact "Existem 4 transacoes" :aceitacao
                           (count (:transacoes (json/parse-string
                                                (conteudo "/transacoes") true))) => 4))
                    
                    (fact "Existe 1 receita com rotulo 'salario'"
                          (count (:transacoes (json/parse-string
                                               (conteudo "/transacoes?rotulos=salario") true))) => 1)
                    
                    (fact "Existem 2 despesas com rotulo 'livro' ou 'curso'"
                          (count (:transacoes (json/parse-string
                                               (conteudo "/transacoes?rotulos=livro&rotulos=curso") true))) => 2)
                    
                    (fact "Existem 2 despesas com rotulo 'educacao'"
                          (count (:transacoes (json/parse-string
                                               (conteudo "/transacoes?rotulos=educacao") true))) => 2))


