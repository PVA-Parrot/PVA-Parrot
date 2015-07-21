(ns pva-parrot.io
  (:require [clojure.data.csv :refer [read-csv]]))

(defn- extract-body
  "Drop headings and sample name column from csv data."
  [data]
  (map rest (rest data)))

(defn- parse-numbers
  "Parse CSV strings into numbers."
  [csv-body]
  (map #(map (fn [s] (Double/parseDouble s)) %) csv-body))

(defn csv-to-matrix [csv]
  (-> (read-csv csv)
    (extract-body)
    (parse-numbers)))
