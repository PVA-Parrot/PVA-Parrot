(ns pva-parrot.backend.service-test
  (:require [clojure.test :refer :all]
            [pva-parrot.backend.service :refer :all]))

(deftest backend-service
  (testing "Event Handling"
    (testing "file import"
      (is (= 3 (event-msg-handler {:id :pva-parrot/import-file}))))))
