(ns wilbur.middleware
  (:require
    [ring.middleware.json :refer [wrap-json-body wrap-json-params wrap-json-response]]
    [ring.middleware.defaults :refer [site-defaults api-defaults wrap-defaults]]
    [ring.logger.timbre :refer [wrap-with-logger]]
    [ring.middleware.reload :refer [wrap-reload]]
    [prone.middleware :refer [wrap-exceptions]]))

(defn wrap-api-middleware [handler]
  (-> handler
      (wrap-with-logger)
      (wrap-exceptions)
      (wrap-reload)
      (wrap-json-response)
      (wrap-json-body {:keywords? true :bigdecimals? true})
      (wrap-defaults api-defaults)))

(defn wrap-site-middleware [handler]
  (-> handler
      (wrap-with-logger)
      (wrap-exceptions)
      (wrap-reload)
      (wrap-defaults site-defaults)))
