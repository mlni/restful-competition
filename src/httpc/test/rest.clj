(ns httpc.test.rest
  (:use [httpc.player]
	httpc.test.common
	:reload-all)
  (:gen-class))

(defn expect-success [_]
  (fn [r]
    (on-success r (respond-correct))))

(defn expect-content [session]
  (assert-content (:content session)))

; todo: make resource name randomer

(defn expect-not-found [_]
  (fn [r]
    (cond (:error r) (respond-error (:error r))
	  (= 404 (:code (:status r))) (respond-correct)
	  :else (respond-fail "Server did not respond with 404"))))

(defn put-resource [p]
  (let [random-content (generate-random-str 64)]
    (to-question :method :put
		 :suffix "/resource/bar"
		 :body random-content)))

(defn get-resource [p]
  (to-question :method :get
	       :suffix "/resource/bar"))

(defn delete-resource [p]
  (to-question :method :delete
	       :suffix "/resource/bar"))

(defn- update-state-on-success [n]
  (fn [test result response]
    (when (= :ok (:status result))
      (if (:body (:request test))
	; save the content used in PUT for later asserts
	(assoc n :content (:body (:request test)))
	n))))

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

(def *state-machine*
     {nil      [put-resource expect-success :saved]
      :saved   [get-resource expect-content :loaded]
      :loaded  [delete-resource expect-success :deleted]
      :deleted [get-resource expect-not-found nil]})

(defn multistep-testcase [p session functions]
  (let [current-state (:state session)
	[test-fn assert-fn next-state] ((construct-statemachine functions) current-state)
	next {:state next-state}]
    (make-test p
	       (test-fn p)
	       (assert-fn session)
	       :next-state (update-state-on-success next)
	       :final (nil? next))))

(defn test-restful-resource [p & {session :state}]
  "Test PUT/GET/DELETE cycle of a resource"
  (let [current-state (:state session)
	[test-fn assert-fn next-state] (*state-machine* current-state)
	next {:state next-state}]
    (make-test p
	       (test-fn p)
	       (assert-fn session)
	       :next-state (update-state-on-success next)
	       :final (nil? next-state))))
