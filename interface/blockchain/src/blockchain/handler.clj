(ns blockchain.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-body]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.util.response :refer [redirect]]
            [ring.middleware.cors :refer [wrap-cors]]
            [blockchain.functions :refer :all]
            [cheshire.core :as json]
            [clj-http.client :as http]))

(defn como-json [conteudo & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json; charset=utf-8"}
   :body (json/generate-string conteudo)})

(defroutes app-routes
  (GET "/" [] (como-json (if (= (count @cadeia) 0)
                                               (iniciarBlockChain)
                                               @cadeia)))
  
  (GET "/home" [] (slurp "resources/public/pages/blockChain.html"))

  (GET "/chain" [] (como-json @cadeia))
  
  (POST "/transacao" request
    (let [transacoes (:body request)]
      (if (empty? @cadeia)
        (como-json (iniciarBlockChain))
        (como-json (add transacoes)))))
  
  (route/not-found "Not Found"))

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
      (wrap-cors :access-control-allow-origin [#"http://localhost:3000"]
                 :access-control-allow-methods [:get :post :put :delete])))