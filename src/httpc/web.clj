(ns httpc.web
  (:require (ring.util [response :as response]))
  (:use [hiccup core page-helpers form-helpers]
	[httpc player]))


(defn- non-blank? [s]
  (and (not (nil? s))
       (not= "" (.trim s))))

(defn- format-time [ts]
  (.format (java.text.SimpleDateFormat. "HH:mm:ss")
	   (java.util.Date. ts)))

(defn layout [& body]
  (html5
   [:head
    [:title "Restful world"]]
   [:body
    body]))

(defn index-page []
  (layout
   [:h1 "Hello restful world!"]
   [:p (link-to "/register" "I want to play!")]
   [:table.data
    [:tr
     [:th "Player name"]
     [:th "Points"]]
    (for [p (players-by-score)]
      [:tr
       [:td (link-to (str "/player/" (:id p)) (:name p))]
       [:td (:score p)]])]))

(defn register-page [& [name url msg]]
  (layout
   [:h1 "Register"]
   (if msg
     [:div.error msg])
   (form-to [:post "/register"]
	    (label :name "Team name:")
	    (text-field :name name)
	    [:br]
	    (label :url "Server url:")
	    (text-field :url url)
	    [:br]
	    (submit-button "Register now"))))

(defn do-register [name url]
  (if (and (non-blank? name)
	   (non-blank? url))
    (if (not (player-exists? name))
      (do
	(add-player! name url)
	(response/redirect "/"))
      (register-page name url "Name already exists"))
    (register-page name url "Please fill in team name and server url")))

(defn player-page [id]
  (let [p (player-by-id id)] ; todo: redirect to root if player not found
    (layout
     [:h1 (:name p)
      [:div.score "Score: " (:score p)]
      [:h2 "Log"]
      [:table.data
       [:tr
	[:th "Time"]
	[:th "Event"]
	[:th "Score"]
	[:th "Message"]]
       (for [evt (:log p)]
	 [:tr
	  [:td (format-time (:time evt))]
	  [:td (:status evt)]
	  [:td (:score evt)]
	  [:td (:message evt)]])]])))