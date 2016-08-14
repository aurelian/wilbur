(ns wilbur.handler
  (:require [compojure.core :refer [GET POST defroutes context routes wrap-routes]]
            [ring.logger.timbre :as logger]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.util.response :refer [response]]
            [wilbur.middleware :refer [wrap-site-middleware wrap-api-middleware]]
            [wilbur.db :as db]
            [wilbur.site :as site]))

(defroutes api
    (context "/api/v1" []
             (POST "/posts.json" [] (response {:message "Hello World!"}))
             (GET  "/posts.json" [] (response {:posts (db/posts)}))))

(def app
  (logger/wrap-with-logger
    (routes (-> api
                (wrap-routes wrap-json-response wrap-api-middleware))
            (-> site/site-routes
                (wrap-routes wrap-site-middleware)))))

