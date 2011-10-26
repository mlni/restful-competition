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
	       (assert-fn next)
	       :next-state (update-state-on-success next)
	       :score (:score session)
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
	resources (map #(str "foo" %1) (range 1 21 2))
	resource-name (rand-nth resources)
	suffix (str "/resource/" resource-name)]
    (merge s
	   {:suffix suffix
	    :content random-content
	    :score 2
	    :question (to-question :method :put
				   :suffix suffix
				   :headers {"Content-Type" "text/plain"}
				   :body random-content)})))

(defn get-resource [s]
  (-> s
      (assoc
	  :question (to-question :method :get
				 :headers {"Accept" "text/plain"}
				 :suffix (:suffix s)))
      (assoc :score 4)))

(defn delete-resource [s]
  (assoc s
    :question (to-question :method :delete
			   :suffix (:suffix s))))

; test Range header
(defn get-partial-resource [s]
  (let [range (random-int 30 50)]
    (merge s
	   {:range range
	    :score 6
	    :question (to-question :method :get
				   :suffix (:suffix s)
				   :headers {"Range" (str "bytes=0-" range)
					     "Accept" "text/plain"})})))

(defn expect-partial-content [session]
  (let [content (subs (:content session) 0 (:range session))]
    (fn [resp & args]
      (on-success resp
		  #(if (> (count (:content resp)) (:range session))
		     (respond-error "Content longer than expected")
		     (if (content-equals? (:content resp) content)
		       (respond-correct)
		       (respond-fail)))))))

(defn test-restful-resource [p & {session :state}]
  "Test PUT/GET/DELETE cycle of a resource"
  (let [workflow (concat [[put-resource expect-success]
			  [get-resource expect-content]]
			 (when (>= (correct-answers) 5)
			   [[get-partial-resource expect-partial-content]])
			 [[delete-resource expect-success]
			  [get-resource expect-not-found]])]
    (multistep-testcase p session workflow)))


(defn test-content-types [p & {session :state}]
  (letfn [(put-json [s]
		    (let [resources (map #(str "foo" %1) (range 2 21 2))
			  path (str "/resource/" (rand-nth resources))
			  content (generate-random-str 128)
			  json (str "{ \"data\": \"" content "\" }")]
		      (merge s
			     {:question (to-question :method :put
						  :suffix path
						  :body json
						  :headers {"Content-Type" "application/json"})
			      :content content
			      :path path
			      :json json
			      :score 4})))
	  (put-xml [s]
		   (let [xml (str "<root><data>" (:content s) "</data></root>")]
		     (merge s
			    {:question (to-question :method :put
						    :suffix (:path s)
						    :body xml
						    :headers {"Content-Type" "text/xml"})
			      :xml xml})))
	  (get-json [s]
		    (assoc s :question (to-question :suffix (:path s)
						    :headers {"Accept" "application/json"})))
	  (get-xml [s]
		   (assoc s :question (to-question :suffix (:path s)
						   :headers {"Accept" "text/xml"})))
	  (delete-resource [s]
			   (merge s
				  {:question (to-question :method :delete
							  :suffix (:path s))
				   :score 7}))
	  (assert-content-type [r type content]
			       (fn []
				 (if (.startsWith (str (get-in r [:headers :content-type])) type)
				   ((assert-content content) r)
				   (respond-fail "Content type not recognized"))))
	  (expect-json [s] (fn [r] (on-success r (assert-content-type r "application/json"
								      (:json s)))))
	  (expect-xml [s] (fn [r] (on-success r (assert-content-type r "text/xml"
								     (:xml s)))))]
    (let [wf [[put-json expect-success]
	      [put-xml expect-success]
	      [get-json expect-json]
	      [get-xml expect-xml]
	      [delete-resource expect-success]]]
      (multistep-testcase p session wf))))
