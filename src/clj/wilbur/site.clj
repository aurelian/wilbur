(ns wilbur.site
  (:require [config.core :refer [env]]
            [hiccup.page :refer [include-js include-css html5]]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [resources]]))

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
   [:title "wilbur"]
   [:link {:href "https://fonts.googleapis.com/css?family=Sanchez" :rel "stylesheet"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(def loading-page
  (html5
    (head)
    [:body.container
     mount-target
     (include-js "/js/app.js")]))

(defroutes site-routes
  (GET "/" [] loading-page)
  (resources "/"))

