(ns wilbur.helpers.http
  "HTTP Response Helpers"
  (:require
    [ring.util.response :as r :refer [response status header]]))

(defn ok [body]
  (r/response body))

(defn bad-request [body]
  (r/status (r/response body) 400))

(defn created [body location]
  (->
    (r/response body)
    (r/status 201)
    (r/header "Location" location)))

(defn no-content []
  (r/status (r/response nil) 204))

