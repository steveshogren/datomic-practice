(use 'datomic.samples.repl)
(easy!)

;; inspired by http://www.lshift.net/blog/2010/08/21/some-relational-algebra-with-datatypes-in-clojure-12
(defrecord Supplier [number name status city])
(defrecord Part [number name colour weight city])
(defrecord Shipment [supplier part quantity])

;; sample data
(def suppliers
  #{(Supplier. "S1" "Smith" 20 "London")
    (Supplier. "S2" "Jones" 10 "Paris")
    (Supplier. "S3" "Blake" 30 "Paris")})
(def parts
  #{(Part. "P1" "Nut" "Red" 12.0 "London")
    (Part. "P2" "Bolt" "Green" 17.0 "Paris")
    (Part. "P3" "Screw" "Blue" 17.0 "Oslo")})
(def shipments
  #{(Shipment. "S1" "P1" 300)
    (Shipment. "S2" "P2" 200)
    (Shipment. "S2" "P3" 400)})

(doc maps->rel)
(maps->rel suppliers [:city :name])

(defpp
  query-on-defrecord-results
  (d/q '[:find ?name
         :where ["Paris" ?name]]
       (maps->rel suppliers [:city :name])))

(defpp
  join-on-defrecord-results
  (d/q '[:find ?name
         :in $suppliers $shipments
         :where
         [$suppliers ?supplier ?name "Paris"]
         [$shipments ?supplier]]
       (maps->rel suppliers [:number :name :city])
       (maps->rel shipments [:supplier])))

;; Appears to let joins happen over any disparate data
;; using given keys, e.g. the ?part number here, we
;; join by referencing it in a traditional join table
;; and "calling out" the value in each set using the same
;; named value.
;; Also, each vec of the :where here below uses a $x apparently
;; to match to a given set of data
(defpp
  join-on-defrecord-order
  (d/q '[:find ?name
         :in $suppliers $parts $shipments 
         :where
         [$suppliers ?supplier "Paris"]
         [$parts ?part ?name]
         [$shipments ?supplier ?part]]
       (maps->rel suppliers [:number :city])
       (maps->rel parts [:number :name])
       (maps->rel shipments [:supplier :part])))
