(ns httpc.test.rest
  (:use [httpc.player]
	httpc.test.common
	:reload-all)
  (:gen-class))

(defn expect-success [next-state & args]
  (fn [r]
    (let [[status msg] (on-success r (respond-correct))]
      (if (= status :ok)
	[(make-log-event status msg) next-state]
	(make-log-event status msg)))))

(defn expect-content [next-state session]
  (assert-content (:content session)))

(defn expect-not-found [next-state & args]
  (fn [r]
    (cond (:error r) (apply make-log-event (respond-error (:error r)))
	  (= 404 (:code (:status r)))
	  [(apply make-log-event (respond-correct)) next-state]
	  :else (apply make-log-event (respond-fail)))))

(defn put-resource [p assert]
  (make-test p
	     (to-question p
			  :method :put
			  :suffix "/resource/foo")
	     assert))

(defn get-resource [p assert]
  (make-test p
	     (to-question p
			  :method :get
			  :suffix "/resource/foo")
	     assert))

(defn delete-resource [p assert]
  (make-test p
	     (to-question p
			  :method :delete
			  :delete "/resource/foo")
	     assert))


(def *state-machine*
     {nil      [put-resource expect-success :saved]
      :saved   [get-resource expect-success :loaded]
      :loaded  [delete-resource expect-success :deleted]
      :deleted [get-resource expect-not-found nil]})

(defn test-restful-resource [p & {session :state}]
  "Test PUT/GET/DELETE cycle of a resource"
  (let [state (:state session)
	[test-fn assert-fn next-state] (*state-machine* state)
	next {:state next-state}]
    (test-fn p (assert-fn next session))))
