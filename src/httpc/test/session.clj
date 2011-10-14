(ns httpc.test.session
  (:use httpc.test.common
	[clojure.contrib.str-utils :only [str-join]]
	clojure.contrib.core))

(defn- pick-random-session [state n]
  (let [n (rand-int n)]
    [n (get-in state [n] {})]))

(defn- contains-cookie? [resp]
  (contains? (:headers resp) :set-cookie))

(defn- format-cookie [cookies]
  ; http client library returns either a string or a vector of cookies
  (let [cookies (if (sequential? cookies) cookies [cookies])]
    (str-join "; "
	      (map (fn [p] (let [ps (.split p ";")]
			     (first ps)))
		   cookies))))

(defn- calculate-next-state [next sessions sid]
  (if (nil? next)
    (fn [test result resp]
      (dissoc-in sessions [sid]))
    (fn [test result resp]
      (if (= :ok (:status result))
	(if (contains-cookie? resp)
	  (-> sessions
	      (assoc-in [sid :cookies] (format-cookie (:set-cookie (:headers resp))))
	      (assoc-in [sid :state] next))
	  (assoc-in sessions [sid :state] next))
	(dissoc-in sessions [sid])))))

(defn- construct-statemachine [fns]
  (let [keys (partition 2 1 (concat [nil]
				    (range (dec (count fns)))
				    [nil]))]
    (reduce (fn [r [k v]] (assoc r k v))
	    {}
	    (map (fn [[this next] val]
		   [this [val next]])
		 keys fns))))

(defn session-testcase [p sessions functions parallel-sessions]
  (let [statemachine (construct-statemachine functions)
	[sid session] (pick-random-session sessions parallel-sessions)
	[test-fn next-state] (statemachine (:state session))
	session (test-fn session)]
   (make-test p
	      (to-question :params {:q (:question session)}
			   :headers (if (:cookies session)
				      {"Cookie" (:cookies session)}
				      nil)
			   :session session); is this actually used?
	      (assert-content (:expected session))
	      :next-state (calculate-next-state next-state
						(assoc-in sessions [sid] session)
						sid)
	      :final (nil? next-state))))


; test remembering my name

(defn test-my-name-session [p & {sessions :state testname :test-name}]
  (letfn [(init [s]
		(let [name (rand-nth ["Matti" "Bill" "Bob" "John" "Steve"
				      "Donald" "Dennis" "Rob" "Edsger" "Martin"])]
		  (-> s
		      (assoc :expected name)
		      (assoc :question (format "My name is %s. What is my name" name)))))
	  (resend [s]
		  (assoc s :question "What is my name"))]
   (let [correct-answers (get-in p [:completed-tests testname] 0)
	 parallel-sessions (if (> correct-answers 5) 3 1)
	 workflow [init
		   resend
		   resend]]
     (session-testcase p sessions workflow parallel-sessions))))


; test arithmetics with sessions

(defn test-arithmetic-with-session [p & {sessions :state testname :test-name}]
  (letfn [(init [s]
		(let [{:keys [x y a b op result]} (create-arithmetic-testcase)]
		  (merge s {:param1 a :param2 b :val1 x :val2 y :op-name op :result result
			    :expected x :question (format "Let %s be %s. What is %s" a x a)})))
	  (arg2 [s]
		(let [{:keys [param2 val2]} s]
		  (-> s
		      (assoc :question (format "Let %s be %s. What is %s" param2 val2 param2))
		      (assoc :expected val2))))
	  (result [s]
		  (let [{:keys [param1 param2 op-name result]} s]
		    (-> s
			(assoc :question
			  (format "Remember how much is %s %s %s" param1 op-name param2))
			(assoc :expected result))))]
    
   (let [corrects (get-in p [:completed-tests testname] 0)
	 num-sessions (if (>= corrects 5) 3 1)
	 workflow [initialize-session-arithmetic
		   send-second-argument
		   send-arithmetic-question]]
     (session-testcase p sessions workflow num-sessions))))
