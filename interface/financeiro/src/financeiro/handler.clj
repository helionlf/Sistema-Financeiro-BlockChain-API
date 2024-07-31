(ns financeiro.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [cheshire.core :as json]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-body]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.util.response :refer [redirect]]
            [ring.middleware.cors :refer [wrap-cors]]
            [financeiro.db :as db]
            [financeiro.transacoes :as transacoes]))

(defn como-json [conteudo & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json; charset=utf-8"}
   :body (json/generate-string conteudo)})

(defroutes app-routes
  (GET "/" [] (redirect "/home" ))

  (GET "/home" [] (slurp "resources/public/pages/homepage.html"))

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


;; (def app
;;   (-> (wrap-defaults app-routes api-defaults)
;;       (wrap-json-body {:keywords? true :bigdecimals? true})
;;       (wrap-resource "public")
;;       wrap-content-type
;;       wrap-not-modified))

(def app
  (-> (wrap-defaults app-routes api-defaults)
      (wrap-json-body {:keywords? true :bigdecimals? true})
      (wrap-resource "public")
      wrap-content-type
      wrap-not-modified
      (wrap-cors :access-control-allow-origin [#"http://localhost:3001"]
                 :access-control-allow-methods [:get :post :put :delete])))