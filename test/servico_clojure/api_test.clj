(ns servico-clojure.api-test
  (:require [clojure.test :refer :all]
            [servico-clojure.servidor :as servidor]
            [com.stuartsierra.component :as component]
            [servico-clojure.database :as database]
            [servico-clojure.routes :as rotas])
  (:use [clojure.pprint]))

(def my-component-system
  (component/system-map
    :database (database/new-database)
    :rotas (rotas/new-rotas)
    :servidor (component/using (servidor/new-servidor) [:database :rotas])))

(def component-result (component/start my-component-system))

(def test-request (-> component-result :servidor :test-request))

(deftest tarefa-api-test
  (testing "Hello World Test"
    (let [path "/hello?name=EdsonGarcia"
          response (test-request :get path)
          body (:body response)]
      (is (= "Bem vindo!! Edson"))))
  (testing "CRUD Test"
    (let [_ (test-request :post "/tarefa?nome=Caminhar&status=pendente")
          _ (test-request :post "/tarefa?nome=Escrever&status=pendente")
          tasks (clojure.edn/read-string (:body (test-request :get "/tarefa")))
          task1 (-> tasks first second)
          task1-id (:id task1)
          task2 (->  tasks second second)
          task2-id (:id task2)
          _ (test-request :delete (str "/tarefa/" task1-id))
          _ (test-request :patch (str "/tarefa/" task2-id "?nome=TerminarCursoWebApi&status=feito"))
          tasks-processed (clojure.edn/read-string (:body (test-request :get "/tarefa")))
          task-updated (-> tasks-processed vals first)]
      (is (= 2 (count tasks)))
      (is (= "Caminhar" (:nome task1)))
      (is (= "pendente" (:status task1)))
      (is (= "Escrever" (:nome task2)))
      (is (= "pendente" (:status task2)))
      (is (= 1 (count tasks-processed)))
      (is (= "TerminarCursoWebApi" (:nome task-updated)))
      (is (= "feito" (:status task-updated))))))

;Para executar o teste: Alt + Shift + L (para carregar o namespace por completo)
;Seguido de botão direito do mouse: REPL->Run Tests in Current NS in REPL

;(test-request :get "/hello?name=EdsonGarcia")
;(test-request :post "/tarefa?nome=Caminhar&status=pendente")
;(test-request :post "/tarefa?nome=Escrever&status=pendente")
;(test-request :post "/tarefa?nome=correrLoucamente&status=pendente")
;(test-request :post "/tarefa?nome=ler&status=pendente")
;(test-request :post "/tarefa?nome=Estudar&status=pendente")
;
;(test-request :delete "/tarefa/33d53997-1d6f-4a53-842b-0b61e5ebdd95")
;(test-request :patch "/tarefa/5c9d3a10-dd35-46e9-9999-93332f44f5ed?nome=Correr&status=feito")
;
;(println "Listando todas as tarefas")
;(clojure.edn/read-string (:body (test-request :get "/tarefa")))

