(ns clojure-sql-builder.core)

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
      :em        (str campo " IN (" (clojure.string/join ", " (map formata-valor valor)) ")")
      ;; outros comparadores aqui (:menor_que, :diferente_de, etc.)
      (throw (Exception. (str "Operador desconhecido: " op))))))

(declare processa-clausula)

(defn processa-e-ou [logica-fn separador]
  (let [condicoes (second logica-fn) ; Pega o vetor de condições
        ;; Usa `map` para processar cada item da lista recursivamente
        partes (map processa-clausula condicoes)
        clausula-final (clojure.string/join separador partes)]
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
  
 ;; --- Teste no REPL ---
(def filtro-exemplo
  (list 'e_s [{:campo :idade, :maior_que 20}
              (list 'ou_s [{:campo :camiseta, :igual_a "verde"}
                           {:campo :camiseta, :igual_a "azul"}])]))

(processa-clausula filtro-exemplo)