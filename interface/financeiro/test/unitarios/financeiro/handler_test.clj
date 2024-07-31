(ns financeiro.handler-test
  (:require [midje.sweet :refer :all]
            [ring.mock.request :as mock]
            [cheshire.core :as json]
            [financeiro.handler :refer :all]
            [financeiro.db :as db]))

(facts "Da um 'Ola, Mundo!' na rota raiz"
       (let [response (app (mock/request :get "/"))]
         (fact "o status da resposta e 200"
               (:status response) => 200)

         (fact "o texto do corpo e 'Ola, mundo!'"
               (:body response) => "Ola, mundo!")))

(facts "Rota invalida nao existe"
       (let [response (app (mock/request :get "/invalid"))]
         (fact "o codigo de erro e 404"
               (:status response) => 404)

         (fact "o texto do corpo e 'Recurso nao encontrado'"
               (:body response) => "Recurso nao encontrado")))

(facts "Saldo inicial e 0"
       (against-background [(json/generate-string {:saldo 0})
                            => "{\"saldo\":0}"
                            (db/saldo) => 0])

       (let [response (app (mock/request :get "/saldo"))]
         (fact "o formato e 'application/json'"
               (get-in response [:headers "Content-Type"])
               => "application/json; charset=utf-8")

         (fact "o status da resposta e 200"
               (:status response) => 200)

         (fact "o texto do corpo e um JSON cuja chave e saldo e o valor e 0"
               (:body response) => "{\"saldo\":0}")))

(facts "Registra uma receita no valor de 10"
       #_{:clj-kondo/ignore [:unresolved-symbol]}
       (against-background (db/registrar {:valor 10
                                          :tipo "receita"})
                           => {:id 1 :valor 10 :tipo "receita"})

       (let [response
             (app (-> (mock/request :post "/transacoes")
                      (mock/json-body {:valor 10
                                       :tipo "receita"})))]

         (fact "o status da resposta e 201"
               (:status response) => 201)

         (fact "o texto do corpo e um JSON com o conteudo enviado e um id"
               (:body response) =>
               "{\"id\":1,\"valor\":10,\"tipo\":\"receita\"}")))

(facts "Existe rota para lidar com filtro de transacao por tipo"
       (against-background
        [(db/transacoes-do-tipo "receita")
         => '({:id 1 :valor 2000 :tipo "receita"})
         (db/transacoes-do-tipo "despesa")
         => '({:id 2 :valor 89 :tipo "despesa"})
         (db/transacoes)
         => '({:id 1 :valor 2000 :tipo "receita"}
              {:id 2 :valor 89 :tipo "despesa"})]
        (fact "Filtro por receita"
              (let [response (app (mock/request :get "/receitas"))]
                (:status response) => 200
                (:body response) => (json/generate-string
                                     {:transacoes '({:id 1 :valor 2000 :tipo "receita"})})))
        (fact "Filtro por despesa"
              (let [response (app (mock/request :get "/despesas"))]
                (:status response) => 200
                (:body response) => (json/generate-string
                                     {:transacoes '({:id 2 :valor 89 :tipo "despesa"})})))
        (fact "Sem filtro"
              (let [response (app (mock/request :get "/transacoes"))]
                (:status response) => 200
                (:body response) => (json/generate-string
                                     {:transacoes '({:id 1 :valor 2000 :tipo "receita"}
                                                    {:id 2 :valor 89 :tipo "despesa"})})))))

(facts "Filtra transacoes por parametros de busca na URL"
       (def livro {:id 1 :valor 88 :tipo "despesa" :rotulos ["livro" "educacao"]})
       (def curso {:id 2 :valor 106 :tipo "despesa" :rotulos ["curso" "educacao"]})
       (def salario {:id 3 :valor 8000 :tipo "receita" :rotulos ["salario"]})

       (against-background
        [(db/transacoes-com-filtro {:rotulos ["livro" "curso"]})
         => [livro curso]
         (db/transacoes-com-filtro {:rotulos "salario"})
         => [salario]]

        (fact "Filtro multiplos rotulos"
              (let [response (app (mock/request :get "/transacoes?rotulos=livro&rotulos=curso"))]
                (:status response) => 200
                (:body response) => (json/generate-string {:transacoes [livro curso]})))

        (fact "Filtro com unico rotulo"
              (let [response (app (mock/request :get "/transacoes?rotulos=salario"))]
                (:status response) => 200
                (:body response) => (json/generate-string {:transacoes [salario]})))))
