;;; Project config

(def project
  {:version "0.1.0"
   :title   "PVA Parrot"
   :description "A GUI application for polytopic vector analysis (PVA)."
   :sources #{"src/cljs" "src/clj"}})

;;; Boot config

(set-env!
  :source-paths (:sources project)
  :resource-paths #{"resources"}
  :dependencies '[
    [org.clojure/clojurescript "0.0-3308"]
    [adzerk/boot-cljs "0.0-3308-0" :scope "test"]
    [adzerk/boot-cljs-repl "0.1.9" :scope "test"]
    [adzerk/boot-reload "0.3.1" :scope "test"]
    [adzerk/boot-test "1.0.4" :scope "test"]
    [boot-deps "0.1.6"]
    [org.martinklepsch/boot-garden "1.2.5-5" :scope "test"]
    [com.taoensso/encore "2.1.1"]
    [com.taoensso/sente  "1.5.0"]
    [compojure "1.4.0"]
    [com.taoensso/timbre "4.0.2"]
    [funcool/boot-codeina "0.1.0-SNAPSHOT" :scope "test"]
    [garden "1.2.5"]
    [http-kit "2.1.19"]
    [incanter "1.9.1-SNAPSHOT"]
    [jeluard/boot-notify "0.2.0" :scope "test"]
    [org.clojure/clojure "1.7.0"]
    [org.clojure/data.csv "0.1.2"]
    [pandeiro/boot-http "0.6.3-SNAPSHOT" :scope "test"]
    [reagent "0.5.0"]
    [ring/ring-core "1.4.0"]
    [org.clojure/test.check "0.7.0" :scope "test"]])

(require
  '[adzerk.boot-cljs :refer [cljs]]
  '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
  '[adzerk.boot-reload :refer [reload]]
  '[adzerk.boot-test :refer :all]
  '[org.martinklepsch.boot-garden :refer [garden]]
  '[funcool.boot-codeina :refer [apidoc]]
  '[jeluard.boot-notify :refer [notify]]
  '[pandeiro.boot-http :refer [serve]])

;;; Environments

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

;;; Building

(task-options!
  pom {:project     'pva-parrot
       :version     (:version project)
       :description (:description project)})

(task-options!
  apidoc {:version     (:version project)
          :title       (:title project)
          :sources     (:sources project)
          :description (:description project)
          :src-uri     "https://github.com/PVA-Parrot/PVA-Parrot/tree/master/"
          :target      "target/doc"})

(deftask build []
  (comp (notify)
        (cljs)
        (garden :styles-var 'pva-parrot.styles/base
                :vendors ["webkit"]
                :auto-prefix #{:align-items}
                :output-to "css/garden.css")))

;;; Running

(deftask serve-backend []
  (comp
    (serve :handler 'pva-parrot.backend.service/api
      :reload true
      :httpkit true
      :port 3333)))

(deftask tests
  "Run all PVA-Parrot tests"
  []
  (set-env! :source-paths #(conj % "test"))
  (test))

;;; Aliases

(deftask prod
  "Simple alias to run application in production mode No REPL or automatic
  reloading code inserted."
  []
  (comp (production)
        (serve-backend)
        (serve)
        (watch)
        (build)))

(deftask dev
  "Simple alias to run application in development mode"
  []
  (comp (serve-backend)
        (serve)
        (watch)
        (reload)
        (build)))

;; Work around for not having a native boot task to just install dependencies
(deftask deps
  "Install dependencies"
  []
  (println "Checking and installing dependencies."))
