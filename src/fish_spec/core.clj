;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;;  Description:
;;  A clojure.spec example specifications that generate Dr. Seuss inspired rhymes,
;;  based on http://gigasquidsoftware.com/blog/2016/05/29/one-fish-spec-fish/
;;
;;  Author: John Stevenson - @jr0cket
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns fish-spec.core
  (:require [clojure.spec :as spec]))


;;;;;;;;;;;;;;;;;;;;;;;;
;; Create data structure for numbers

;; Create a map with a finite number of fish numbers, this is a childs rhyme after all
(def fish-numbers {0 "Zero"
                   1 "One"
                   2 "Two"})

;; fish-numbers

;;;;;;;;;;;;;;;;;;;;;;;;
;; Create specification for numbers

;; use the spec/def to register the spec we are going to define for global reuse
;; use a namespaced keyword ::fish-number to express that our specification for a valid number is the keys of the fish-numbers map
(spec/def ::fish-number (set (keys fish-numbers)))


;;;;;;;;;;;;;;;;;;;;;;;;
;; Test specification for numbers

(spec/valid? ::fish-number 0)
(spec/valid? ::fish-number 1)
(spec/valid? ::fish-number 2)
(spec/valid? ::fish-number 3)
(spec/valid? ::fish-number 4)

;; Some of the above tests fail, so we can ask for an explination why

(spec/explain ::fish-number 4)

;; in the repl the following is output:
;; => val: 4 fails predicate: (set (keys fish-numbers))

;;;;;;;;;;;;;;;;;;;;;;;;
;; Create specification for colours

;; define three colours (with three letters so the output looks nice)
(spec/def ::colour #{"Red" "Blu" "Dun"})

;;;;;;;;;;;;;;;;;;;;;;;;
;; Specifying a sequence of the values

;; specify the sequence of values in the parameter vector: two numbers followed by two colours

(spec/def ::first-line (spec/cat :number1 ::fish-number
                                 :number2 ::fish-number
                                 :colour1 ::colour
                                 :colour2 ::colour))

;; the spec is associating each part with a tag, to identify what was matched or not, and its predicate/pattern

;; if we try to explain a failing spec, it will tell us where it went wrong

;; Test the spec with an incorrect colour
(spec/explain ::first-line [1 2 "Red" "Blk"])

;; => In: [3] val: "Black" fails spec: :fish-spec.core/colour at: [:colour2] predicate: #{"Blue" "Dun" "Red"}

;;;;;;;;;;;;;;;;;;;;;;;;
;; More tests.

;; The second number should be bigger than the first
(defn one-bigger?[{:keys [number1 number2]}]
  (= number2 (inc number1)))

;; test one-bigger?
(one-bigger? {:number1 1 :number2 2})
(one-bigger? {:number1 10 :number2 20})

;; The colours should be different values, so lets create an updated specification for that

(spec/def ::first-line (spec/and (spec/cat :number1 ::fish-number :number2 ::fish-number :colour1 ::colour :colour2 ::colour)
                                 one-bigger?
                                 #(not= (:colour1 %) (:colour2 %))))


;; now we can test if our data is valid

(spec/valid? ::first-line [1 2 "Red" "Blu"])
(spec/valid? ::first-line [1 2 "Red" "Blk"])
(spec/valid? ::first-line [3 0 "Red" "Blu"])
(spec/valid? ::first-line [3 2 "Grn" "Blu"])


;;;;;;;;;;;;;;;;;;;;;;;;
;; Peeking underneath the spec

;; get the destructured, conformed values using spec/conform, which returns the tags along with the values

(spec/conform ::first-line [1 2 "Red" "Blu"])
;; => {:number1 1, :number2 2, :colour1 "Red", :colour2 "Blu"}

(spec/valid? ::first-line [2 1 "Red" "Blu"])

(spec/explain ::first-line [2 1 "Red" "Blu"])
;; => val: {:number1 2, :number2 1, :colour1 "Red", :colour2 "Blu"} fails predicate: one-bigger?


;;;;;;;;;;;;;;;;;;;;;;;;
;; Generating test data

;; The s/exercise function will generate data for your specifications.
;; It will generate 10 items by default, or we can provide an argument as to how many items are wanted

(spec/exercise ::first-line 7)

#_([(0 1 "Red" "Blu") {:number1 0, :number2 1, :colour1 "Red", :colour2 "Blu"}]
   [(0 1 "Blu" "Dun") {:number1 0, :number2 1, :colour1 "Blu", :colour2 "Dun"}]
   [(1 2 "Blu" "Dun") {:number1 1, :number2 2, :colour1 "Blu", :colour2 "Dun"}]
   [(0 1 "Blu" "Red") {:number1 0, :number2 1, :colour1 "Blu", :colour2 "Red"}]
   [(1 2 "Dun" "Blu") {:number1 1, :number2 2, :colour1 "Dun", :colour2 "Blu"}]
   [(1 2 "Dun" "Blu") {:number1 1, :number2 2, :colour1 "Dun", :colour2 "Blu"}]
   [(0 1 "Red" "Blu") {:number1 0, :number2 1, :colour1 "Red", :colour2 "Blu"}])


;; The spec/exercise generates the results, but not everything makes a good rhyme.
;; For example, 2 does not rhyme with dun, but it does rhyme with blue


;;;;;;;;;;;;;;;;;;;;;;;;
;; Adding rhyming predicate to specification

;; Add an extra predicate (true/false check) to ensure the results always rhyme

(defn fish-numbers-rhymes-with-colour? [{number :number2 colour :colour2}]
  (or
   (= [number colour] [2 "Blu"])
   (= [number colour] [1 "Dun"])))

;; Now we redefine ::first-line specification to include this extra predicate to the existing predicates for the specification

(spec/def ::first-line (spec/and (spec/cat :number1 ::fish-number :number2 ::fish-number :colour1 ::colour :colour2 ::colour)
                                 one-bigger?
                                 #(not= (:colour1 %) (:colour2 %))
                                 fish-numbers-rhymes-with-colour?))

(spec/valid? ::first-line [1 2 "Red" "Blu"])
(spec/explain ::first-line [1 2 "Red" "Dun"])

;; Exception Unable to resolve spec: :first-line  clojure.spec/the-spec (spec.clj:95)
;; val: {:number1 1, :number2 2, :colour1 "Red", :colour2 "Dun"} fails predicate: fish-numbers-rhymes-with-colour?

;; lets generate some tests again, based on the updated specification

(spec/exercise ::first-line)

#_([(0 1 "Red" "Dun") {:number1 0, :number2 1, :colour1 "Red", :colour2 "Dun"}]
   [(0 1 "Blu" "Dun") {:number1 0, :number2 1, :colour1 "Blu", :colour2 "Dun"}]
   [(1 2 "Dun" "Blu") {:number1 1, :number2 2, :colour1 "Dun", :colour2 "Blu"}]
   [(0 1 "Red" "Dun") {:number1 0, :number2 1, :colour1 "Red", :colour2 "Dun"}]
   [(0 1 "Blu" "Dun") {:number1 0, :number2 1, :colour1 "Blu", :colour2 "Dun"}]
   [(1 2 "Red" "Blu") {:number1 1, :number2 2, :colour1 "Red", :colour2 "Blu"}]
   [(1 2 "Red" "Blu") {:number1 1, :number2 2, :colour1 "Red", :colour2 "Blu"}]
   [(0 1 "Red" "Dun") {:number1 0, :number2 1, :colour1 "Red", :colour2 "Dun"}]
   [(1 2 "Red" "Blu") {:number1 1, :number2 2, :colour1 "Red", :colour2 "Blu"}]
   [(1 2 "Red" "Blu") {:number1 1, :number2 2, :colour1 "Red", :colour2 "Blu"}])

;; Emacs tip: use M-x cider-pprint-eval-last-sexp or C-c C-p to pretty print the output of the spec/exercise function in a seperate window and then paste it back in here after a #_ to comment the whole data structure


;;;;;;;;;;;;;;;;;;;;;;;;
;; Using core.spec with functions

;; create a function that will create a string of our rhyme

(defn fish-line
  "Create our rhyme in a nicely formatted string"
  [number1 number2 colour1 colour2]
  (clojure.string/join " "
                       (map #(str % " fish.")
                            [(get fish-numbers number1)
                             (get fish-numbers number2)
                             colour1
                             colour2])))


;;;;;;;;;;;;;;;;;;;;;;;;
;; Deconstructing the fish-line function

;; The expression in the function body of fish-line is quite big, so lets build up to it in smaller parts so we can see what its doing.

;; lets create a vector of values by looking up the values in the fish-numbers data structure we defined right at the start of the code

[(get fish-numbers 1)
 (get fish-numbers 2)]

;; in the fish-line function we also include the strings of the colours we passed as arguments when calling the function.  Those arguments have no context here, so we just hard code the colour strings in our example.
[(get fish-numbers 1)
 (get fish-numbers 2)
 "Red"
 "Blu"]

;; Now we can add the string " fish." to the end of each value in the vector we have created.  Using the map function, we take each element from the vector and give it to our anonymous function in the % position.  The str function inside our anonymous function simply returns a new string made from the current element from the vector and " fish."  Once map has iterated over all the elements in the vector the new vector of fish strings is returned.
(map #(str % " fish.")
     [(get fish-numbers 1)
      (get fish-numbers 2)
      "Red"
      "Blu"])

;; finaly we join the string values in the vector together into one string, using a white space, " ", as a seperator

(clojure.string/join " "
                     (map #(str % " fish.")
                          [(get fish-numbers 1)
                           (get fish-numbers 2)
                           "Red"
                           "Blu"]))

;; So the above code is exactly the same as:
(clojure.string/join " "
                     ["One fish." "Two fish." "Red fish." "Blu fish."])


;; Okay, now back to clojure.spec

;;;;;;;;;;;;;;;;;;;;;;;;
;; Adding a specification to the fish-line function

;; A specification that checks that the arguements passed to the function are validated with ::first-line and the return value is a string
(spec/fdef fish-line
           :args ::first-line
           :ret string?)

;; Instrument the fish-line function
(spec/instrument #'fish-line)

;; lets test the call to fish-line function with data that should pass
(fish-line 1 2 "Red" "Blu")

;; now what happens when we provide arguements that fail the tests
(fish-line 2 1 "Red" "Blu")

;; ExceptionInfo Call to #'fish-spec.core/fish-line did not conform to spec:
;; val: {:number1 2, :number2 1, :colour1 "Red", :colour2 "Blu"} fails at: [:args] predicate: one-bigger?
;; :clojure.spec/args  (2 1 "Red" "Blu")
;;   clojure.core/ex-info (core.clj:4617)
