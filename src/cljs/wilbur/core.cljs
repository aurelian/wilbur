(ns wilbur.core
    (:require [reagent.core :as r]
              [reagent.session :as session]
              [markdown.core :as md]
              [clojure.string :as str]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]))


;; ------------------------
;; Forward

(declare post-path)
(declare edit-post-path)
;; ------------------------
;; App State

(def app-state (r/atom {:posts [{:id 1 :title "Hello World" :body "### Is the mic on?\nThis is a list with items:\n* Item 1\n* Item 2"
                                 :author "aurelian" :category "day2day"}
                                {:id 2 :title "Compile IT!" :body "### Save The Idea\nBecause this is something *else*."
                                 :author "aurelian" :category "real life"}]}))

;; ------------------------
;; Utils

(defn find-post [post-id]
  (some #(if (= (js/parseInt post-id) (:id %)) %) (:posts @app-state)))

;; ------------------------
;; Components

(defn html [text]
  [:div {:class "body" :dangerouslySetInnerHTML {:__html text}}])

(defn post-component [post]
  [:div {:id (:id post) :class "post"}
   [:h2>a {:href (post-path post)} (:title post)]
   [html (md/md->html (:body post))]
   [:div
    [:p>em (str "#" (:category post))]
    [:a {:href (edit-post-path post)} "edit"]]])

(defn posts-component []
  [:div
   (for [post (:posts @app-state)]
     ^{:key (:id post)} [post-component post])])

;; -------------------------
;; Views

(defn layout [content]
  [:div
    [:header>h1>a {:href "/"} "Wilbur Whateley"]
    [:div content]
    [:footer>a {:href "/about"} "go to about page"]])

(defn not-found-page [message]
  [layout
   [:div message]])

(defn posts-page []
  [layout
   [posts-component]])

(defn post-form-component [post]
  [:div "Hello"])

(defn post-page [post-id]
  (if-let [post (find-post post-id)]
    [layout [post-component post]]
    [not-found-page (str "Post with id= '" post-id "` was not found")]))

(defn edit-post-page [post-id]
  (if-let [post (find-post post-id)]
    [layout [post-form-component post]]
    [not-found-page (str "Post with id= '" post-id "` was not found")]))

(defn about-page []
  [layout [:div "About Wilbur Whateley"]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/defroute "/" []
  (session/put! :current-page posts-page))

(secretary/defroute edit-post-path "/posts/:id/edit" [id]
  (session/put! :current-page #(edit-post-page id)))

(secretary/defroute post-path "/posts/:id" [id]
  (session/put! :current-page #(post-page id)))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

(secretary/defroute "*" []
  (session/put! :current-page #(not-found-page "Page was not found")))

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
