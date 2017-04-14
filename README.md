# xelery

Clojure XML Schema Reader

Reads an XSD and produces a readable Clojure structure.

For example, this schema:

        <?xml version="1.0" encoding="ISO-8859-1" ?>
        <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
        targetNamespace="http://com.akolov/schema/cv/1.0"
        xmlns:cv="http://com.akolov/schema/cv/1.0" elementFormDefault="qualified">
        <xs:element name="cv">
            <xs:complexType>
                <xs:sequence>
                    <xs:element name="firstName" type="cv:nameType"/>
                    <xs:element name="secondName" type="cv:nameType"/>
                    <xs:element name="sex" type="cv:sexType"/>
                    <xs:element name="freeText" type="xs:string" minOccurs="0" maxOccurs="3"/>
                    <xs:element name="positions">
                        <xs:complexType>
                            <xs:sequence>
                                <xs:element name="position" type="cv:position" maxOccurs="unbounded"/>
                            </xs:sequence>
                        </xs:complexType>
                    </xs:element>
                </xs:sequence>
            </xs:complexType>
        </xs:element>
        <xs:complexType name="position">
            <xs:sequence>
                <xs:element name="startDate" type="xs:date"/>
                <xs:element name="endDate" type="xs:date"/>
                <xs:element name="customer" type="xs:string"/>
                <xs:element name="description" type="xs:string"/>
                <xs:element name="techniques">
                    <xs:complexType>
                        <xs:sequence>
                            <xs:element name="technique" type="xs:string" maxOccurs="unbounded"/>
                        </xs:sequence>
                    </xs:complexType>
                </xs:element>
            </xs:sequence>
        </xs:complexType>
        <xs:simpleType name="nameType" >
            <xs:restriction base="xs:string">
                <xs:pattern value="[a-zA-z ]{1,50}"/>
            </xs:restriction>
        </xs:simpleType>
        <xs:simpleType name="sexType">
          <xs:restriction base="xs:string">
            <xs:enumeration value="M"/>
            <xs:enumeration value="F"/>
          </xs:restriction>
        </xs:simpleType>
    </xs:schema>

Is transformed to:

    {:elements
     [{:multiplicty [1 1],
       :pattern "[a-zA-z ]{1,50}",
       :facets {:whitespace "preserve"},
       :type :string,
       :name "firstName"}
      {:multiplicty [1 1],
       :pattern "[a-zA-z ]{1,50}",
       :facets {:whitespace "preserve"},
       :type :string,
       :name "secondName"}
      {:multiplicty [1 1],
       :enum #{"F" "M"},
       :facets {:whitespace "preserve"},
       :type :enum,
       :name "sex"}
      {:multiplicty [0 3], :type :string, :name "freeText"}
      {:multiplicty [1 1],
       :elements
       [{:multiplicty [1 :unbounded],
         :elements
         [{:multiplicty [1 1], :type :date, :name "startDate"}
          {:multiplicty [1 1], :type :date, :name "endDate"}
          {:multiplicty [1 1], :type :string, :name "customer"}
          {:multiplicty [1 1], :type :string, :name "description"}
          {:multiplicty [1 1],
           :elements
           [{:multiplicty [1 :unbounded],
             :type :string,
             :name "technique"}],
           :name "techniques"}],
         :name "position"}],
       :name "positions"}],
     :name "cv"}
    
# Usage

In Leiningen, add

     [com.akolov/xelery "0.4.6"]
    
For a schema file:

     (schema-element (File. "x.xsd"))
     
For a string containing schema
     (def s "<s:schema.....")
     (schema-element s)

## License

Copyright © 2013  

Distributed under the Eclipse Public License, the same as Clojure.
