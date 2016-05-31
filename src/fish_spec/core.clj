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
