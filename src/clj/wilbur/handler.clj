(ns wilbur.handler
  (:require [compojure.core :refer [GET POST PATCH DELETE defroutes context routes wrap-routes]]
            [ring.logger.timbre :as logger]
            [ring.middleware.json :refer [wrap-json-body wrap-json-params wrap-json-response]]
            [ring.util.response :as r :refer [response status header]]
            [wilbur.middleware :refer [wrap-site-middleware wrap-api-middleware]]
            [wilbur.db :as db]
            [wilbur.site :as site]))

;; -----------------------
;; HTTP response codes
(defn ok [body]
  (r/response body))

(defn created [body location]
  (->
    (r/response body)
    (r/status 201)
    (r/header "Location" location)))

(defn no-content []
  (r/status (r/response nil) 204))

;; -----------------------
;; Post "controller"
(defn create-post! [{:keys [post]}]
  (let [post (db/wrap-create-post! post)]
    (created post (str "/posts/" (:id post)))))

(defn update-post! [{:keys [post]}]
  (let [post (db/wrap-update-post! post)]
    (ok post)))

(defn delete-post! [id]
  (if (= 1 (db/wrap-delete-post! id))
    (no-content)))

(comment
  (create-post! {:post {:title "Hi" :category_name "day2day" :body "It's a **jungle** out there!"}})
  (update-post! {:post {:id 11 :title "Dear Goat" :category_name "goats"}})
  (db/wrap-update-post! {:id 6 :title "Hello Horse" :category_name "horses"})
  (map :id (db/posts))
  (db/find-post {:id 6} {:result-set-fn first})
  (no-content)
  (delete-post! "8")
  )

(defroutes api
    (context "/api/v1" []
             (POST   "/posts.json" {body :body} (create-post! body))
             (PATCH  "/posts/:id{[0-9]+}.json" {{id :id :as params} :params body :body} (update-post! body))
             (DELETE "/posts/:id{[0-9]+}.json" {{id :id} :params} (delete-post! id))
             (GET    "/posts.json" [] (response {:posts (db/posts)}))))

(def app
  (logger/wrap-with-logger
    (routes (-> api
                (wrap-json-response)
                (wrap-json-body {:keywords? true})
                (wrap-routes wrap-api-middleware))
            (-> site/site-routes
                (wrap-routes wrap-site-middleware)))))

