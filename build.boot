
(set-env!
  :source-paths #{"src/cljs" "src/clj"}
  :resource-paths #{"resources"}
  :dependencies '[
    [org.clojure/clojurescript "0.0-2814"]
    [adzerk/boot-cljs      "0.0-2814-1"      :scope "test"]
    [adzerk/boot-cljs-repl "0.1.9"           :scope "test"]
    [adzerk/boot-reload    "0.2.4"           :scope "test"]
    [pandeiro/boot-http    "0.6.3-SNAPSHOT"  :scope "test"]
    [jeluard/boot-notify   "0.1.1"           :scope "test"]
    [boot-garden           "1.2.5-2"         :scope "test"]
    [adzerk/boot-test      "1.0.4"           :scope "test"]
    [reagent               "0.5.0-alpha3"]
    [com.taoensso/encore   "1.21.0"]
    [com.taoensso/sente    "1.3.0"]
    [garden                "1.2.5"]
    [ring/ring-core       "1.3.2"]
    [http-kit             "2.1.19"]
    [compojure            "1.3.1"]
    [com.taoensso/timbre  "3.4.0"]
    [org.clojure/data.csv "0.1.2"]
    [incanter "1.9.1-SNAPSHOT"]
  ])

(require
  '[adzerk.boot-cljs      :refer [cljs]]
  '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
  '[adzerk.boot-reload    :refer [reload]]
  '[adzerk.boot-test      :refer :all]
  '[pandeiro.boot-http    :refer [serve]]
  '[boot-garden.core      :refer [garden]]
  '[jeluard.boot-notify   :refer [notify]])

(deftask build []
  (comp
    (notify)
    (cljs)
    (garden :styles-var 'pva-parrot.styles/base
      :vendors ["webkit"]
      :auto-prefix #{:align-items}
      :output-to "css/garden.css")))

(deftask dev-run []
  (comp (serve)
    (watch)
    (reload)
    (build)))

(deftask production []
  (task-options! cljs {:optimizations :advanced
                       :compiler-options {:closure-defines {:goog.DEBUG false}}}
    garden {:pretty-print false})
  identity)

(deftask development []
  (task-options! cljs {:optimizations :none
                       :unified-mode true
                       :source-map true}
    reload {:on-jsload 'pva-parrot.app/init})
  identity)

(deftask prod
  "Simple alias to run application in production mode
       No REPL or automatic reloading code inserted."
  []
  (comp (production)
    (serve)
    (watch)
    (build)))

(deftask dev
  "Simple alias to run application in development mode"
  []
  (comp (development)
    (dev-run)))
(deftask run-tests
  "Run all PVA-Parrot tests"
  []
  (set-env! :source-paths #(conj % "test"))
  (test))

(deftask serve-backend []
  (comp
    (serve :handler 'pva-parrot.backend.service/api
      :reload true
      :httpkit true
      :port 3333)))

(task-options!
  pom {:project 'pva-parrot
       :version "0.1.0"
       :description "A GUI application for polytopic vector analysis (PVA)."})
