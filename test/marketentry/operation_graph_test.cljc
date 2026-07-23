(ns marketentry.operation-graph-test
  "Integration tests for `marketentry.operation/build` -- proves the
  REAL compiled `langgraph.graph` StateGraph runs end-to-end via
  `langgraph.graph/run*` through commit / hard-hold / escalate-approve /
  escalate-reject routes, and that the flagship
  `probation-period-exceeds-ceiling?` check genuinely blocks a
  `:filing/submit` through the compiled graph. No prior test file in
  this repo exercised `operation/build` at all -- every other test
  covers `governor`/`phase`/`facts`/`registry`/`store` in isolation,
  which proves those pure functions work but not that the graph wiring
  (`set-entry-point`/`add-conditional-edges`/`compile-graph`/
  `interrupt-before`) actually threads them together."
  (:require [clojure.test :refer [deftest is testing]]
            [langgraph.graph :as g]
            [marketentry.facts :as facts]
            [marketentry.operation :as operation]
            [marketentry.store :as store]))

(def ^:private op-context {:actor-id "operator-01" :phase 3})

(defn- exec
  ([actor tid request] (exec actor tid request op-context))
  ([actor tid request context]
   (g/run* actor {:request request :context context} {:thread-id tid})))

(deftest commit-path-engagement-intake-auto-commits-in-phase-3
  (testing ":engagement/intake is the only op in phase-3's :auto set --
            a clean intake proposal commits straight through the REAL
            compiled graph with no interrupt, and the ledger is verified
            EMPTY before the run so the post-run fact is genuinely this
            run's own effect"
    (let [s (store/seed-db)
          actor (operation/build s)]
      (is (empty? (store/ledger s)))
      (let [result (exec actor "t-commit"
                         {:op :engagement/intake :subject "eng-test-1"
                          :patch {:id "eng-test-1" :operator "Test MChJ"
                                  :jurisdiction "UZB" :status :intake}})
            state (:state result)]
        (is (= :done (:status result)))
        (is (= :commit (:disposition state)))
        (let [ledger (store/ledger s)]
          (is (= 1 (count ledger)))
          (is (= :committed (:t (first ledger))))
          (is (= :engagement/intake (:op (first ledger)))))
        (is (= "Test MChJ" (:operator (store/engagement s "eng-test-1"))))))))

(deftest hard-hold-no-spec-basis-blocks-before-escalation
  (testing "a :jurisdiction/assess proposal for an unregistered
            jurisdiction (facts/spec-basis returns nil) is a HARD
            governor violation -- the real graph routes straight to
            :hold, never pausing for human approval even though
            :jurisdiction/assess is not in phase-3's :auto set"
    (let [s (store/seed-db)
          actor (operation/build s)]
      (is (empty? (store/ledger s)))
      (let [result (exec actor "t-hold"
                         {:op :jurisdiction/assess :subject "eng-1" :no-spec? true})
            state (:state result)]
        (is (= :done (:status result)) "no interrupt -- HARD holds never pause for approval")
        (is (= :hold (:disposition state)))
        (let [ledger (store/ledger s)]
          (is (= 1 (count ledger)))
          (is (= :governor-hold (:t (first ledger))))
          (is (some #{:no-spec-basis} (map :rule (:violations (first ledger))))))))))

(deftest escalate-then-approve-commits-and-genuinely-consults-advisor
  (testing ":jurisdiction/assess is NEVER in any phase's :auto set (see
            marketentry.phase), so even a Governor-clean proposal for a
            REAL jurisdiction (UZB, with a real spec-basis) GENUINELY
            interrupts (checkpointed) at :request-approval -- the ledger
            stays EMPTY until a human resumes it. Also proves the
            Advisor's real proposal (the UZB spec-basis's own
            :provenance string, not a hardcoded literal in
            marketentry.operation) threads through
            :advise -> :govern -> :decide -> :request-approval -> :commit"
    (let [s (store/seed-db)
          actor (operation/build s)]
      (is (empty? (store/ledger s)))
      (let [held (exec actor "t-escalate" {:op :jurisdiction/assess :subject "eng-1"})]
        (is (= :interrupted (:status held)))
        (is (= [:request-approval] (:frontier held)))
        (is (empty? (store/ledger s)) "not yet committed -- awaiting human sign-off")
        (let [approved (g/run* actor {:approval {:status :approved :by "compliance-officer-01"}}
                               {:thread-id "t-escalate" :resume? true})
              approved-state (:state approved)]
          (is (= :done (:status approved)))
          (is (= :commit (:disposition approved-state)))
          (let [ledger (store/ledger s)]
            (is (= 1 (count ledger)))
            (is (= :committed (:t (first ledger))))
            (is (= :jurisdiction/assess (:op (first ledger)))))
          (let [assessment (store/assessment-of s "eng-1")]
            (is (some? assessment))
            (is (= (:provenance (facts/spec-basis "UZB")) (:spec-basis assessment))
                "the committed assessment carries the REAL UZB spec-basis's own
                provenance string -- proof the graph genuinely threads the
                Advisor's proposal through rather than hardcoding one")))))))

(deftest escalate-then-reject-holds
  (testing "a human compliance officer rejecting an escalated
            :jurisdiction/assess routes to :hold via the
            :request-approval node's own decision, and durably records
            the rejection -- not a hand-rolled parallel path"
    (let [s (store/seed-db)
          actor (operation/build s)
          _held (exec actor "t-reject" {:op :jurisdiction/assess :subject "eng-3"})
          rejected (g/run* actor {:approval {:status :rejected :by "compliance-officer-01"}}
                           {:thread-id "t-reject" :resume? true})
          rejected-state (:state rejected)]
      (is (= :done (:status rejected)))
      (is (= :hold (:disposition rejected-state)))
      (let [ledger (store/ledger s)]
        (is (= 1 (count ledger)))
        (is (= :approval-rejected (:t (first ledger))))))))

(deftest flagship-probation-ceiling-hard-hold-through-compiled-graph
  (testing "the flagship `probation-period-exceeds-ceiling?` check
            (Labour Code Article 130, category-dependent: 3 months
            general / 6 months senior-management) is genuinely folded
            into the compiled graph's :govern node -- an engagement
            declaring an 8-month probation for a :general employee (the
            3-month ceiling) HARD-blocks a :filing/submit proposal end
            to end, even with fully-satisfied evidence and a matching
            claimed fee (isolating this assertion from the OTHER hard
            checks, which are proven independently above)"
    (let [s (store/seed-db)
          _ (store/commit-record!
             s {:effect :engagement/upsert
                :value {:id "eng-test-5" :operator "Xorazm Savdo MChJ"
                        :procurement-channel "xarid.uzex.uz public procurement portal"
                        :base-fee 500000 :monthly-rate 30000 :monitoring-months 12
                        :claimed-fee 860000.0
                        :engages-local-staff-under-probation? true
                        :probation-employee-category :general
                        :probation-period-months 8
                        :requires-stir? true :stir-verified? true
                        :drafted? false :submitted? false
                        :jurisdiction "UZB" :status :intake}})
          _ (store/commit-record!
             s {:effect :assessment/set
                :path ["eng-test-5"]
                :payload {:jurisdiction "UZB"
                          :checklist (:required-evidence (facts/spec-basis "UZB"))
                          :spec-basis (:provenance (facts/spec-basis "UZB"))}})
          actor (operation/build s)]
      (is (empty? (store/ledger s)) "fixture setup writes engagement/assessment directly via the
                                     Store protocol, not the ledger -- ledger starts empty")
      (let [result (exec actor "t-probation" {:op :filing/submit :subject "eng-test-5"})
            state (:state result)]
        (is (= :done (:status result)) "no interrupt -- HARD holds never pause for approval")
        (is (= :hold (:disposition state)))
        (let [ledger (store/ledger s)]
          (is (= 1 (count ledger)))
          (is (= :governor-hold (:t (first ledger))))
          (is (some #{:probation-period-exceeds-ceiling} (map :rule (:violations (first ledger))))))))))

(deftest clean-filing-submit-escalates-never-auto-commits-even-in-phase-3
  (testing "`:filing/submit` is deliberately ABSENT from every phase's
            :auto set (marketentry.phase docstring's own stated
            invariant) -- a fully clean, evidence-complete, fee-matching,
            probation-compliant submission STILL escalates for human
            sign-off at phase-3 (full autonomy), proving the phase gate
            genuinely enforces this permanent structural rule through
            the compiled graph, not merely in the pure `phase/gate`
            function tested in isolation elsewhere"
    (let [s (store/seed-db)
          _ (store/commit-record!
             s {:effect :assessment/set
                :path ["eng-1"]
                :payload {:jurisdiction "UZB"
                          :checklist (:required-evidence (facts/spec-basis "UZB"))
                          :spec-basis (:provenance (facts/spec-basis "UZB"))}})
          actor (operation/build s)
          result (exec actor "t-clean-submit" {:op :filing/submit :subject "eng-1"})]
      (is (= :interrupted (:status result))
          "even governor-clean, phase-3, high-confidence :filing/submit
          never auto-commits -- real-world xarid.uzex.uz actuation is
          always a human call")
      (is (= [:request-approval] (:frontier result)))
      (is (empty? (store/ledger s))))))
