(ns xelery.core-test
  (:use clojure.test
        xelery.core))

(deftest parse-not-null
  (testing "Parsing returns non null result")
  (is (not= nil (parse-resource "schema1.xsd"))))

(deftest parse
  (testing "parsing sample schema"
    (is (= '(:elements :type :name)
             (keys (parse-resource "schema1.xsd")) ))))
