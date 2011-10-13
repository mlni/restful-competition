(ns httpc.test.session
  (:use httpc.test.common
	[clojure.contrib.str-utils :only [str-join]]))

(defn pick-random-session [state n]
  (let [n (rand-int n)]
    [n (get state n {})]))

(defn expect-name [s]
  (assert-content (:expected s)))

(defn initialize-session [s]
  (let [name (rand-nth ["Matti" "Bill" "Bob" "John" "Steve" "Donald"])]
    (-> s
	(assoc :expected name)
	(assoc :question (format "My name is %s. What is my name" name)))))

(defn resend-question [session]
  (assoc session :question "What is my name"))

(def session-statemachine {nil   [initialize-session :sent1]
			   :sent1 [resend-question :sent2]
			   :sent2 [resend-question :sent3]
			   :sent3 [resend-question nil]})

(defn contains-cookie? [resp]
  (contains? (:headers resp) :set-cookie))

(defn format-cookie [cookies]
  ; http client library returns either a string or a vector of cookies
  (let [cookies (if (sequential? cookies) cookies [cookies])]
    (str-join "; "
	      (map (fn [p] (let [ps (.split p ";")]
			     (first ps)))
		   cookies))))

(defn calculate-next-state [next sessions sid]
  (if (nil? next)
    (fn [test result resp]
      (dissoc sessions sid))
    (fn [test result resp]
      (if (= :ok (:status result))
	(if (contains-cookie? resp)
	  (-> sessions
	      (assoc-in [sid :cookies] (format-cookie (:set-cookie (:headers resp))))
	      (assoc-in [sid :state] next))
	  (assoc-in sessions [sid :state] next))
	(dissoc sessions sid)))))

(defn test-my-name-session [p & {sessions :state}]
  (let [correct-answers (get-in p [:completed-tests "test-my-name-session"] 0)
	parallel-session (if (> correct-answers 5) 3 1)
	[sid session] (pick-random-session sessions parallel-session)
	[test-fn next-state] (session-statemachine (session :state))
	session (test-fn session)]
   (make-test p
	      (to-question :params {:q (:question session)}
			   :headers (if (:cookies session)
				      {"Cookie" (:cookies session)}
				      nil)
			   :session session)
	      (expect-name session)
	      :next-state (calculate-next-state next-state (assoc sessions sid session) sid)
	      :final (nil? next-state))))
