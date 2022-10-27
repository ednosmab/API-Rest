(ns servico-clojure.servidor
  (:require [servico-clojure.database :as database]
            [io.pedestal.http :as http]
            [io.pedestal.test :as test]
            [io.pedestal.interceptor :as i]
            [com.stuartsierra.component :as component]))

(defrecord Servidor [database rotas]
  component/Lifecycle
  (start [this]
    (println "Start servidor")
    (defn assoc-store [context]
      (update context :request assoc :store (:store database)))

    (def db-interceptor
      {:name :db-interceptor
       :enter assoc-store})

    (def service-map-base {::http/routes (:endpoints rotas)
                           ::http/port 9999
                           ::http/type :jetty
                           ::http/join? false})

    (def service-map (-> service-map-base
                         (http/default-interceptors)
                         (update ::http/interceptors conj (i/interceptor db-interceptor))))

    (defonce server (atom nil))

    (defn start-server []
      (reset! server (http/start (http/create-server service-map))))

    (defn test-request [verb url]
      (test/response-for (::http/service-fn @server) verb url))

    ;(defn stop-server []
    ;  (http/stop @server))

    (defn stop-server []
      (http/stop @server))

    (defn restart-server []
      (stop-server)
      (start-server))

    ; Com o start ativo, não é possível recarregar o nRepl (Alt + Shift + L)
    ;(start-server)

    ; Com o restart ativo é possível recarregar o nRepl (Alt + Shift + L)
    ;(restart-server)
    ;(start-server)

    (defn start []
      (try (start-server) (catch Exception e (println "Erro ao estartar o servidor" (.getMessage e))))
      (try (restart-server) (catch Exception e (println "Erro ao restartar o servidor" (.getMessage e)))))
    (start)
    (assoc this :test-request test-request))
  (stop [this]
    (println "Stop servidor")
    (assoc this :test-request nil))
  )

(defn new-servidor []
  (map->Servidor {}))









