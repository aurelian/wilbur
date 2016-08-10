(ns wilbur.core
    (:require [reagent.core :as r]
              [reagent.session :as session]
              [markdown.core :as md]
              [clojure.string :as str]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]))

;; ------------------------
;; Forward Declaration of routes
(declare root-path)
(declare post-path)
(declare edit-post-path)
(declare new-post-path)
(declare about-path)

;; ------------------------
;; App State

(def app-state (r/atom {:posts [{:id 3 :title "Hello World"
                                 :body "### Is the mic on?\nThis is a list with items:\n* Item 1\n* Item 2"
                                 :author "aurelian" :category "day2day"}
                                {:id 8 :title "Compile IT!"
                                 :body "### Save The Idea\nBecause this is something *else*."
                                 :author "aurelian" :category "real life"}]}))

;; ------------------------
;; Utils

(defn find-post-index [post-id]
  (some #(if (= (js/parseInt post-id) (:id %)) (.indexOf (:posts @app-state) %)) (:posts @app-state)))

(defn find-post-cursor [post-id]
  (r/cursor app-state [:posts (find-post-index post-id)]))

(defn update-field [cursor field value]
 (swap! cursor assoc field value))

;; TODO: save it to backend
(defn save-post [cursor id]
  (println cursor)
  (secretary/dispatch! (post-path {:id id})))

;; ------------------------
;; Components

(defn LinkTo [path text];; & attributes]
  [:a {:href path} text])

(defn Html [text]
  [:section {:class "body" :dangerouslySetInnerHTML {:__html text}}])

(defn InputText [cursor field value]
  [:div.input-field
    [:input {:value value
           :on-change #(update-field cursor field (.. % -target -value))}]])

(defn TextArea [cursor field value]
  [:div.input-field
    [:textarea {:value value
                :rows 20 :cols 50
                :on-change #(update-field cursor field (.. % -target -value))}]])

(defn PostForm [post-cursor]
  (let [{:keys [id title body category]} @post-cursor]
    [:div.post-form
     [InputText post-cursor :title title]
     [TextArea post-cursor :body body]
     [:div
      [:button {:on-click #(save-post post-cursor id)} "Save Post"]]]))

(defn Post [post-cursor editing]
  (let [{:keys [id title body category]} @post-cursor]
    [:article.post {:id (str "post-" id)}
     [:h2.title [LinkTo (post-path {:id id}) title]]
     [Html (md/md->html body)]
     [:div
      [:p>em (str "#" category)]
      (if editing
        [PostForm post-cursor]
        [LinkTo (edit-post-path {:id id}) "edit"])]]))

(defn Posts []
  [:div
   (for [idx (range (count (:posts @app-state)))]
      (let [post-cursor (r/cursor app-state [:posts idx])]
          ^{:key idx} [Post post-cursor false]))])

;; -------------------------
;; Views

(defn layout [content]
  [:div.layout
    [:header>h1 (LinkTo (root-path) "Wilbur Whateley")]
    [:div.content content]
    [:footer [:ul
              [:li (LinkTo (new-post-path) "new post")]
              [:li (LinkTo (about-path) "go to about page")]]]])

(defn not-found-page [message]
  [layout
   [:div message]])

(defn posts-page []
  [layout
   [Posts]])

(defn post-page [post-id]
  (if-let [post-cursor (find-post-cursor post-id)]
    [layout [Post post-cursor]]
    [not-found-page (str "Post with id= '" post-id "` was not found")]))

(defn edit-post-page [post-id]
  (if-let [post-cursor (find-post-cursor post-id)]
    [layout [Post post-cursor true]]
    [not-found-page (str "Post with id= '" post-id "` was not found")]))

(defn new-post-page []
  [layout
    [:div "New Post Page"]])

(defn about-page []
  [layout [:div "About Wilbur Whateley"]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/set-config! :prefix "/#")

(secretary/defroute root-path "/" []
  (session/put! :current-page posts-page))

(secretary/defroute new-post-path "/posts/new" []
  (session/put! :current-page #(new-post-page)))

(secretary/defroute post-path "/posts/:id" [id]
  (session/put! :current-page #(post-page id)))

(secretary/defroute edit-post-path "/posts/:id/edit" [id]
  (session/put! :current-page #(edit-post-page id)))

(secretary/defroute about-path "/about" []
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
