(ns wilbur.test.handler
  (:require
    [clojure.test :refer :all]
    [clojure.java.jdbc :as jdbc]
    [ring.mock.request :as mock]
    [wilbur.db :as db :refer [migrate-all-things! db-conn]]
    [wilbur.handler :refer [app]]))

(use-fixtures :once (fn [f]
                     (db/migrate-all-things!)
                     (f)))

;;
;;  (jdbc/with-db-transaction [t-conn *db*]
;;        (jdbc/db-set-rollback-only! t-conn)
;;

(deftest test-site
  (testing "posts.json"
    (let [response (app (mock/request :get "/api/v1/posts.json"))]
      (is (= (:status response) 200))))
  (testing "main route"
    (let [response (app (mock/request :get "/"))]
      (is (= (:status response) 200)))))

