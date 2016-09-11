(ns wilbur.core
    (:require [reagent.core :as r]
              [reagent.session :as session]
              [markdown.core :as md]
              [clojure.string :as str]
              [ajax.core :refer [GET POST PATCH DELETE]]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]))

;; ------------------------
;; Forward Declaration of routes
(declare root-path)
(declare post-path)
(declare edit-post-path)
(declare new-post-path)
(declare login-path)
(declare about-path)

;; ------------------------
;; App State
(def app-state (r/atom {:ready true :posts []}))

(def credentials (r/atom {:username nil :password nil}))

(def default-new-post
  {:id nil :title "New Horror Post" :body "Some *markdown* to your ❤️ 's desire" :author "wilbur" :category_name ""})

;; ------------------------
;; Utils

;; (defn find-post-by-id [post-id]
;;   (r/atom (some #(if (= (js/parseInt post-id) (:id %)) %) (:posts @app-state))))

(defn find-post-index [post-id]
  (some #(if (= (js/parseInt post-id) (:id %)) (.indexOf (:posts @app-state) %))
        (:posts @app-state)))

(defn find-post [post-id]
  (r/cursor app-state [:posts (find-post-index post-id)]))

(defn update-field [object field value]
 (swap! object assoc field value))

;; ------------------------
;; Backend hooks. All right.

;; GET/POST
(defn api-posts-path []
  "/api/v1/posts.json")

;; PATCH/DELETE
(defn api-post-path [post-id]
  (str "/api/v1/posts/" post-id ".json"))

(defn error-handler [details]
  (.warn js/console (str "Failed to fetch posts from the server: " details)))

(defn load-posts! [app-state]
  (GET (api-posts-path)
       {:handler (fn [posts] (swap! app-state assoc :posts (:posts posts)))
        :error-handler error-handler
        :response-format :json :keywords? true}))

(defn save-post! [post]
  (PATCH (api-post-path (:id @post))
         {:format :json
          :response-format :json :keywords? true
          :params {:post @post} ;; {:post {:id 5 :title "Hello" :body "Body" :category_name "le category"}
          :handler (fn [post]
                     (secretary/dispatch! (post-path {:id (:id post)})))
          :error-handler error-handler}))

(defn create-post! [post]
  (POST (api-posts-path)
        {:format :json
         :response-format :json :keywords? true
         :params {:post (dissoc @post :id)} ;; {:post {:title "Hello" :body "Body" :category_name "le category"}
         :handler (fn [post]
                    (swap! app-state update :posts conj post)
                    (secretary/dispatch! (root-path)))
         :error-handler error-handler}))

(defn delete-post! [post]
  (DELETE (api-post-path (:id @post))
          {:format :json
           :response-format :json :keywwords? true
           :handler (fn [_]
                        (swap! app-state update-in [:posts]
                               (fn [posts] (vec (remove #(= (:id @post) (:id %)) posts))))
                        (secretary/dispatch! (root-path)))
           :error-handler error-handler}))

;; ------------------------
;; Utility Components

(defn LinkTo [path text & attributes]
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

;; ------------------------
;; Main Components

(defn PostForm [post is-new?]
  (let [{:keys [id title body category_name]} @post]
    [:div.post-form
     [InputText post :title title]
     [TextArea  post :body body]
     [InputText post :category_name category_name]
     [:div
      (if is-new?
        [:button {:on-click #(create-post! post)} "Create Post"]
        [:button {:on-click #(save-post! post)} "Save Post"])]]))

(defn PostActions [post is-new?]
  (let [{:keys [id]} @post]
    [:div.actions
     [LinkTo (edit-post-path {:id id}) "edit"]
     (if-not is-new?
       [:span "--" [:a {:href "javascript:void(0)" :on-click #(delete-post! post)} "delete"]])]))

(defn PostTitle [post]
  (let [{:keys [id title]} @post is-new? (nil? id)]
    (if is-new?
      [:h2.title [LinkTo "javascript:void(0)" title]]
      [:h2.title [LinkTo (post-path {:id id}) title]])))

(defn Post [post editing]
  (let [{:keys [id title body category_name]} @post]
    [:article.post
     [PostTitle post]
     [Html (md/md->html body)]
     [:div
      [:p>em (str "#" category_name)]
      (if editing
        [PostForm post (nil? id)]
        [PostActions post (nil? id)])]]))

(defn Posts []
  [:div.posts
   (for [post (sort-by :id > (:posts @app-state))]
        (let [post-atm (r/atom post)]
          ^{:key (:id post)} [Post post-atm false]))])

;; -------------------------
;; Views

(defn layout [content]
  [:div.layout
    [:header>h1 [LinkTo (root-path) "Wilbur"]]
    [:div.content content]
    [:footer [:ul
              [:li (LinkTo (new-post-path) "new post")]
              [:li (LinkTo (login-path) "login")]
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
  [layout [:div.about-page [:h2 "About Wilbur"]]])

(defn login! [user pass]
  (println (str "user=" user "|pass=" pass ".")))

(defn login-page [credentials]
  (let [{:keys [username password]} @credentials]
    (fn []
      [layout
       [:div.login-page
        [:h2 "Login"]
        [:div.input-field
         [:input {:type "text"
                  :on-change #(swap! credentials assoc :username (-> % .-target .-value))}]]
        [:div.input-field
         [:input {:type "password"
                  :on-change #(swap! credentials assoc :password (-> % .-target .-value))}]]
        [:button {:on-click #(login! (:username @credentials) (:password @credentials))} "login"]]])))

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

(secretary/defroute login-path "/login" []
  (session/put! :current-page #(login-page credentials)))

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
