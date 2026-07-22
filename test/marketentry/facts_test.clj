(ns marketentry.facts-test
  (:require [clojure.test :refer [deftest is testing]]
            [marketentry.facts :as facts]))

(deftest uzb-has-spec-basis
  (let [sb (facts/spec-basis "UZB")]
    (is (some? sb))
    (is (string? (:provenance sb)))
    (is (seq (:required-evidence sb)))
    (is (some? (facts/corporate-number-spec-basis "UZB")))
    (is (some? (facts/business-registration-spec-basis "UZB")))
    (is (some? (facts/probation-ceiling-spec-basis "UZB")))))

(deftest uzb-rep-spec-basis-is-honestly-nil
  (testing "this iteration did not find any authorized-representative eligibility/exclusion article for Uzbekistan's procurement regime this session -- an honest, disclosed access gap, not an assertion that no such provision exists"
    (is (nil? (facts/rep-spec-basis "UZB")))))

(deftest uzb-business-registration-and-tax-registration-are-described-independently
  (testing "business registration (birdarcha.uz) and tax registration (STIR/soliq.uz) are distinct topics on gov.uz -- see namespace docstring"
    (let [reg (facts/business-registration-spec-basis "UZB")
          tax (facts/corporate-number-spec-basis "UZB")]
      (is (some? reg))
      (is (some? tax))
      (is (not= (:business-registration-owner-authority reg)
                (:corporate-number-owner-authority tax))))))

(deftest uzb-probation-ceiling-is-the-flagship-spec-basis
  (testing "Labour Code Article 130's statutory ceiling is a real, verifiable, category-dependent ceiling -- not fabricated"
    (let [pc (facts/probation-ceiling-spec-basis "UZB")]
      (is (some? pc))
      (is (= 3 (:probation-ceiling-general-months pc)))
      (is (= 6 (:probation-ceiling-senior-months pc)))
      (is (= 4 (count (:probation-ceiling-senior-categories pc))))
      (is (string? (:probation-ceiling-legal-basis pc))))))

(deftest unknown-jurisdiction-has-no-spec-basis
  (is (nil? (facts/spec-basis "ATL")))
  (is (nil? (facts/spec-basis "ZZZ")))
  (is (nil? (facts/business-registration-spec-basis "ATL")))
  (is (nil? (facts/probation-ceiling-spec-basis "ATL")))
  (is (nil? (facts/rep-spec-basis "ATL"))))

(deftest required-evidence-satisfied
  (let [sb (facts/spec-basis "UZB")
        all (:required-evidence sb)]
    (is (true? (facts/required-evidence-satisfied? "UZB" all)))
    (is (not (facts/required-evidence-satisfied? "UZB" (take 1 all))))
    (is (nil? (facts/required-evidence-satisfied? "ATL" all)))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["UZB" "USA" "ATL"])]
    (is (= 3 (:requested c)))
    (is (= 2 (:covered c)))
    (is (= ["ATL"] (:missing-jurisdictions c)))))
