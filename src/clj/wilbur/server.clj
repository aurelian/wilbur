(ns wilbur.server
  (:require [wilbur.handler :refer [app]]
            [wilbur.db :as db]
            [config.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn start-jetty [&args]
  (let [port (Integer/parseInt (or (env :port) "3000"))]
    (run-jetty app {:port port :join? false})))

(defn -main [& args]
  (cond
    (some #{"migrate"} args)
    (do
      (db/migrate-all-things!)
      (System/exit 0))
    :else
    (start-jetty args)))

