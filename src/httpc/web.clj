(ns httpc.web
  (:require (ring.util [response :as response])
	    (clojure.contrib.json)
	    [compojure.route :as route])
  (:use [hiccup core page-helpers form-helpers]
	[httpc player]
	[httpc.test suite]
	[clojure.contrib.json]
	compojure.core :reload-all))


(defn- non-blank? [s]
  (and (not (nil? s))
       (not= "" (.trim s))))

(defn- looks-like-url? [s]
  (try
    (java.net.URL. s)
    true
    (catch Exception e
      false)))

(defn- format-time [ts]
  (.format (java.text.SimpleDateFormat. "HH:mm:ss")
	   (java.util.Date. ts)))

(defn- layout [& body]
  (html5
   [:head
    [:title "Restful world"]
    (include-css "/style/bootstrap.min.css"
		 "/style/custom.css")
    (include-js "/js/jquery.min.js"
		"/js/jquery.tablesorter.min.js")]
   [:body
    [:div.topbar
     [:div.fill
      [:div.container
       [:a.brand {:href "/"} "RESTful competition"]
       [:ul.nav
	[:li
	 (link-to "/register" "Register player")]]]]]
    [:div.container
     body]]))

(defn- heading1 [& body]
  [:div.page-header
   [:h1
    body]])
(defn- content [& body]
  [:div.content
   body])

(defn message-ok [msg]
  [:div.alert-message.success
   [:p msg]])

(defn all-scores []
  (for [p (players-by-score)]
    [:tr
     [:td (link-to (str "/player/" (:id p)) (:name p))]
     [:td (:url p)]
     [:td (:score p)]]))

(defn index-page []
  (layout
   (heading1 "Scores")
   (content
    [:table.zebra-striped
     [:thead
      [:tr
       [:th "Player name"]
       [:th "Player url"]
       [:th "Points"]]]
     [:tbody {:id "scores"}
      (all-scores)]
     ]
    (javascript-tag
     "$(function() {
        // $('table').tablesorter({ sortList: [[2,1]]  });
        setInterval(function() {
          $.ajax({ url: '/scores',
                   success: function(data) {
                     $('#scores').html(data);
                     // $('table').trigger('update'); 
                   }});
        }, 5000);
      });"))))

(defn scores-json-fragment []
  (let [body (reduce (fn [r p] (conj r {:id (:id p)
					:name (:name p)
					:score (:score p)
					:completed (count (:completed-tests p))
					:total (count (all-test-in-suite))}))
		     [] (players-by-score))]
    (-> (response/response (json-str body))
	(response/content-type "application/json"))))

(defn- accepts-json? [headers]
  (re-find #"application/json" (str (headers "accept"))))

(defn scores-fragment [h]
  (if (accepts-json? h)
    (scores-json-fragment)
    (html (all-scores))))

(defn register-page [& [name url msg]]
  (layout
   (heading1 "Register player")
   (content
    (when msg
      [:div.alert-message.error
       [:p msg]])
    (form-to [:post "/register"]
	    [:fieldset
	     [:div.clearfix
	      (label :name "Team name:")
	      [:div.input
	       (text-field :name name)]]
	     [:div.clearfix
	      (label :url "Server url:")
	      [:div.input
	       (text-field :url url)]]]
	    [:div.actions
	     [:input.btn.primary {:type "submit" :value "Register now"}]]
	    ))))

(defn do-register [name url]
  (if (and (non-blank? name)
	   (non-blank? url))
    (if (not (looks-like-url? url))
      (register-page name url "Please fill in a valid url")
      (if (not (player-exists? name))
	(do
	  (add-player! name url)
	  (response/redirect "/"))
	(register-page name url "Name already exists")))
    (register-page name url "Please fill in team name and server url")))

(defn- status-icon [status]
  (get {:ok "/img/ok.png"
	:error "/img/error.png"
	:fail "/img/warn.png"
	:timeout "/img/error.png"} status))

(defn player-scores [id]
  (let [p (player-by-id id)] ; todo: redirect to root if player not found
    [:div
     [:div.score "Score: "
      [:strong
       (:score p)]]
     [:br]
     [:table.zebra-striped
      [:tr
       [:th "Time"]
       [:th "Result"]
       [:th "Score"]
       [:th "Message"]]
      (for [evt (:log p)]
	[:tr
	 [:td (format-time (:time evt))]
	 [:td.center [:img {:src (status-icon (:status evt))
			    :alt (name (:status evt))}]]
	 [:td.center (:score evt)]
	 [:td {:width "70%"} (:message evt)]])]]))

(defn player-page [id]
  (let [p (player-by-id id)] ; todo: redirect to root if player not found
    (layout
     (heading1 "Team " (:name p))
     [:div#scores
      (player-scores id)]
     (javascript-tag
      (str
        "$(function() {
           setInterval(function() {
             $.ajax({ url: '/player/" id "/scores',
                      success: function(data) {
                        $('#scores').html(data);
                      }});
           }, 5000);
         });")))))

(defn player-scores-fragment [id]
  (html
   (player-scores id)))

(defn admin-page [& [msg]]
  (layout
   (heading1 "Admin")
   (when msg
     (message-ok msg))
   (form-to [:post "/admin/switch"]
	    [:fieldset
	     [:legend "Test suite"]
	     [:div.clearfix
	      (label :suite "Active suite:")
	      [:div.input
	       [:ul.inputs-list
		(for [suite (all-suites)]
		  [:li
		   [:label
		    (radio-button :suite
			       (= suite (deref *suite*))
			       (:name suite))
		    [:span " " (:name suite)]]])]]]]
	    [:div.actions
	     [:input {:type "submit" :class "btn primary" :value "Select"}]])
   (form-to [:post "/admin/reset"]
	    [:h3 "Reset all scores"]
	    [:p "Reset all scores to zero in order to start from a new suite."]
	    [:div.actions
	     [:input {:type "submit" :class "btn primary" :value "Reset"
		      :onclick "return confirm('Sure?')"}]
	     ])
   (form-to [:post "/admin/backup"]
	    [:h3 "Save/restore game state"]
	    [:p "Save the whole game state into permanent storage or restore it from storage."]
	    [:div.actions
	     [:input {:type "submit" :class "btn primary" :name "save" :value "Save"}]
	     "&nbsp;"
	     [:input {:type "submit" :class "btn danger" :name "restore" :value "Restore"
		      :onclick "return confirm('Sure?')"}]
	     ])))

(defn graph-page []
  (layout
   (include-js "/js/smoothie.js")
   (include-js "/js/leaderboard.js")
   [:center
    [:canvas#chart {:width 750 :height 250}]]
   [:h2 "Players"
    [:table.zebra-striped
     [:thead
      [:tr
       [:th "Name"]
       [:th "Color"]
       [:th {:width "50%"} "Score"]
       [:th "Progress"]
       [:th "Action"]]]
     [:tbody#scores
      ]]]))

(defn do-switch-suite [suite]
  (when (non-blank? suite)
    (switch-suite! suite)
    (reset-progress!))
  (admin-page "Suite switched"))

(defn do-reset-scores []
  (reset-scores!)
  (response/redirect "/admin"))

(defn do-kick-player [id]
  (remove-player! id)
  (response/redirect "/admin/graph"))

(defn do-backup [p]
  (println "do-backup" p)
  (when (p :save)
    (println "saving")
    (save-data!))
  (when (p :restore)
    (println "restoring")
    (load-data!))
  (response/redirect "/admin"))

(defn authenticate [name pass]
  (and (= name "admin") (= pass "admin123")))

(defroutes public-routes
  (GET "/" [] (index-page))
  (GET "/scores" {h :headers} (scores-fragment h))
  (GET "/register" [] (register-page))
  (POST "/register" {params :params} (do-register (params :name) (params :url)))
  (GET "/player/:id" {params :params} (player-page (params :id)))
  (GET "/player/:id/scores" {params :params} (player-scores-fragment (params :id))))

(defroutes admin-routes
  (GET "/admin" [] (admin-page))
  (GET "/admin/graph" [] (graph-page))
  (POST "/admin/switch" {params :params} (do-switch-suite (params :suite)))
  (POST "/admin/reset" [] (do-reset-scores))
  (POST "/admin/kick" {params :params} (do-kick-player (params :id)))
  (POST "/admin/backup" {params :params} (do-backup params)))
