(ns wilbur.middleware
  (:require [ring.middleware.defaults :refer [site-defaults api-defaults wrap-defaults]]
            [ring.logger.timbre :refer [wrap-with-logger]]))

(defn wrap-api-middleware [handler]
  (-> handler
      wrap-with-logger
      (wrap-defaults api-defaults)))

(defn wrap-site-middleware [handler]
  (-> handler
      wrap-with-logger
      (wrap-defaults site-defaults)))
