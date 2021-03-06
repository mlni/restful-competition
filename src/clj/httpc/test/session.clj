(ns httpc.test.session
  (:use httpc.test.common
	[clojure.contrib.str-utils :only [str-join]]
	clojure.contrib.core))

"Support for session-supporting test cases."

(defn- pick-random-session [state n]
  (let [n (rand-int n)]
    [n (get-in state [n] {})]))

(defn- contains-cookie? [resp]
  (contains? (:headers resp) :set-cookie))

(defn- format-cookie [cookies]
; http client library returns either a string or a vector of cookies
  (str-join "; "
	    (map (fn [[k v]] (format "%s=%s" k v))
		 cookies)))

(defn- parse-cookie-header [cookies]
  (letfn [(cookie-key-value-pairs [p]
				  (let [name-value (first (.split p ";"))
					name (first (.split name-value "="))
					value (subs name-value (inc (count name)))]
				    [name value]))
	  (collect-non-empty [result [key val]]
			     (if val
			       (assoc result key val)
			       result))]
   (let [cookies (if (sequential? cookies) cookies [cookies])]
     (reduce collect-non-empty
	     {}
	     (map cookie-key-value-pairs
		  cookies)))))

(defn- calculate-next-state [next sessions sid]
  (if (nil? next)
    (fn [test result resp]
      (dissoc-in sessions [sid]))
    (fn [test result resp]
      (if (= :ok (:status result))
	(if (contains-cookie? resp)
	  (-> sessions
	      (update-in [sid :cookies] merge (parse-cookie-header (:set-cookie (:headers resp))))
	      (assoc-in [sid :state] next))
	  (assoc-in sessions [sid :state] next))
	(dissoc-in sessions [sid])))))

(defn- construct-statemachine [fns]
  "Given a list of functions, generate a mapping of state-names to [function next-state] pairs.
   The first state is always called nil and the last transition is to nil state."
  (let [keys (partition 2 1 (concat [nil]
				    (range (dec (count fns)))
				    [nil]))]
    (reduce (fn [r [k v]] (assoc r k v))
	    {}
	    (map (fn [[this next] val]
		   [this [val next]])
		 keys fns))))

(defn workflow [functions]
  (let [state-machine (construct-statemachine functions)]
    (fn [session]
      (let [[current-step next-state-name] (state-machine (:state session))]
	(-> session
	    current-step
	    (assoc :next-state next-state-name))))))

(defn session-testcase [sessions workflow parallel-sessions]
  "Run the next step in session-using test case. The test case has to be specified
   using a sequence of functions that manipulate the session state.
   The test instance is constructed using the :question and :cookies keys."
  (let [[sid session] (pick-random-session sessions parallel-sessions)
	session (workflow session)
	next-state (:next-state session)]
   (make-test (to-question :params {:q (:question session)}
			   :headers (if (:cookies session)
				      {"Cookie" (format-cookie (:cookies session))}
				      nil))
	      (assert-content (:expected session))
	      :next-state (calculate-next-state next-state
						(assoc-in sessions [sid] session)
						sid)
	      :score (:score session)
	      :final (nil? next-state))))
