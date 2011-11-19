(ns httpc.test.common
  (:use [httpc player log]
	clojure.pprint)
  (:gen-class))

(defn make-test [player request expectation & {:as kv-pairs}]
  (merge {:final true}
	 (merge kv-pairs
		{:player player
		 :request request
		 :expect expectation})))

(defn to-question [& {:keys [suffix params headers method body]}]
  {:url suffix
   :query params
   :headers headers
   :method method
   :body body})

(defn function-name [f]
  (str (:name (meta f))))

(defn random-int [min max]
  (+ min (rand-int (- max min))))

(defn random-ints [n min max]
  (repeatedly n #(random-int min max)))

(defn create-arithmetic-testcase [ops]
  (let [[x y] (random-ints 2 1 100)
	[a b] (rand-nth [["a" "b"] ["x" "y"] ["f" "g"] ["i" "j"]])
	op (rand-nth ops)
	op-name (function-name op)]
    {:x x :y y :a a :b b
     :op op-name :result (op x y)}))

(defn content-equals? [content expected]
  (= (.toLowerCase (str expected)) (.. (str content) toLowerCase trim)))

(defn- result
  ([[s m]] (result s m))
  ([status msg]
     {:status status
      :msg msg}))

(defn- is-non-positive-code? [code]
  (let [code-int (int (if code code 0))]
    (not (<= 200 code-int 299))))

(defn respond-error
  ([] (respond-error "Server did not respond with 200"))
  ([msg] (result [:error msg])))

(defn respond-correct []
  (result [:ok "Correct"]))

(defn respond-fail
  ([] (respond-fail "Wrong answer!"))
  ([msg] (result [:fail msg])))

(defn respond-timeout
  ([] (result [:timeout "Request timed out"])))

(defn on-success [resp pred]
  (cond (:error resp) (if (.startsWith (str (:error resp)) "java.util.concurrent.TimeoutException")
			(respond-timeout)
			(respond-error (:error resp)))
	(is-non-positive-code? (:code (:status resp))) (respond-error)
	(map? pred) pred
	:else (pred)))

(defn assert-content [expected]
  (fn [resp & more]
    (on-success resp
		#(if (content-equals? (:content resp) expected)
		   (respond-correct)
		   (respond-fail)))))

(def *correct-answers*)
(def *test-name*)

(defn correct-answers []
  *correct-answers*)

(defn test-name []
  *test-name*)

(defn count-completed-tests [player test-names]
  (count (filter (fn [name] (pos? (get-in player [:completed-tests name] 0)))
		 test-names)))

(defn completed-test-names [player]
  (map first
       (filter (fn [[name completed]] (pos? completed))
	       (get player :completed-tests))))

(defn- add-scores [result test]
  (let [base-score (or (get test :score) 1)
	test-score (if (and (:final test)
			    (< (correct-answers) 10))
		     (* 10 base-score)
		     base-score)
	base-penalty (or (get test :penalty) -1)
	test-penalty (if (>= (correct-answers) 2) (* 10 base-penalty) base-penalty)
	bonus-score (or (get test :bonus-score) 0)]
    (merge result {:test-score test-score
		   :test-penalty test-penalty
		   :bonus-score bonus-score})))

(defn- maybe-add-bonus-question [test]
  (if (< (rand) 0.04)
    (-> test
	(assoc-in [:request :headers "X-The-Ultimate-Question"]
		  "What is the answer to Question of Life, the Universe and Everything?")
	(assoc-in [:bonus-answer] "42")
	(assoc-in [:bonus-score] 42))
    test))

(defn setup-test [test-fn player]
  (let [test-name (function-name test-fn)
	test-state (get-in player [:test-state test-name])]
    (binding [*test-name* test-name
	      *correct-answers* (get-in player [:completed-tests test-name] 0)]
      (-> player
	  (test-fn :state test-state :test-name test-name)
	  (assoc :name test-name)
	  maybe-add-bonus-question))))

(defn assert-test [test resp]
  (if (and (:bonus-answer test)
	   (content-equals? (:content resp) (:bonus-answer test)))
    (result :bonus "Bonus!")
    ((:expect test) resp)))

(defn assert-response! [test resp]
  "Assert the received response against the expected result."
  (binding [*test-name* (:name test)
	    *correct-answers* (get-in test [:player :completed-tests (:name test)] 0)]
   (let [result (assert-test test resp)
	 log-entry (make-log-event (add-scores result test))]
     (when (:next-state test)
       (let [ns ((:next-state test) test result resp)]
	 (set-player-attr! (:player test) [:test-state (test-name)] ns)
	 (player-log (:player test)
		     "state %s"
		     (:test-state (get-in @*players* [(get-in test [:player :id])])))))
     (when (and (= :ok (:status result))
		(:final test))
       (update-player-attr! (:player test) [:completed-tests]
			    #(update-in % [(test-name)]
					(fn [v] (inc (or v 0))))))
     (player-log (:player test) "= %s" log-entry)
     (record-event! (:player test) log-entry))))
