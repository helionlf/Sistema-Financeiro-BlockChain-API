(ns uitransacoes.core
  (:require [cheshire.core :refer [generate-string parse-string]]
            [clj-http.client :as http-client]
            [cheshire.core :as json])
  (:gen-class))

;; url de cada API
(def url_financeiro "http://localhost:3000")
(def url_blockchain "http://localhost:3001")

(defn api-url [base path]
  (str base path))

(defn parametrizar-entrada [valor tipo]
  {:valor valor :tipo tipo})

;;cadastra uma nova transação
(defn cadastrar-transacao [valor tipo]
  (let [transacao (parametrizar-entrada valor tipo)]
    (-> (http-client/post (api-url url_financeiro "/transacoes")
                          {:body (generate-string transacao)
                           :headers {"Content-Type" "application/json"}})
        :body
        (parse-string true)))
  (println "\nTransacao cadastrada com sucesso!\n"))

;; Exibe todas as transações cadastrada pelo usuário
(defn exibir-transacoes []
  (let [response (http-client/get (api-url url_financeiro "/transacoes") {:as :json})
        transacoes (get-in response [:body :transacoes])]
    (println "\nTransacoes:")
    (doall (map #(println "Valor:" (get % :valor) "-" (get % :tipo)) transacoes)))
  (println ""))

;; Guarda todas as transações cadastradas na blockChain
(defn registrar_transacoes []
  (let [response (http-client/get (api-url url_financeiro "/transacoes") {:as :json})
        transacoes (get-in response [:body :transacoes])] 
    (-> (http-client/post (api-url url_blockchain "/transacao")
                          {:body (generate-string transacoes)
                           :headers {"Content-Type" "application/json"}})
        :body
        (parse-string true)))
  (println "\nTodas as suas transacoes foram registradas!\n"))

(defn como-json [conteudo]
  (let [json-str (json/generate-string conteudo)
        transacao (json/parse-string json-str true)]
    (println "Valor:" (:valor transacao) "-" (:tipo transacao))))

(defn formatar-dados [vet]
  (doall (map #(como-json %) vet)))

;;Exibe cada bloco da blockChain
(defn exibir-blocks []
  (let [response (http-client/get (api-url url_blockchain "/chain") {:as :json})
        blocks (get-in response [:body])] 
    (println "\nBlockChain:\n")
    (doall (map #(do
                   (println "Id:" (get % :id))
                   (println "Nonce:" (get % :nonce))
                   (println "Dados:\n")
                   (if (= (get % :id) 1)
                     (println (get % :dados))
                     (formatar-dados (get % :dados)))
                   (println "\nHash Anterior:" (get % :hashAnterior))
                   (println "Hash:" (get % :hash) "\n")) blocks))))
 
(defn loop-app 
  ([]
   (println "Sistema Financeiro - Escolha uma opcao:")
   (println "1 - Cadastrar Transacoes")
   (println "2 - Exibir Transacoes")
   (println "3 - Registrar Transacoes")
   (println "4 - Exibir Blocos da Cadeia")
   (println "5 - Fechar Programa")
   (def opcao (read))
   (loop-app opcao))
  
  ([opcao]
   (cond
     (= opcao 1)
     (do
       (println "\nDigite o valor:")
       (def valor (read))
       (println "\nDigite o tipo:")
       (def tipo (read))
       (cadastrar-transacao valor tipo)
       (loop-app))
   
     (= opcao 2)
     (do
       (exibir-transacoes)
       (loop-app))
     
     (= opcao 3) 
     (do
       (registrar_transacoes)
       (loop-app))
     
     (= opcao 4)
     (do
       (exibir-blocks)
       (loop-app))
     
     (= opcao 5) 
     (println "Progama encerrrado com sucesso!")
     
     :else (do
             (println "Opcao invalida! Informe outra opcao:")
             (recur (read))
             ))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (loop-app))
