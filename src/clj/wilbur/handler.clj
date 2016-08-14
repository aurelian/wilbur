(ns wilbur.handler
  (:require [compojure.core :refer [GET POST defroutes context routes wrap-routes]]
            [cheshire.core :as json]
            [wilbur.middleware :refer [wrap-site-middleware wrap-api-middleware]]
            [wilbur.db :as db]
            [wilbur.site :as site]

            ))

(defn json-response [data]
  {:status  200
   :headers {"Content-Type" "application/json; charset=utf-8"}
   :body    (json/generate-string data)})

(defroutes api
  (context "/api/v1" []
           (POST "/posts.json" [] (json-response {:message "Hello World!"}))
           (GET  "/posts.json" [] (json-response {:posts (db/posts)}))))

(def app
  (routes (-> api
              (wrap-routes wrap-api-middleware))
          (-> site/site-routes
              (wrap-routes wrap-site-middleware))))

