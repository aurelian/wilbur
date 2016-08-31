(ns wilbur.handler
  (:require
    [compojure.core :refer [GET POST PATCH DELETE defroutes context routes wrap-routes]]
    [clj-time.core :as time]
    [buddy.auth.backends.token :refer [jws-backend]]
    [buddy.auth.middleware :refer [wrap-authorization wrap-authentication]]
    [buddy.sign.jwt :as jwt]
    [ring.middleware.json :refer [wrap-json-body wrap-json-params wrap-json-response]]
    [wilbur.db :as db]
    [wilbur.site :as site]
    [wilbur.helpers.http :as http]
    [wilbur.middleware :refer [wrap-site-middleware wrap-api-middleware]]))

;; -----------------------
;; auth
(def secret "mysecret")
(def authdata {:admin "secret" :test "secret"})
(def auth-backend (jws-backend {:secret secret :options {:alg :hs512}}))

;; -----------------------
;; Post "controller"
(defn create-post! [{:keys [post]}]
  (let [post (db/wrap-create-post! post)]
    (http/created post (str "/posts/" (:id post)))))

(defn update-post! [{:keys [post]}]
  (let [post (db/wrap-update-post! post)]
    (http/ok post)))

(defn delete-post! [id]
  (if (= 1 (db/wrap-delete-post! id))
    (http/no-content)))

(comment
  (create-post! {:post {:title "Hi" :category_name "day2day" :body "It's a **jungle** out there!"}})
  (update-post! {:post {:id 11 :title "Dear Goat" :category_name "goats"}})
  (db/wrap-update-post! {:id 6 :title "Hello Horse" :category_name "horses"})
  (map :id (db/posts))
  (db/find-post {:id 6} {:result-set-fn first})
  (no-content)
  (delete-post! "8"))

(defn login!
  [{:keys [username password]}]
  (let [valid? (some-> authdata
                       (get (keyword username))
                       (= password))]
    (if valid?
      (let [claims {:user (keyword username)
                    :exp (time/plus (time/now) (time/seconds 3600))}
            token (jwt/sign claims secret {:alg :hs512})]
        (http/ok {:token token}))
      (http/bad-request {:message "wrong auth data"}))))

(defroutes api
    (context "/api/v1" []
             (POST   "/login.json" {body :body} (login! body))
             (POST   "/posts.json" {body :body} (create-post! body))
             (PATCH  "/posts/:id{[0-9]+}.json" {{id :id :as params} :params body :body} (update-post! body))
             (DELETE "/posts/:id{[0-9]+}.json" {{id :id} :params} (delete-post! id))
             (GET    "/posts.json" [] (http/ok {:posts (db/posts)}))))

(def app
    (routes (-> api
                (wrap-authorization auth-backend)
                (wrap-authentication auth-backend)
                (wrap-json-response)
                (wrap-json-body {:keywords? true :bigdecimals? true})
                (wrap-routes wrap-api-middleware)
                )
            (-> site/site-routes
                (wrap-routes wrap-site-middleware))))

