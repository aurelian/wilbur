(ns wilbur.core
    (:require [reagent.core :as r]
              [reagent.session :as session]
              [markdown.core :as md]
              [clojure.string :as str]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]))

;; ------------------------
;; App State
(def app-state (r/atom {:posts [{:id 1 :body "## Hello World\nThis is a list with items:\n* Item 1\n* Item 2" :tags ["day2day" "real life"]}
                                {:id 2 :body "## Save The Idea\nBecause this is something *else*." :tags ["real life" "programming"]}]}))

;; ------------------------
;; Components

(defn markdown-component [text]
  [:div {:class "body" :dangerouslySetInnerHTML {:__html text}}])

(defn tags-component [tag-list]
  [:p>em (str/join ", " tag-list)])

(defn post-component [post]
  [:div {:id (:id post) :class "post"}
   [markdown-component (md/md->html (:body post))]
   [tags-component (:tags post)]])

(defn posts-component []
  [:div
   (for [post (:posts @app-state)]
     ^{:key (:id post)} [post-component post])])

;; -------------------------
;; Views

(defn home-page []
  [:div [:h1 "Welcome to wilbur"]
   [posts-component]
   [:div [:a {:href "/about"} "go to about page"]]])

(defn about-page []
  [:div [:h2 "About wilbur"]
   [:div [:a {:href "/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (r/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
