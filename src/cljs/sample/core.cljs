(ns sample.core
  (:require
    [reagent.core :as r]
    [reagent.dom :as rdom]
    [goog.events :as events]
    [goog.history.EventType :as HistoryEventType]
    [markdown.core :refer [md->html]]
    [sample.ajax :as ajax]
    [ajax.core :refer [GET POST]]
    [reitit.core :as reitit]
    [clojure.string :as string])
  (:import goog.History))

(defonce session (r/atom {:page :home}))
(defonce app-state (r/atom []))
(defonce buttons (r/atom [["+" "plus"] ["-" "minus"] ["*" "mult"] ["/" "div"]]))

(defn nav-link [uri title page]
  [:a.navbar-item
   {:href   uri
    :class (when (= page (:page @session)) "is-active")}
   title])

(defn navbar [] 
  (r/with-let [expanded? (r/atom false)]
    [:nav.navbar.is-info>div.container
     [:div.navbar-brand
      [:a.navbar-item {:href "/" :style {:font-weight :bold}} "sample"]
      [:span.navbar-burger.burger
       {:data-target :nav-menu
        :on-click #(swap! expanded? not)
        :class (when @expanded? :is-active)}
       [:span][:span][:span]]]
     [:div#nav-menu.navbar-menu
      {:class (when @expanded? :is-active)}
      [:div.navbar-start
       [nav-link "#/" "Home" :home]
       [nav-link "#/about" "About" :about]]]]))

(defn about-page []
  [:section.section>div.container>div.content
   [:img {:src "/img/warning_clojure.png"}]])

(defn setColor [total]
  (if (< total 0 ) "#FF0000"
    (if (<= total 19) "#90EE90" (if (<= total 49) "#ADD8E6" "#FFA07A"))))
(defn list-item [item]
  (let [x (:x item) y (:y item) operation (:op item) total (:total item)]
    [:li {:style {:list-style-type "circle" :color (setColor total)}} x  " " operation " " y " = " (str total)]
    ))


(comment (setColor 55))



(defn input-field [key form-data]
  [:div.field
   [:label.label "Operand"]
   [:div.control
    [:input.input.is-primary
     {:style {:margin-bottom "15px"}
      :type "number"
      :value (key @form-data)
      :on-change #(swap! form-data assoc key (js/parseInt (-> % .-target .-value)))}]]])


(defn update-state [form-data operator]
  (GET (str "/api/math/" (second operator))
       {:params {:x (:x @form-data) :y (:y @form-data)}
        :handler #(swap! app-state conj (conj @form-data % {:op (first operator)}))}) )


(defn button [form-data operator]
  [:button.button.is-primary {:on-click #(update-state form-data operator)} (first operator) ]
  )
(defn operations [form-data]
  [:div
   [:label.label "Operators"]
   [:div.buttons
    (for [operator @buttons]
      [button form-data operator])
    ]])

(defn equation-list []
  [:div {:style {:margin-top "10px"}}
   [:label.label "Equations"]
   [:ul {:style {:margin-left "15px"}}
    (for [item @app-state]
      [list-item item])]])



(defn home-page []
  (let [form-data (r/atom {})]
    (fn []
      [:div {:style {:margin "50px" :width "350px"}}
       [:div.box
        [input-field :x form-data]
        [input-field :y form-data]
        [operations form-data]
        [equation-list]]
       ])))

(def pages
  {:home #'home-page
   :about #'about-page})

(defn page []
  [(pages (:page @session))])

;; -------------------------
;; Routes

(def router
  (reitit/router
    [["/" :home]
     ["/about" :about]]))

(defn match-route [uri]
  (->> (or (not-empty (string/replace uri #"^.*#" "")) "/")
       (reitit/match-by-path router)
       :data
       :name))
;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [^js/Event.token event]
        (swap! session assoc :page (match-route (.-token event)))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn fetch-docs! []
  (GET "/docs" {:handler #(swap! session assoc :docs %)}))

(defn ^:dev/after-load mount-components []
  (rdom/render [#'navbar] (.getElementById js/document "navbar"))
  (rdom/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (ajax/load-interceptors!)
  (fetch-docs!)
  (hook-browser-navigation!)
  (mount-components))
