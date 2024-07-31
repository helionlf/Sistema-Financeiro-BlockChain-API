(ns blockchain.functions
  (:import [java.security MessageDigest]))

(def dadosGenesis "Coloque neste campo os dados das transacoes")

;cadeia de blocos
(def cadeia (atom []))

;SHA-256 para gerar o Hash
(defn sha256 [string]
  (let [digest (.digest (MessageDigest/getInstance "SHA-256") (.getBytes string "UTF-8"))]
    (apply str (map (partial format "%02x") digest))))

;Função para calcular o Hash Anterior ao Próximo Bloco
(defn calculoHashPrevio []
    (if (= (count @cadeia) 0)
        "0000000000000000000000000000000000000000000000000000000000000000"
        (:hash (get @cadeia (dec (count @cadeia))))
    )
)

;Minerar para encontrar o Nonce
(defn minerar [id nonce dados hashAnterior]
    (if (.startsWith (sha256 (str id nonce dados hashAnterior)) "0000")
        nonce
        (recur id (inc nonce) dados hashAnterior)
    )
)

(defn add[dados]
    (def id (inc (count @cadeia)))
    (def hashAnterior (calculoHashPrevio))
    (def nonce (minerar id 1 dados hashAnterior))
    (swap! cadeia conj {:id id :nonce nonce :dados dados :hashAnterior hashAnterior :hash (sha256 (str id nonce dados hashAnterior))})
    (get @cadeia (dec (count @cadeia)))
)

(defn iniciarBlockChain[] 
    (add dadosGenesis)
    @cadeia
)