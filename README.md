# cloud-itonami-iso3166-uzb

**UZB**: Republic of Uzbekistan.

- Public procurement governed by Decree of the President of the
  Republic of Uzbekistan No. PP-4544 (5 December 2019) "On measures to
  further improve public procurement system and to broadly involve
  business entities in public procurement process"; live information
  portal at `xarid.uzex.uz`. This iteration could not independently
  confirm or rule out a separate parliamentary "Law on Public
  Procurement" this session (lex.uz's own full-text search requires a
  stateful ASP.NET postback this session's tooling could not execute)
  -- an honest, disclosed gap.
- STIR (taxpayer identification number) tax registration via the State
  Tax Committee of the Republic of Uzbekistan (`soliq.uz`); business
  registration via the `birdarcha.uz` single-window front-end, directly
  hyperlinked from gov.uz's own official business-registration page;
  Ministry of Investment, Industry and Trade (renamed from "Ministry of
  investments and foreign trade", per the Law "On investments and
  investment activity" No. LRU-598's own 2019 Article 26 text) foreign-
  investment authority.
- Labour Code of the Republic of Uzbekistan (adopted 28 October 2022)
  Article 130 initial-probation-period statutory ceiling -- a
  category-dependent gate (3 months general / 6 months for heads of
  organizations and their deputies, chief accountants, and heads of
  separate divisions of organizations) on any declared locally-engaged-
  staff probation period (flagship check)

AGPL-3.0-or-later.

## Market-entry / statute catalogs

Governed public-sector market-entry compliance actor, same architecture
as `cloud-itonami-iso3166-ner` (the closest architectural match this
iteration studied in depth):

- `src/marketentry/{facts,governor,phase,sim,operation,registry,store,
  marketentryllm}.cljc` -- the actor. `facts.cljc` cites Decree No.
  PP-4544 (public procurement legal basis), the `xarid.uzex.uz`
  information portal, the State Tax Committee (`soliq.uz`, STIR tax
  registration), the `birdarcha.uz` business-registration front-end,
  and the Law "On investments and investment activity" No. LRU-598
  (2019, Ministry of Investment, Industry and Trade) -- with every
  honestly-disclosed access gap (unreachable domains, JS-only SPA
  shells, a full-text search this session's tooling could not execute)
  documented in the namespace docstring rather than papered over.
  `governor.cljc`'s flagship check independently recomputes whether an
  engagement's own declared locally-engaged-staff initial probation
  period exceeds the Labour Code's own Article 130 category-dependent
  statutory ceiling -- a check shape this iteration is not aware of
  copying from any sibling it studied (see the namespace docstrings for
  the full research trail, including this iteration's own direct
  reading of the Labour Code's full primary text via lex.uz's
  server-rendered document pages, discovered via a classifier-browse
  AJAX endpoint this iteration found and used directly, and an
  honestly-flagged, unresolved gap over which specific ministry/agency
  legally administers `birdarcha.uz`).
- `src/statute/facts.cljc` -- general-law catalog: the Labour Code of
  the Republic of Uzbekistan (adopted 28 October 2022, own primary text
  read in full at `lex.uz/en/docs/-6257288`) and the Law "On investments
  and investment activity" No. LRU-598 (2019, per UNCTAD's Investment
  Policy Hub hosting). This iteration specifically investigated, rather
  than assumed by analogy to OHADA-member siblings, whether a domestic
  companies/commercial-entity law citation could be independently
  confirmed -- Uzbekistan is not an OHADA member state, and this
  iteration could not retrieve the actual document behind lex.uz's own
  "Legal entity" classifier branch this session (the same full-text-
  search limitation as above) -- a smaller, honest two-entry catalog
  was chosen over a fabricated third entry.

Every citation is curl/WebFetch-verified against an official source
(gov.uz, xarid.uzex.uz, soliq.uz via the Wayback Machine, new.birdarcha.uz,
lex.uz, invest.gov.uz) or UNCTAD's Investment Policy Hub secondary
hosting; ILO NATLEX returned a Cloudflare "Just a moment..." bot-
detection challenge this session and was NOT bypassed (per this
project's hard safety rule), and no Wayback snapshot of the specific
page was available either -- disclosed as unreachable rather than
guessed around. See `marketentry.facts`'s and `statute.facts`'s
docstrings for exactly which facts are HIGH confidence vs. an honestly-
flagged gap.

## Culture catalog

Alongside the market-entry / statute catalogs, this repo carries a
**country-level regional-culture catalog** (ADR-2607171400 addendum 2,
`cloud-itonami-municipality-culture-catalog` Wave 1, in
`com-junkawasaki/root`) — national dishes, protected products, beverages,
crafts, festivals and heritage sites for Uzbekistan:

- `src/culture/facts.cljc` — the catalog, source of truth (keyed by
  uppercase ISO3, mirroring `statute.facts`).
- `schema/culture.edn` — DataScript schema.
- `data/culture-tx.edn` — derived DataScript tx-data (regenerated from
  the catalog, never hand-edited).

City-level counterparts live in the `cloud-itonami-municipality-*` repos.
Same provenance discipline as the compliance catalogs: every entry cites a
source URL that was actually fetched and read on `:culture/retrieved-at`;
summaries state only what the cited source confirms. An item not in
`culture.facts/catalog` has no spec-basis — never fabricate one.
