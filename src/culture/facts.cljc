(ns culture.facts
  "Country-level regional-culture catalog for Uzbekistan (UZB) -- national
  dishes, protected products, beverages, crafts, festivals and heritage
  sites, per ADR-2607171400 addendum 2 (cloud-itonami-municipality-
  culture-catalog Wave 1, in com-junkawasaki/root). Sibling namespace to
  `marketentry.facts` / `statute.facts` (ADR-2607141700); city-level
  counterparts live in the cloud-itonami-municipality-* repos.

  Catalog is keyed by UPPERCASE ISO3 (mirrors `statute.facts`); entries
  carry no :culture/municipality (that attribute is city-level only).

  Every entry cites a source URL that was actually fetched and read on
  :culture/retrieved-at -- never fabricated. Summaries state only what the
  cited source confirms. An item not in this table has NO spec-basis, full
  stop; extend `catalog`, do not invent an id/url.")

(def catalog
  "iso3 -> vector of culture entries."
  {"UZB"
   [{:culture/id "uzb.dish.plov"
     :culture/name "Plov"
     :culture/name-local "Palov"
     :culture/country "UZB"
     :culture/kind :dish
     :culture/summary "Traditional Uzbek rice dish (also called palov or osh) made with fatty lamb, julienned carrots, onions, garlic, rice and cumin; UNESCO inscribed the dish and its culture on the Representative List of the Intangible Cultural Heritage of Humanity in 2016."
     :culture/url "https://en.wikipedia.org/wiki/Uzbek_plov"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "uzb.dish.laghman"
     :culture/name "Laghman"
     :culture/country "UZB"
     :culture/kind :dish
     :culture/summary "Noodle dish of meat, vegetables and pulled noodles, of Uyghur origin, also common in Uzbekistan, Tajikistan, Turkmenistan, Russia and northeastern Afghanistan."
     :culture/url "https://en.wikipedia.org/wiki/Laghman_(food)"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "uzb.dish.samsa"
     :culture/name "Samsa"
     :culture/name-local "Somsa"
     :culture/country "UZB"
     :culture/kind :dish
     :culture/summary "Savoury baked pastry stuffed with meat or vegetables, eaten as street food across Central Asia including Uzbekistan (referred to there as Uzbek somsas)."
     :culture/url "https://en.wikipedia.org/wiki/Samsa_(food)"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "uzb.dish.non"
     :culture/name "Tandyr nan"
     :culture/name-local "Non"
     :culture/country "UZB"
     :culture/kind :dish
     :culture/summary "Central Asian flatbread baked stuck to the wall of a vertical clay tandyr oven; in Uzbek culture non (bread) carries great ceremonial importance."
     :culture/url "https://en.wikipedia.org/wiki/Tandyr_nan"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "uzb.craft.suzani"
     :culture/name "Suzani"
     :culture/country "UZB"
     :culture/kind :craft
     :culture/summary "Embroidered decorative tribal textile made in Uzbekistan (notably Bukhara, Samarkand, Tashkent, Piskent and Nurata), Tajikistan, Afghanistan and other Central Asian countries."
     :culture/url "https://en.wikipedia.org/wiki/Suzani_(textile)"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "uzb.festival.navruz"
     :culture/name "Nowruz"
     :culture/name-local "Navruz"
     :culture/country "UZB"
     :culture/kind :festival
     :culture/summary "Spring new-year festival tied to the Northern Hemisphere vernal equinox; Uzbekistan is one of five Central Asian countries (with Tajikistan, Kyrgyzstan, Turkmenistan and Kazakhstan) that celebrate Nowruz/Navruz as a public holiday."
     :culture/url "https://en.wikipedia.org/wiki/Nowruz"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "uzb.heritage.itchan-kala"
     :culture/name "Itchan Kala"
     :culture/country "UZB"
     :culture/kind :heritage
     :culture/summary "Walled inner town of the city of Khiva, Uzbekistan, protected as a UNESCO World Heritage Site since 1990."
     :culture/url "https://en.wikipedia.org/wiki/Itchan_Kala"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "uzb.heritage.bukhara"
     :culture/name "Historic Centre of Bukhara"
     :culture/country "UZB"
     :culture/kind :heritage
     :culture/summary "Historic center of Bukhara, Uzbekistan, containing numerous mosques and madrasas; listed as a UNESCO World Heritage Site in 1993."
     :culture/url "https://en.wikipedia.org/wiki/Historic_Centre_of_Bukhara"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}]})

(defn spec-basis [iso3] (get catalog iso3))

(defn coverage
  ([] (coverage (keys catalog)))
  ([iso3s]
   (let [have (filter catalog iso3s)
         missing (remove catalog iso3s)]
     {:requested (count iso3s)
      :covered (count have)
      :covered-jurisdictions (vec (sort have))
      :missing-jurisdictions (vec (sort missing))
      :note (str "cloud-itonami-iso3166-uzb culture catalog "
                 "(ADR-2607171400 addendum 2, Wave 1): " (count (get catalog "UZB"))
                 " UZB entries, each with a fetched-and-read citation. "
                 "Extend `culture.facts/catalog`, never fabricate an id/url.")})))

(defn by-kind [iso3 kind]
  (filterv #(= (:culture/kind %) kind) (spec-basis iso3)))
