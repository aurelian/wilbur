(ns wilbur.middleware
  (:require [ring.middleware.defaults :refer [site-defaults api-defaults wrap-defaults]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.logger.timbre :refer [wrap-with-logger]]
            [ring.middleware.reload :refer [wrap-reload]]))

(defn wrap-api-middleware [handler]
  (-> handler
      wrap-with-logger
      (wrap-defaults api-defaults)
      wrap-exceptions
      wrap-reload))

(defn wrap-site-middleware [handler]
  (-> handler
      wrap-with-logger
      (wrap-defaults site-defaults)
      wrap-exceptions
      wrap-reload))
