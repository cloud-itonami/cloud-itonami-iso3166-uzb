(ns statute.facts-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [statute.facts :as facts]))

(deftest uzb-has-spec-basis
  (let [sb (facts/spec-basis "UZB")]
    (is (= 2 (count sb)))
    (is (every? #(str/starts-with? (:statute/url %) "https://") sb))
    (is (every? :statute/law-number sb))))

(deftest unknown-jurisdiction-has-no-spec-basis
  (is (nil? (facts/spec-basis "ATL")))
  (is (nil? (facts/spec-basis "ZZZ"))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["UZB" "JPN" "ATL"])]
    (is (= 3 (:requested c)))
    (is (= 1 (:covered c)))
    (is (= ["ATL" "JPN"] (:missing-jurisdictions c)))))

(deftest by-topic-filters
  (is (= ["uzb.labour-code-2022"]
         (mapv :statute/id (facts/by-topic "UZB" :labor))))
  (is (= ["uzb.investment-law-2019"]
         (mapv :statute/id (facts/by-topic "UZB" :investment))))
  (is (empty? (facts/by-topic "UZB" :environment)))
  (is (empty? (facts/by-topic "ATL" :labor))))
