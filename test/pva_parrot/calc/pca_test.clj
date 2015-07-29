(ns pva-parrot.calc.pca-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]
            [pva-parrot.calc.pca :refer :all]))

(def num-tests 100)

;;; Generators

(defn gen-square-matrix [n]
  (gen/vector (gen/vector gen/int n) n))

;;; Property tests

(defspec transpose-twice num-tests
  (prop/for-all [m (gen-square-matrix 5)]
                (= m (transpose (transpose m)))))

(defspec switches-indices num-tests
  (prop/for-all [m (gen-square-matrix 5)]
                (= ((m 1) 3)
                   (((transpose m) 3) 1))))

;;; Conventional tests

(deftest calc-pca
  (testing "Basic linear algebra functions"

    (testing "transpose"
      (is transpose "transpose function is defined"))))
