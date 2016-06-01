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

