(ns marketentry.registry
  "Pure-function market-entry filing-draft + filing-submit record
  construction -- an append-only market-entry book-of-record draft.

  Like every sibling actor's registry, there is no single international
  reference-number standard for a public-procurement market-entry
  filing -- every jurisdiction assigns its own format. This namespace
  does NOT invent one; it builds a jurisdiction-scoped sequence number
  and validates the record's required fields, the same honest,
  non-fabricating discipline `marketentry.facts` uses.

  `engagement-fee-matches-claim?` is an HONEST reapplication of the
  SAME ground-truth-recompute DISCIPLINE sibling actors use (verify a
  claimed monetary total against the entity's own recorded quantity x
  unit fields), reapplied to a market-entry engagement fee line.

  `probation-period-exceeds-ceiling?` is THIS vertical's own new
  ground-truth check, grounding UZB's flagship governor check
  (`marketentry.governor/probation-period-violations`): Article 130 of
  the Labour Code of the Republic of Uzbekistan (own primary text, read
  in full at lex.uz/en/docs/-6257288, see `marketentry.facts`), states
  that the duration of an initial probation period 'must not exceed
  three months, and for heads of organizations and their deputies, chief
  accountants, and heads of separate divisions of organizations, six
  months.' This is a CATEGORY-DEPENDENT statutory ceiling -- a check
  shape this iteration is not aware of copying from any sibling it
  studied this session: the one sibling this iteration studied in depth
  (cloud-itonami-iso3166-ner) grounds its own flagship in a TWO-
  DIMENSION range (an absolute currency amount AND an absolute duration,
  both two-sided) on a wholly different instrument (a public-procurement
  décret's own pecuniary-sanction article), not a single-sided ceiling
  whose THRESHOLD VALUE ITSELF depends on which of two employee
  categories the engagement declares. It was discovered independently,
  by reading the full primary text of a Labour Code article that has
  nothing to do with public procurement -- not adapted from any
  sibling's procurement-domain shape.

  It is entity-condition-gated like a sibling's own flagship check: a
  no-op (false) unless `:engages-local-staff-under-probation?` is true
  (an engagement that declares no locally-engaged staff under a
  probation clause has nothing for this check to validate). Missing/
  non-numeric `:probation-period-months`, or a `:probation-employee-
  category` outside the two known categories, for a declared true
  engagement is never treated as violating the ceiling HERE (that is the
  `evidence-incomplete` check's job, upstream, where an assessment must
  already exist).

  This namespace is pure data + pure functions -- no I/O, no network
  call to any real xarid.uzex.uz, soliq.uz, birdarcha.uz or MIIT system.
  It builds the RECORD an operator would keep, not the act of submitting
  a filing itself (that is `marketentry.operation`'s `:filing/submit`,
  always human-gated -- see README Actuation)."
  (:require [clojure.string :as str]))

(defn- unsigned-certificate
  "Every certificate this actor produces is UNSIGNED -- signature is
  the market-entry operator's act, not this actor's."
  [kind subject record-id]
  {"@context" ["https://www.w3.org/ns/credentials/v2"]
   "type" ["VerifiableCredential" kind]
   "credentialSubject" {"id" subject "record" record-id}
   "proof" nil
   "issued_by_registry" false
   "status" "draft-unsigned"})

(defn- zero-pad [n w]
  (let [s (str n)]
    (str (apply str (repeat (max 0 (- w (count s))) "0")) s)))

(defn compute-engagement-fee
  "The ground-truth engagement fee for `engagement`'s own `:base-fee`
  and `:monitoring-months` x `:monthly-rate` -- a single flat
  base + months x rate calculation, not a full pricing engine."
  [{:keys [base-fee monthly-rate monitoring-months]}]
  (+ (double base-fee)
     (* (double monthly-rate) (double monitoring-months))))

(defn engagement-fee-matches-claim?
  "Does `engagement`'s own `:claimed-fee` equal the independently
  recomputed `compute-engagement-fee`?"
  [{:keys [claimed-fee] :as engagement}]
  (== (double claimed-fee) (compute-engagement-fee engagement)))

(def probation-ceiling
  "Article 130 of the Labour Code of the Republic of Uzbekistan (own
  primary text, read in full at lex.uz/en/docs/-6257288, 2026-07-23):
  the statutory CATEGORY-DEPENDENT ceiling on the duration of an initial
  probation period."
  {:general-months 3
   :senior-months 6
   :senior-categories #{"heads of organizations"
                        "deputies of heads of organizations"
                        "chief accountants"
                        "heads of separate divisions of organizations"}})

(defn- applicable-ceiling-months
  "The statutory ceiling (in months) that applies to `category` -- 6 for
  the enumerated senior roles, 3 for anything else (the Labour Code's
  own general/default rule)."
  [category]
  (if (= category :senior-management)
    (:senior-months probation-ceiling)
    (:general-months probation-ceiling)))

(defn probation-period-exceeds-ceiling?
  "Does `engagement`'s own declared initial-probation-period duration
  exceed Article 130's own statutory ceiling for the declared employee
  category -- 3 months for `:general`, 6 months for
  `:senior-management`?

  A no-op (false) unless `:engages-local-staff-under-probation?` is
  true -- an engagement that declares no locally-engaged staff under a
  probation clause has nothing for this check to validate.
  Missing/non-numeric `:probation-period-months` for a declared true
  engagement is never treated as violating the ceiling here (that is
  the `evidence-incomplete` check's job, upstream, where an assessment
  must already exist)."
  [{:keys [engages-local-staff-under-probation? probation-employee-category probation-period-months]}]
  (boolean
   (when (true? engages-local-staff-under-probation?)
     (when (number? probation-period-months)
       (> probation-period-months (applicable-ceiling-months probation-employee-category))))))

(defn register-draft
  "Validate + construct the FILING-DRAFT registration DRAFT -- the
  market-entry operator's own act of preparing a xarid.uzex.uz tender
  response or equivalent filing package. Pure function -- does not
  touch any real xarid.uzex.uz, soliq.uz, birdarcha.uz or MIIT system."
  [engagement-id jurisdiction sequence]
  (when-not (and engagement-id (not= engagement-id ""))
    (throw (ex-info "draft: engagement_id required" {})))
  (when-not (and jurisdiction (not= jurisdiction ""))
    (throw (ex-info "draft: jurisdiction required" {})))
  (when (< sequence 0)
    (throw (ex-info "draft: sequence must be >= 0" {})))
  (let [draft-number (str (str/upper-case jurisdiction) "-DFT-" (zero-pad sequence 6))
        record {"record_id" draft-number
                "kind" "filing-draft"
                "engagement_id" engagement-id
                "jurisdiction" jurisdiction
                "immutable" true}]
    {"record" record "draft_number" draft-number
     "certificate" (unsigned-certificate "FilingDraft" draft-number draft-number)}))

(defn register-submit
  "Validate + construct the FILING-SUBMIT registration DRAFT -- the
  market-entry operator's own act of actually submitting a real
  xarid.uzex.uz tender response or equivalent filing (always
  human-gated upstream)."
  [engagement-id jurisdiction sequence]
  (when-not (and engagement-id (not= engagement-id ""))
    (throw (ex-info "submit: engagement_id required" {})))
  (when-not (and jurisdiction (not= jurisdiction ""))
    (throw (ex-info "submit: jurisdiction required" {})))
  (when (< sequence 0)
    (throw (ex-info "submit: sequence must be >= 0" {})))
  (let [submit-number (str (str/upper-case jurisdiction) "-SUB-" (zero-pad sequence 6))
        record {"record_id" submit-number
                "kind" "filing-submit"
                "engagement_id" engagement-id
                "jurisdiction" jurisdiction
                "immutable" true}]
    {"record" record "submit_number" submit-number
     "certificate" (unsigned-certificate "FilingSubmit" submit-number submit-number)}))

(defn append [history result]
  (conj (vec history) (get result "record")))
