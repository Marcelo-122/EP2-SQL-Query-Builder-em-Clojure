(ns clojure-sql-builder.core)
(require '[clojure.string :as str])
;; Formata um valor para SQL (strings precisam de aspas simples)
(defn formata-valor [valor]
  (if (string? valor)
    (str "'" valor "'")
    (str valor)))

;; Processa UM mapa de condição
(defn processa-condicao [condicao]
  (let [campo (name (:campo condicao)) ; Pega o nome do campo
        ;; Pega o primeiro par chave-valor que não seja :campo
        [op valor] (first (dissoc condicao :campo))]
    (case op
      :igual_a   (str campo " = " (formata-valor valor))
      :maior_que (str campo " > " (formata-valor valor))
      :diferente_de   (str campo " <> " (formata-valor valor))
      :not_like (str campo " NOT LIKE " (formata-valor valor)) 
      :menor_que      (str campo " < " (formata-valor valor))
      :menor_igual_que (str campo " <= " (formata-valor valor))
      :maior_igual_que (str campo " >= " (formata-valor valor))
      :entre          (str campo " BETWEEN " (formata-valor valor))
      :like           (str campo " LIKE " (formata-valor valor)) 
      :not_in (str campo " NOT IN (" (str/join ", " (map formata-valor valor)) ")")
      :em        (str campo " IN (" (str/join ", " (map formata-valor valor)) ")")
      ;; outros comparadores aqui (:menor_que, :diferente_de, etc.)
      (throw (Exception. (str "Operador desconhecido: " op))))))

(declare processa-clausula)

(defn processa-e-ou [logica-fn separador]
  (let [condicoes (second logica-fn) ; Pega o vetor de condições
        ;; Usa `map` para processar cada item da lista recursivamente
        partes (map processa-clausula condicoes)
        clausula-final (str/join separador partes)]
    (str "(" clausula-final ")"))) ; Envolve em parênteses
  
;; Função "despachante": decide o que fazer com base no tipo de entrada
  (defn processa-clausula [clausula]
  (cond
    ;; Se for um mapa é uma condição simples
    (map? clausula)
    (processa-condicao clausula)

    ;; Se for uma lista começando com 'e_s'
    (and (list? clausula) (= 'e_s (first clausula)))
    (processa-e-ou clausula " AND ")

    ;; Se for uma lista começando com 'ou_s'
    (and (list? clausula) (= 'ou_s (first clausula)))
    (processa-e-ou clausula " OR ")

    :else
    (throw (Exception. (str "Cláusula inválida: " clausula)))))
  
(defn to-sql [estado-query]
  (let [campos (str/join ", " (:fields estado-query))
        tabela (:table estado-query)
        where    (when (:where estado-query) (str "WHERE " (:where estado-query)))
        order-by (:order-by estado-query)
        limit (:limit estado-query)]
    (clojure.string/join " " (filter some? ["SELECT" campos "FROM" tabela where order-by limit]))))

;; Funçoes builders
(defn busca_tabela [nome-tabela]
  ;; O ponto de partida: cria o mapa de estado inicial
  {:table nome-tabela
   :fields ["*"] ; Padrão é *
   :where nil})

(defn campos [estado-query lista-campos]
  ;; Recebe o estado, atualiza os campos e retorna um NOVO estado
  (assoc estado-query :fields lista-campos))

(defn filtros [estado-query clausula-where]
  ;; Recebe o estado, processa a cláusula where e retorna um NOVO estado
  (assoc estado-query :where (processa-clausula clausula-where)))

(defn order-by [estado-query ord-map]
  (assoc estado-query :order-by (str "ORDER BY " (name (:campo ord-map)) " " (name (:direcao ord-map)))))

(defn limit [estado-query limite]
  (assoc estado-query :limit (str "LIMIT " limite)))

;; --- Teste no REPL ---
;; Usando a macro -> para encadear as chamadas de forma legível
(-> (busca_tabela "usuario")
    (campos ["abc" "xyz"])
    (filtros (list 'e_s [{:campo :nome, :igual_a "José"}
                         {:campo :idade, :maior_que 20}
                         {:campo :id, :em [10 20 30]}
                         {:campo :status, :igual_a true}
                         (list 'ou_s [{:campo :camiseta, :igual_a "verde"}
                                      {:campo :camiseta, :igual_a "azul"}])]))
    (order-by {:campo :idade :direcao :desc})
    (limit 10)
    to-sql)