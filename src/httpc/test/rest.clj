(ns httpc.test.rest
  (:use [httpc.player]
	httpc.test.common
	:reload-all)
  (:gen-class))

(defn- update-state-on-success [n]
  (fn [test result response]
    (when (= :ok (:status result))
      n)))

(defn- construct-statemachine [fn-pairs]
  "Given a list of functions, generate a mapping of state-names to [function next-state] pairs.
   The first state is always called nil and the last transition is to nil state."
  (let [keys (partition 2 1 (concat [nil] (range (dec (count fn-pairs))) [nil]))]
    (reduce (fn [r [k v]] (assoc r k v))
	    {}
	    (map (fn [[this next] [test-fn assert-fn]]
		   [this [test-fn assert-fn next]])
		 keys fn-pairs))))

(defn- multistep-testcase [p session workflow]
  (let [current-state (:state session)
	[test-fn assert-fn next-state] ((construct-statemachine workflow) current-state)
	next (-> session test-fn (assoc :state next-state))]
    (make-test p
	       (:question next)
	       (assert-fn session)
	       :next-state (update-state-on-success next)
	       :final (nil? next-state))))

; end of generic functionality


(defn expect-success [_]
  (fn [r]
    (on-success r (respond-correct))))

(defn expect-content [session]
  (assert-content (:content session)))

(defn expect-not-found [_]
  (fn [r]
    (cond (:error r) (respond-error (:error r))
	  (= 404 (:code (:status r))) (respond-correct)
	  :else (respond-fail "Server did not respond with 404"))))


(defn put-resource [s]
  (let [random-content (generate-random-str 100)
	resource-name (rand-nth ["foo" "bar" "baz" "quux"])
	suffix (str "/resource/" resource-name)]
    (merge s
	   {:suffix suffix
	    :content random-content
	    :question (to-question :method :put
				   :suffix suffix
				   :body random-content)})))

(defn get-resource [s]
  (assoc s
    :question (to-question :method :get
			   :suffix (:suffix s))))

(defn delete-resource [s]
  (assoc s
    :question (to-question :method :delete
			   :suffix (:suffix s))))

(defn test-restful-resource [p & {session :state}]
  "Test PUT/GET/DELETE cycle of a resource"
  (let [workflow [[put-resource expect-success]
		  [get-resource expect-content]
		  [delete-resource expect-success]
		  [get-resource expect-not-found]]]
    (multistep-testcase p session workflow)))



; test Range header

(defn put-partial-resource [s]
  (let [random-content (generate-random-str 100)
	resource-name (rand-nth ["foo1" "foo2" "foo3"])
	suffix (str "/resource/" resource-name)]
    (merge s
	   {:suffix suffix
	    :content random-content
	    :question (to-question :method :put
				   :suffix suffix
				   :body random-content)})))

(defn get-partial-resource [s]
  (let [range (random-int 30 50)]
    (merge s
	   {:range range
	    :question (to-question :method :get
				   :suffix (:suffix s)
				   :headers {"Range" (str "bytes=0-" range)})})))

; add error message indicating too long content
(defn expect-partial-content [session]
  (let [content (subs (:content session) 0 (:range session))]
   (assert-content content)))

(defn test-range-header [p & {session :state}]
  (let [workflow [[put-partial-resource expect-success]
		  [get-partial-resource expect-partial-content]]]
    (multistep-testcase p session workflow)))
