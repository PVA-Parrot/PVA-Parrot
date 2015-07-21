
(ns pva-parrot.backend.service-test
  (:require [clojure.test :refer :all]
            [pva-parrot.backend.service :refer :all]))

(deftest backend-service
  (testing "Event Handling"

    (testing "setup"
      (is chsk-send! "send function is defined"))

    (testing "file import"
      (let [message-id :data-import/csv-sent]

        (is (= nil (event-msg-handler {:id message-id}))
          "without given data it returns nil")))))
