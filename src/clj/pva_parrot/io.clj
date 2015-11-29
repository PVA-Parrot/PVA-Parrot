(ns pva-parrot.io
  (:require [clojure.data.csv :as csv]))

(defn- extract-body
  "Drop headings and sample name column from csv data."
  [data]
  (map rest (rest data)))

(defn- parse-numbers
  "Parse CSV strings into numbers."
  [csv-body]
  (map #(map (fn [s] (Double/parseDouble s)) %) csv-body))

(def csv-to-matrix (comp parse-numbers extract-body))

(def read-csv-file (comp csv/read-csv slurp))

(defn process-csv [csv]
  (let [headings   (first csv)
        body       (rest csv)
        variable-names (rest headings)]
    {:headings headings
     :body body
     :num-samples (count body)
     :num-variables (count variable-names)
     :matrix (csv-to-matrix csv)}))
