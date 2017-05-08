(ns xelery.core-test
  (:use clojure.test
        xelery.core))

(deftest parse-file-not-null
  (testing "Parsing file returns non null result")
  (is (not= nil (parse-resource "schema1.xsd"))))

(deftest parse-string-not-null
  (testing "Parsing string returns non null result")
  (is (not= nil (schema-element (slurp (java.io.File. (resource-location "schema1.xsd")))))))

(deftest parse
  (testing "parsing sample schema extract keys"
    (is (= #{:elements :type :name}
           (set (keys (parse-resource "schema1.xsd")))))))

(deftest parse
  (testing "parsing sample schema correctly"
    (is (= (parse-resource "schema1.xsd")

        { :name "cv",
         :type :complex,
         :elements [{:name "firstName", :type :string, :typeName "nameType", :documentation "A Name Type",
                     :facets {:whitespace "preserve"}, :pattern "[a-zA-z ]{1,50}", :multiplicity [1 1]}
                    {:name "secondName", :type :string, :typeName "nameType", :documentation "A Name Type",
                     :facets {:whitespace "preserve"}, :pattern "[a-zA-z ]{1,50}", :multiplicity [1 1]}
                    {:name "sex", :type :enum, :typeName "sexType", :facets {:whitespace "preserve"}, :enumvals #{"M" "F"}, :multiplicity [1 1]}
                    {:name "freeText", :type :string, :multiplicity [0 3]}
                    {:name "positions", :type :complex, :elements
                           [{:name "position", :type :complex, :elements
                                   [{:name "startDate", :type :date, :multiplicity [1 1]}
                                    {:name "endDate", :type :date, :multiplicity [1 1]}
                                    {:name "customer", :type :string, :multiplicity [1 1]}
                                    {:name "description", :type :string, :multiplicity [1 1]}
                                    {:name "techniques", :type :complex, :elements
                                           [{:name "technique", :type :string, :multiplicity [1 :unbounded]}],
                                     :multiplicity [1 1]}], :multiplicity [1 :unbounded]}], :multiplicity [1 1]}]}
        ))))
