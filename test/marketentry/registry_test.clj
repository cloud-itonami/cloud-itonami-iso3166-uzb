(ns marketentry.registry-test
  (:require [clojure.test :refer [deftest is testing]]
            [marketentry.registry :as registry]))

(deftest engagement-fee-recompute
  (let [e {:base-fee 500000 :monthly-rate 30000 :monitoring-months 12 :claimed-fee 860000.0}]
    (is (== 860000.0 (registry/compute-engagement-fee e)))
    (is (true? (registry/engagement-fee-matches-claim? e))))
  (let [bad {:base-fee 500000 :monthly-rate 30000 :monitoring-months 12 :claimed-fee 999000.0}]
    (is (false? (registry/engagement-fee-matches-claim? bad)))))

(deftest register-draft-and-submit
  (let [d (registry/register-draft "eng-1" "UZB" 0)
        s (registry/register-submit "eng-1" "UZB" 0)]
    (is (= "UZB-DFT-000000" (get d "draft_number")))
    (is (= "UZB-SUB-000000" (get s "submit_number")))
    (is (nil? (get-in d ["certificate" "proof"])))
    (is (= "draft-unsigned" (get-in s ["certificate" "status"])))))

(deftest register-requires-ids
  (is (thrown? Exception (registry/register-draft "" "UZB" 0)))
  (is (thrown? Exception (registry/register-submit "eng-1" "" 0))))

(deftest probation-period-exceeds-ceiling-recompute
  (testing "Labour Code Art.130 -- a declared general-category probation period within the 3-month ceiling is fine"
    (is (false? (registry/probation-period-exceeds-ceiling?
                 {:engages-local-staff-under-probation? true
                  :probation-employee-category :general :probation-period-months 3}))))
  (testing "a declared general-category probation period ABOVE the 3-month ceiling is a violation"
    (is (true? (registry/probation-period-exceeds-ceiling?
                {:engages-local-staff-under-probation? true
                 :probation-employee-category :general :probation-period-months 4}))))
  (testing "a declared senior-management probation period within the 6-month ceiling is fine"
    (is (false? (registry/probation-period-exceeds-ceiling?
                 {:engages-local-staff-under-probation? true
                  :probation-employee-category :senior-management :probation-period-months 6}))))
  (testing "a declared senior-management probation period ABOVE the 6-month ceiling is a violation"
    (is (true? (registry/probation-period-exceeds-ceiling?
                {:engages-local-staff-under-probation? true
                 :probation-employee-category :senior-management :probation-period-months 7}))))
  (testing "exactly at each category's own ceiling does not violate the range"
    (is (false? (registry/probation-period-exceeds-ceiling?
                 {:engages-local-staff-under-probation? true
                  :probation-employee-category :general :probation-period-months 3})))
    (is (false? (registry/probation-period-exceeds-ceiling?
                 {:engages-local-staff-under-probation? true
                  :probation-employee-category :senior-management :probation-period-months 6}))))
  (testing "a general-category duration that would be fine for senior-management (e.g. 6 months) still violates the general 3-month ceiling"
    (is (true? (registry/probation-period-exceeds-ceiling?
                {:engages-local-staff-under-probation? true
                 :probation-employee-category :general :probation-period-months 6}))))
  (testing "entity-condition-gated: a no-op (false) unless :engages-local-staff-under-probation? is true"
    (is (false? (registry/probation-period-exceeds-ceiling?
                 {:engages-local-staff-under-probation? false
                  :probation-employee-category :general :probation-period-months 999})))
    (is (false? (registry/probation-period-exceeds-ceiling? {}))))
  (testing "missing/non-numeric probation-period-months for a declared true engagement is never treated as violating the ceiling here (evidence-incomplete's job)"
    (is (false? (registry/probation-period-exceeds-ceiling?
                 {:engages-local-staff-under-probation? true
                  :probation-employee-category :general})))
    (is (false? (registry/probation-period-exceeds-ceiling?
                 {:engages-local-staff-under-probation? true
                  :probation-employee-category :general :probation-period-months nil})))))
