# EP2-SQL-Query-Builder-em-Clojure
Um Query Builder para Clojure em que o usuário possa ir incluindo aos poucos detalhes das query e este fazer um select.

## Como usar
Rode o projeto com o REPL do Calva no VSCode.
A maneira mais simples é abrir o Command Palette (Cmd + Shift + P) e digitar "Calva: Jack-in" e em seguida "Calva: Jack-in: Leiningen" e depois selecionar o evaluate desejado.
A query pode ser encontrada em output.calva-repl como por exemplo:
clj꞉clojure-sql-builder.core꞉> 
"SELECT abc, xyz FROM usuario WHERE (nome = 'José' AND idade > 20 AND id IN (10, 20, 30) AND status = true AND (camiseta = 'verde' OR camiseta = 'azul')) ORDER BY idade desc LIMIT 10"
clj꞉clojure-sql-builder.core꞉> 

Se desejado é possivel utilizar o arquivo Mycompiler_code.txt para criar um banco de dados SQL e testar as queries.