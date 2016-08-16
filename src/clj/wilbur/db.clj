(ns wilbur.db
  (:require [yesql.core :refer [defqueries]]
            [migratus.core :as migratus]
            [config.core :refer [env]]))

(def db-conn
  {:connection-uri (:database-url env)})

(defqueries "yesql/queries.sql" {:connection db-conn})

;; ----------------------
;; -- high level stuff.
(defn find-or-create-category [{:keys [name] :as category}]
  (if-let [found (find-category {:name name} {:result-set-fn first})]
    found
    (create-category<! category)))

(defn find-or-create-user [{:keys [name] :as user}]
  (if-let [found (find-user {:name name} {:result-set-fn first})]
    found
    (create-user<! user)))

;; TODO: pass user
(defn wrap-create-post! [{:keys [category_name title body]}]
  (let [category (find-or-create-category {:name category_name})
        user     (find-or-create-user {:name "lavinia"})]
    (merge {:category_name category_name :user_name (:name user)}
      (create-post<! {:title title, :body body :category_id (:id category), :user_id (:id user)}))))

;; TODO: pass user
(defn wrap-update-post! [{:keys [id category_name] :as new-post}]
  (let [existing-post (find-post {:id id} {:result-set-fn first})
        category      (find-or-create-category {:name category_name})
        user          (find-or-create-user {:name "lavinia"})
        updated-post  (merge existing-post
                             (merge new-post {:user_id (:id user) :category_id (:id category)}))]
    (if (= 1 (update-post! updated-post))
      updated-post)))

(defn wrap-delete-post! [id]
  (delete-post! {:id (Integer/parseInt id)}))

;; ----------------------
;; -- low level stuff.
(defn migrate-all-things! []
  (migratus/migrate {:store :database :db db-conn}))

(defn truncate-all-things! []
  (truncate-posts!)
  (truncate-users!)
  (truncate-categories!))

(defn create-default-data! []
  (let [cat1 (find-or-create-category {:name "day2day"})
        cat2 (find-or-create-category {:name "real life"})
        wilbur (find-or-create-user {:name "wilbur"})
        lavinia (find-or-create-user {:name "lavinia"})]
    (create-post<! {:title "Hello World", :body "### Save The Idea\nBecause this is something *else*."
                       :category_id (:id cat1), :user_id (:id lavinia)})
    (create-post<! {:title "Compile IT!", :body "### Is the mic on?\nThis is a list with items:\n* Item 1\n* Item 2",
                       :category_id (:id cat2), :user_id (:id wilbur)})))

(comment
  (truncate-all-things!)
  (create-default-data!)
  (users)
  (categories)
  (posts)
  (map :title (posts))
  (find-category {:name "mapping"} {:result-set-fn first}))

