(ns wilbur.core
  (:require [reagent.core :as r]
            [reagent.session :as session]
            [markdown.core :as md]
            [clojure.string :as str]
            [wilbur.api :as api]
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
(def app-state (r/atom {:current-user "admin" :ready true :posts []}))
(def credentials (r/atom {:username nil :password nil}))

(def default-new-post
  {:id nil :title "New Horror Post" :body "Some *markdown* to your ❤️ 's desire" :author "wilbur" :category_name ""})

;; ------------------------
;; Utils

;; (defn find-post-by-id [post-id]
;;   (r/atom (some #(if (= (js/parseInt post-id) (:id %)) %) (:posts @app-state))))
(defn logged-in? []
  (not (nil? (:current_user @app-state))))

(defn find-post-index [post-id]
  (some #(if (= (js/parseInt post-id) (:id %)) (.indexOf (:posts @app-state) %))
        (:posts @app-state)))

(defn find-post [post-id]
  (r/cursor app-state [:posts (find-post-index post-id)]))

(defn update-field [object field value]
 (swap! object assoc field value))

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

(defn Authenticated? [yes-component no-component]
  (if (logged-in?)
    yes-component
    no-component)
)
;; ------------------------
;; Main Components

(defn PostForm [post is-new?]
  (let [{:keys [id title body category_name]} @post]
    [:div.post-form
     [InputText post :title title]
     [TextArea  post :body body]
     [InputText post :category_name category_name]
     [Authenticated? [:div.actions (if is-new?
                                    [:button {:on-click #(api/create-post! post "")} "Create Post"]
                                    [:button {:on-click #(api/save-post! post "")} "Save Post"]
                                   )]
                     [:span "--not auth"]
      ]]))

(defn PostActions [post is-new?]
  (let [{:keys [id]} @post]
    [:div.actions
     [LinkTo (edit-post-path {:id id}) "edit"]
     (if-not is-new?
       [:span "--" [:a {:href "javascript:void(0)" :on-click #(api/delete-post! post "")} "delete"]])]))

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
              [:li (if-not (logged-in?)
                     (LinkTo (login-path) "login")
                     [:p "hello"]
                     )]
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

(defn login-page [credentials]
  (let [{:keys [username password]} @credentials
        error (r/atom nil)]
    (fn []
      [layout
       [:div.login-page
        [:h2 "Login"]
        [:div.input-field
         [:input {:type "text"
                  :placeholder "username"
                  :on-change #(swap! credentials assoc :username (-> % .-target .-value))}]]
        [:div.input-field
         [:input {:type "password"
                  :placeholder "password"
                  :on-change #(swap! credentials assoc :password (-> % .-target .-value))}]]
        (when-let [error @error]
          [:p.error error])
        [:button {:on-click #(api/login! (:username @credentials) (:password @credentials) error)} 
         "login"]]])))

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/set-config! :prefix "/#")

(secretary/defroute root-path "/" []
  (session/put! :current-page posts-page))

(secretary/defroute new-post-path "/posts/new" []
  (if (logged-in?)
    (session/put! :current-page #(new-post-page))
    (accountant/navigate! (login-path))))

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
  (api/load-posts! app-state)
  (mount-root))
