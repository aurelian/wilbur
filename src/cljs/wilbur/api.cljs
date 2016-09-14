(ns wilbur.api
  (:require 
    [ajax.core :refer [GET POST PATCH DELETE]]))

;; GET/POST
(defn- api-posts-path []
  "/api/v1/posts.json")

;; PATCH/DELETE
(defn- api-post-path [post-id]
  (str "/api/v1/posts/" post-id ".json"))

(defn- api-login-path []
  "/api/v1/login.json")

(defn- error-handler [details]
  (.warn js/console (str "Failed to fetch posts from the server: " details)))

(defn load-posts! [app-state]
  (GET (api-posts-path)
       {:handler (fn [posts] (swap! app-state assoc :posts (:posts posts)))
        :error-handler error-handler
        :response-format :json :keywords? true}))

(defn save-post! [post handler]
  (PATCH (api-post-path (:id @post))
         {:format :json
          :response-format :json :keywords? true
          :params {:post @post} ;; {:post {:id 5 :title "Hello" :body "Body" :category_name "le category"}
          :handler handler ;; (fn [post] (secretary/dispatch! (post-path {:id (:id post)})))
          :error-handler error-handler}))

(defn create-post! [post handler]
  (POST (api-posts-path)
        {:format :json
         :response-format :json :keywords? true
         :params {:post (dissoc @post :id)} ;; {:post {:title "Hello" :body "Body" :category_name "le category"}
         :handler handler ; (fn [post]
                  ;  (swap! app-state update :posts conj post)
                  ;  (secretary/dispatch! (root-path)))
         :error-handler error-handler}))

(defn delete-post! [post handler]
  (DELETE (api-post-path (:id @post))
          {:format :json
           :response-format :json :keywwords? true
           :handler handler ;(fn [_]
                    ;    (swap! app-state update-in [:posts]
                    ;           (fn [posts] (vec (remove #(= (:id @post) (:id %)) posts))))
                    ;    (secretary/dispatch! (root-path)))
           :error-handler error-handler}))

(defn login! [username password error]
  (POST (api-login-path)
        {:format :json
         :response-format :json :keywords? true
         :params {:username username :password password}
         :handler (fn [response]

                    (println response)

                    )
         :error-handler #(reset! error (get-in % [:response :error]))}))

