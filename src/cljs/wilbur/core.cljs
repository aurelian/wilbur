(ns wilbur.core
    (:require [reagent.core :as r]
              [reagent.session :as session]
              [markdown.core :as md]
              [clojure.string :as str]
              [ajax.core :refer [GET POST]]
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

(def app-state (r/atom {:ready true :posts []}))

(def default-new-post
  {:id nil :title "New Horror Post" :body "Some *markdown* to your ❤️ 's desire" :author "wilbur" :category ""})

;; ------------------------
;; Backend hooks. All right.

(defn load-posts! [app-state]
  (GET "/api/v1/posts.json"
       {:handler (fn [data] (swap! app-state assoc :posts (:posts data)))
        :error-handler (fn [details] (.warn js/console (str "Failed to fetch posts from the server: " details)))
        :response-format :json, :keywords? true}))

;; ------------------------
;; Utils

;; (defn find-post-by-id [post-id]
;;   (r/atom (some #(if (= (js/parseInt post-id) (:id %)) %) (:posts @app-state))))

(defn find-post-index [post-id]
  (some #(if (= (js/parseInt post-id) (:id %)) (.indexOf (:posts @app-state) %)) (:posts @app-state)))

(defn find-post [post-id]
  (r/cursor app-state [:posts (find-post-index post-id)]))

(defn update-field [object field value]
 (swap! object assoc field value))

(defn next-post-id []
  (inc (:id (last (:posts @app-state)))))

;; TODO: save it to backend
(defn save-post [post id]
  (secretary/dispatch! (post-path {:id id})))

;; TODO: save it to backend
(defn create-post [post]
  (swap! app-state update :posts conj (merge @post {:id (next-post-id)}))
  (secretary/dispatch! (root-path)))

;; ------------------------
;; Components

(defn LinkTo [path text];; & attributes]
  [:a {:href path} text])

(defn Html [text]
  [:section {:class "body" :dangerouslySetInnerHTML {:__html text}}])

(defn InputText [object field value]
  [:div.input-field
    [:input {:value value
             :on-change #(update-field object field (.. % -target -value))}]])

(defn TextArea [object field value]
  [:div.input-field
    [:textarea {:value value :rows 20 :cols 50
                :on-change #(update-field object field (.. % -target -value))}]])

(defn PostForm [post is-new?]
  (let [{:keys [id title body category]} @post]
    [:div.post-form
     [InputText post :title title]
     [TextArea  post :body body]
     [InputText post :category category]
     [:div
      (if is-new?
        [:button {:on-click #(create-post post)} "Create Post"]
        [:button {:on-click #(save-post post id)} "Save Post"])]]))

(defn PostTitle [post]
  (let [{:keys [id title]} @post is-new? (nil? id)]
    (if is-new?
      [:h2.title [LinkTo "javascript:void(0)" title]]
      [:h2.title [LinkTo (post-path {:id id}) title]])))

(defn Post [post editing]
  (let [{:keys [id title body category]} @post]
    [:article.post
     [PostTitle post]
     [Html (md/md->html body)]
     [:div
      [:p>em (str "#" category)]
      (if editing
        [PostForm post (nil? id)]
        [LinkTo (edit-post-path {:id id}) "edit"])]]))

;; (for [idx (range (count (:posts @app-state)))]
;;    (let [post (r/cursor app-state [:posts idx])]
(defn Posts []
  [:div.posts
   (for [post (sort-by :id > (:posts @app-state))]
        (let [post-atm (r/atom post)]
          ^{:key (:id post)} [Post post-atm false]))])

;; -------------------------
;; Views

(defn layout [content]
  [:div.layout
    [:header>h1 [LinkTo (root-path) "Wilbur Whateley"]]
    [:div.content content]
    [:footer [:ul
              [:li (LinkTo (new-post-path) "new post")]
              [:li (LinkTo (about-path) "go to about page")]]]])

(defn not-found-page [message]
  [layout [:div message]])

(defn posts-page []
  [layout [Posts]])

(defn post-page [post-id]
  (if-let [post (find-post post-id)]
    [layout [Post post]]
    [not-found-page (str "Post with id= '" post-id "` was not found")]))

(defn edit-post-page [post-id]
  (if-let [post (find-post post-id)]
    [layout [Post post true]]
    [not-found-page (str "Post with id= '" post-id "` was not found")]))

(defn new-post-page []
  [layout [Post (r/atom default-new-post) true]])

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
  (load-posts! app-state)
  (mount-root))
