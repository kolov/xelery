(ns xelery.core-test
  (:use clojure.test
        xelery.core))

(deftest parse
  (testing "parsing sample schema"
    (is (= '(:elements :type :name)
             (keys (parse-resource "schema1.xsd")) ))))
