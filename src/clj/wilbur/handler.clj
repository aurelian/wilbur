(ns wilbur.handler
  (:require [compojure.core :refer [GET defroutes context]]
            [compojure.route :refer [not-found resources]]
            [cheshire.core :as json]
            [hiccup.page :refer [include-js include-css html5]]
            [wilbur.middleware :refer [wrap-middleware]]
            [config.core :refer [env]]))

(def mount-target
  [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   [:link {:href "https://fonts.googleapis.com/css?family=Sanchez" :rel "stylesheet"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(def loading-page
  (html5
    (head)
    [:body {:class "body-container"}
     mount-target
     (include-js "/js/app.js")]))

(def posts
  {:posts [{:id 3 :title "Hello World" 
            :body "### Is the mic on?\nThis is a list with items:\n* Item 1\n* Item 2" 
            :author "lavinia" :category "day2day"}
           {:id 8 :title "Compile IT!"
            :body "### Save The Idea\nBecause this is something *else*."
            :author "wilbur" :category "real life"}]})

(defn json-response [data]
  {:status  200
   :headers {"Content-Type" "application/json; charset=utf-8"}
   :body    (json/generate-string data)})

(defroutes routes
  (context "/api/v1" []
    (GET "/posts.json" [] (json-response posts)))
  (GET "*" [] loading-page)
  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
