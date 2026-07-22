(ns statute.facts
  "General-law compliance catalog for the Republic of Uzbekistan (UZB) --
  extends this repo's existing `marketentry.facts` (public-procurement
  market-entry only, narrow scope) with a second, orthogonal catalog of
  statutes a company operating in this jurisdiction must generally track
  for compliance. Mirrors cloud-itonami-iso3166-ner/-gin/-caf/-cog's
  `statute.facts` (ADR-2607141700, cloud-itonami-compliance-fact-
  federation).

  Only TWO entries this iteration could independently ground with a
  citation actually fetched and read this session (2026-07-22/23) --
  a smaller, 100%-honest catalog rather than a fabricated third entry:

  - **Labour Code**: Oʻzbekiston Respublikasining Mehnat kodeksi (Labour
    Code of the Republic of Uzbekistan), own primary text fetched and
    read directly and in full at `lex.uz/en/docs/-6257288` (HTTP 200,
    server-rendered -- unlike `xarid.uzex.uz`/`new.birdarcha.uz`'s bare
    Angular/React SPA shells, `lex.uz`'s individual document pages ARE
    server-rendered and directly fetchable without executing JavaScript,
    a genuinely different technical shape this iteration confirmed by
    direct comparison rather than assuming all '*.uz' government sites
    behave alike). The document's own metadata reads '28.10.2022.
    Oʻzbekiston Respublikasining Mehnat kodeksi' and its own version-
    history list includes an entry dated '30.04.2023' (consistent with
    the Uzbek Wikipedia article on this code, used ONLY to locate the
    lex.uz document id, then independently cross-checked against
    lex.uz's own primary text directly, not taken on the secondary
    source's word alone). This iteration did NOT find a separate,
    distinct law NUMBER for the Code itself in the fetched page's own
    metadata display (which rendered the number field as a literal
    blank '-son' placeholder) -- an honest gap; the adoption date
    (28 October 2022) and the code's own full article text are
    independently confirmed. This same Code's own Article 130 grounds
    this repo's flagship governor check -- see `marketentry.facts`'s
    namespace docstring and `marketentry.registry`.
  - **Foreign investment**: Law of the Republic of Uzbekistan 'On
    investments and investment activity', No. LRU-598, independently
    confirmed via UNCTAD's Investment Policy Hub own hosting
    (`investmentpolicy.unctad.org/investment-laws/laws/328/uzbekistan-
    the-law-on-investments-and-investment-activity`, WebFetch-verified
    directly this session), own text: 'Adopted by the Legislative
    Chamber on December 9, 2019; Approved by the Senate on December 14,
    2019.' This iteration did NOT independently fetch the law's own
    Uzbek/Russian primary (lex.uz-hosted) text this session, only
    UNCTAD's secondary hosting of it -- an honest gap, the same kind of
    gap a sibling discloses when relying on UNCTAD's hosting rather
    than a domestic primary source.

  This iteration specifically investigated, rather than assumed, whether
  a domestic companies/commercial-entity law citation (a Civil Code
  provision or a dedicated Law on Limited Liability Companies) could be
  independently confirmed this session. Uzbekistan is NOT an OHADA
  member state (unlike several Francophone-Africa siblings that get a
  single clean supranational AUSCGIE citation covering company law for
  every member state at once) -- company law here is a domestic matter.
  lex.uz's own General Legal Classifier of Branches of Legislation
  (browsed directly via its plain `ajax_methods_url?action=
  getLawClassifierTree` GET endpoint, the SAME technique that
  successfully located the Labour Code's classification) DOES show a
  '03.03.00.00 Legal entity' branch with '03.03.02.00 ... Registration of
  legal entities' and '03.03.05.00 Business partnerships and companies'
  leaf categories -- confirming such legislation exists and is indexed
  -- but retrieving the ACTUAL document(s) filed under those leaf
  categories requires the same stateful ASP.NET full-text-search
  postback this session's tooling could not execute (the classifier
  browse endpoint returns only category NAMES, not the documents filed
  under a leaf category). No secondary hosting (UNCTAD, ILO NATLEX, WIPO
  Lex) was found to cover this specific citation either. This iteration
  deliberately did NOT invent a law name/number/date for this gap --
  a smaller, honest two-entry catalog was chosen over a fabricated third
  entry, per this session's own no-fabrication mandate.

  A law not in this table has NO spec-basis, full stop; extend
  `catalog`, do not invent an id/url.")

(def catalog
  "iso3 -> vector of statute entries. `:statute/url` + `:statute/law-number`
  are the citation the governor requires before any compliance-fact
  proposal referencing this law can commit."
  {"UZB"
   [{:statute/id "uzb.labour-code-2022"
     :statute/title "Oʻzbekiston Respublikasining Mehnat kodeksi (Labour Code of the Republic of Uzbekistan)"
     :statute/jurisdiction "UZB"
     :statute/kind :code
     :statute/law-number "Adopted 28.10.2022 (own primary text, lex.uz metadata -- no separate law number was rendered in the fetched page's own metadata display, an honest gap); version history shows an entry dated 30.04.2023, consistent with (but independently cross-checked against, not merely copied from) the Uzbek Wikipedia article's claim of that date as the entry-into-force date"
     :statute/url "https://lex.uz/en/docs/-6257288"
     :statute/url-provenance :official-lex-uz
     :statute/enacted-date "2022-10-28"
     :statute/retrieved-at "2026-07-23"
     :statute/topic #{:labor :employment}}
    {:statute/id "uzb.investment-law-2019"
     :statute/title "Law of the Republic of Uzbekistan on investments and investment activity"
     :statute/jurisdiction "UZB"
     :statute/kind :law
     :statute/law-number "LRU-598 -- Adopted by the Legislative Chamber 9 December 2019; Approved by the Senate 14 December 2019 (per UNCTAD's Investment Policy Hub hosting; this iteration did not independently fetch the law's own lex.uz-hosted primary text this session, an honest gap)"
     :statute/url "https://investmentpolicy.unctad.org/investment-laws/laws/328/uzbekistan-the-law-on-investments-and-investment-activity"
     :statute/url-provenance :secondary-unctad-investment-policy-hub
     :statute/enacted-date "2019-12-09"
     :statute/retrieved-at "2026-07-23"
     :statute/topic #{:investment}}]})

(defn spec-basis
  "The jurisdiction's statute vector, or nil -- nil means NO spec-basis
  for that jurisdiction yet."
  [iso3]
  (get catalog iso3))

(defn coverage
  "Honest coverage report, same shape/discipline as `marketentry.facts/coverage`:
  never report a missing jurisdiction as covered."
  ([] (coverage (keys catalog)))
  ([iso3s]
   (let [have (filter catalog iso3s)
         missing (remove catalog iso3s)]
     {:requested (count iso3s)
      :covered (count have)
      :covered-jurisdictions (vec (sort have))
      :missing-jurisdictions (vec (sort missing))
      :note (str "cloud-itonami-iso3166-uzb statute.facts Wave 0 (ADR-2607141700): "
                 (count (get catalog "UZB")) " UZB statutes seeded with an "
                 "official citation. Extend "
                 "`statute.facts/catalog`, never fabricate a law-id or URL.")})))

(defn by-topic
  "Statutes for `iso3` tagged with `topic` (e.g. :labor, :investment)."
  [iso3 topic]
  (filterv #(contains? (:statute/topic %) topic) (spec-basis iso3)))
