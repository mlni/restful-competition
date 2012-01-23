(ns httpc.test.stateful
  (:use httpc.test.common
	httpc.test.session))

(defn- question [fmt & args]
  (apply format (concat [fmt] args)))

(defn test-my-name-session [& {sessions :state}]
  "Remember my name"
  (letfn [(init [s]
		(let [name (rand-nth ["Matti" "Bill" "Bob" "John" "Steve"
				      "Donald" "Dennis" "Rob" "Edsger" "Martin"])]
		  (-> s
		      (assoc :expected name)
		      (assoc :question (question "My name is %s. What is my name" name))
		      (assoc :score 3))))
	  (resend [s]
		  (-> s
		      (assoc :question (question "What is my name"))
		      (assoc :score 5)
		      (assoc :penalty -2)))]
   (let [parallel-sessions (if (> (correct-answers) 5) 3 1)
	 wf (workflow [init
		       resend
		       resend])]
     (session-testcase sessions wf parallel-sessions))))


(defn test-arithmetic-with-session [& {sessions :state}]
  "Remember values of parameters and calculate the value of an arithmetic expression"
  (letfn [(init [s]
		(let [{:keys [x y a b op result]} (create-arithmetic-testcase [* - +])]
		  (merge s {:param1 a :param2 b :val1 x :val2 y :op-name op :result result
			    :expected x :question (question "Let %s be %s. What is %s" a x a)
			    :score 2
			    :penalty -1})))
	  (arg2 [s]
		(let [{:keys [param2 val2]} s]
		  (-> s
		      (assoc :question (question "Let %s be %s. What is %s" param2 val2 param2))
		      (assoc :expected val2)
		      (assoc :score 5)
		      (assoc :penalty -3))))
	  (result [s]
		  (let [{:keys [param1 param2 op-name result]} s]
		    (-> s
			(assoc :question
			  (question "Remember how much is %s %s %s" param1 op-name param2))
			(assoc :expected result)
			(assoc :score 5)
			(assoc :penalty -2))))]
   (let [num-sessions (complicate 1 3)
	 wf (workflow [init
		       arg2
		       result])]
     (session-testcase sessions wf num-sessions))))

(defn test-who-is-taller [& {sessions :state}]
  (letfn [(inch-to-cm [i]
		      (int (Math/round (* i 2.54))))
	  (tallness [inches]
		    (let [feet (quot inches 12)
			  inches (mod inches 12)]
		      (str feet "'" (if (zero? inches) "" (str inches "\"")))))
	  (step1 [s]
		 (let [[t1 t2] (random-ints 2 65 75)]
		   (merge s
			  {:question (format "Bill is %s tall. How tall is he in centimeters"
					     (tallness t1))
			   :expected (inch-to-cm t1)
			   :bob-tallness t2
			   :taller (if (< t1 t2) "Bob" "Bill")
			   :score 5})))
	  (step2 [s]
		 (-> s
		     (assoc :question (format "Bob is %s cm tall. How tall is he in inches"
					      (inch-to-cm (:bob-tallness s))))
		     (assoc :expected (:bob-tallness s))))
	  (step3 [s]
		 (-> s
		     (assoc :question "Which of them is taller")
		     (assoc :expected (:taller s))))]
    (let [num-sessions (complicate 1 3)
	  wf (workflow [step1
			step2
			step3])]
      (session-testcase sessions wf num-sessions))))