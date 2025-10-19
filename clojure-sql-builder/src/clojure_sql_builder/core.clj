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
      :em        (str campo " IN (" (clojure.string/join ", " valor) ")")
      ;; Adicione outros comparadores aqui (:menor_que, :diferente_de, etc.)
      (throw (Exception. (str "Operador desconhecido: " op))))))

(defn -main [& _args]
  (println (processa-condicao {:campo 'nome, :igual_a "José"}))
  (println (processa-condicao {:campo 'idade, :maior_que 20}))
  (println (processa-condicao {:campo 'id, :em [10, 20, 30]})))