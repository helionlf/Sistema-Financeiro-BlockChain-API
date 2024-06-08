(ns blockchain.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-body]]
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

  (GET "/chain" [] (como-json @cadeia))
  
  (POST "/transacao" request
    (let [transacoes (:body request)]
      (if (empty? @cadeia)
        (como-json (iniciarBlockChain))
        (como-json (add transacoes)))))
  
  (route/not-found "Not Found"))

(def app
  (-> (wrap-defaults app-routes api-defaults)
      (wrap-json-body {:keywords? true :bigdecimals? true})))
