(ns financeiro.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [cheshire.core :as json]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-body]]
            [financeiro.db :as db]
            [financeiro.transacoes :as transacoes]))

(defn como-json [conteudo & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json; charset=utf-8"}
   :body (json/generate-string conteudo)})

(defroutes app-routes
  (GET "/" [] "API - Sistema Financeiro")

  (GET "/saldo" [] (como-json {:saldo (db/saldo)}))

  (POST "/transacoes" requisicao
    (if (transacoes/valida? (:body requisicao))
      (-> (db/registrar (:body requisicao))
          (como-json 201))
      (como-json {:mensagem "Requisicao invalida"} 422)))

  (GET "/transacoes" {filtros :params}
    (como-json {:transacoes
                (if (empty? filtros)
                  (db/transacoes)
                  (db/transacoes-com-filtro filtros))}))


  (GET "/receitas" []
    (como-json {:transacoes (db/transacoes-do-tipo "receita")}))

  (GET "/despesas" []
    (como-json {:transacoes (db/transacoes-do-tipo "despesa")}))

  (route/not-found "Recurso nao encontrado"))


(def app
  (-> (wrap-defaults app-routes api-defaults)
      (wrap-json-body {:keywords? true :bigdecimals? true})))
