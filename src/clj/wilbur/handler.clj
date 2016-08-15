(ns wilbur.handler
  (:require [compojure.core :refer [GET POST PATCH defroutes context routes wrap-routes]]
            [ring.logger.timbre :as logger]
            [ring.middleware.json :refer [wrap-json-body wrap-json-params wrap-json-response]]
            [ring.util.response :refer [response]]
            [wilbur.middleware :refer [wrap-site-middleware wrap-api-middleware]]
            [wilbur.db :as db]
            [wilbur.site :as site]))

(defn create-post! [{:keys [body]}]
  (let [post (db/create-post! (:post body))]
    (response post)))

(defn update-post! [id post]
  (let [db-post (db/find-post {:id id} {:result-set-fn first})]
    (db/update-post! (merge db-post post))
    (response (merge db-post post))))

(comment
  (create-post! {:body {:post {:title "Hi" :category_name "day2day" :body "It's a **jungle** out there!"}}})
  (update-post! 8, {:title "O Hai¡"})
  (db/find-post {:id 8} {:result-set-fn first})
  )

(defroutes api
    (context "/api/v1" []
             (POST  "/posts.json" request (create-post! request))
             (PATCH "/posts/:id{[0-9]+}.json" [id :<< Integer/parseInt
                                               :as {body :body}] (update-post! id (:post body)))
             (GET   "/posts.json" [] (response {:posts (db/posts)}))))

(def app
  (logger/wrap-with-logger
    (routes (-> api
                (wrap-json-response)
                (wrap-json-body {:keywords? true})
                (wrap-routes wrap-api-middleware))
            (-> site/site-routes
                (wrap-routes wrap-site-middleware)))))

