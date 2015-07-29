(ns pva-parrot.calc.pca
  (:require [incanter.core :refer :all]
            [incanter.stats :refer :all]
            [clojure.core.matrix :as m]))

(m/set-current-implementation :vectorz)

(defn transpose
  "Transpose the matrix mat."
  [mat]
  (apply mapv vector mat))

(defn vals-to-lists
  ;; TODO: Add docstring
  ""
  [hash-map]
  (zipmap (keys hash-map) (map to-vect (vals hash-map))))

(defn map-columns
  "Map a function to all matrix columns, applying the function to all values in
  a column."
  [f m]
  (map #(apply f %) m))

(defn mins
  "Returns a vector with the minimal values of every column in matrix m."
  [m]
  (map-columns min m))

(defn maxs
  "Returns a vector with the maximum values of every column in matrix m."
  [m]
  (map-columns max m))

(defn means
  "Returns a vector with the mean values of every column in matrix m."
  [m]
  (map mean m))

(defn sds
  "Returns a vector with the standard deviations of every column in matrix m."
  [m]
  (map sd m))

(defn summarize [labels m]
  {:headings ["" "Mean" "St.Dev." "Min." "Max."]
   :body (-> [labels
              (means m)
              (sds m)
              (mins m)
              (maxs m)]
             (transpose))})

(defn normalize-compos
  "Normalize compositions to the range of its min to max value."
  [compos]
  (let [transposed (transpose compos)
        mins       (mins transposed)
        maxs       (maxs transposed)
        spread     (minus maxs mins)]
    (-> compos
        (minus mins)
        (div spread))))

(defn calc-eigenvalues
  "Calculates eigenvalues from the given pca-data as squares of the standard
  deviations."
  [pca-data]
  (map #(* % %) (:std-dev pca-data)))

(defn add-eigenvalues
  "Add eigenvalues to the given pca-data map."
  [pca-data]
  (assoc pca-data :eigen-values (calc-eigenvalues pca-data)))

(defn components
  "Calulates principal components and eigen values.
   Returns map of results with the actual results converted to list of lists."
  [data]
  (-> data
      (normalize-compos)
      (principal-components)
      ;; (decomp-eigenvalue)
      (add-eigenvalues)
      (vals-to-lists)))
