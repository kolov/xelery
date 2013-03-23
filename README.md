# xelery

XML Schema Reader

Reads an XSD and produces a readable Clojure structure.

For example, this schema:

    <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
           xmlns="http://net.kolov/schema/cv"
           targetNamespace="http://net.kolov/schema/cv">
    <xs:element name="cv">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="firstName" type="xs:string"/>
                <xs:element name="secondName" type="xs:string"/>
                <xs:element name="address" type="xs:string"/>
                <xs:element name="age">
                    <xs:simpleType>
                        <xs:restriction base="xs:integer">
                            <xs:minInclusive value="0"/>
                            <xs:maxInclusive value="120"/>
                        </xs:restriction>
                    </xs:simpleType>
                </xs:element>
                <xs:element name="skills" type="skills" />
                <xs:element name="languages">
                  <xs:complexType>
                    <xs:sequence>
                      <xs:element maxOccurs="unbounded" minOccurs="0" name="language" type="xs:string"/>
                    </xs:sequence>
                  </xs:complexType>
  	     </xs:element>
            </xs:sequence>
        </xs:complexType>
    </xs:element>
    <xs:complexType name="skills">
        <xs:sequence>
            <xs:element maxOccurs="4" minOccurs="0" name="skill" type="xs:string"/>
        </xs:sequence>
     </xs:complexType>
    </xs:schema>

Is transformed to:

    {:name "cv" :elements 
      [{:name "firstName",  :multiplicty [1 1], :type "string"} 
       {:name "secondName", :multiplicty [1 1], :type "string"} 
       {:name "address"     :multiplicty [1 1], :type "string"} 
       {:name "age"         :multiplicty [1 1], :type "integer"
          :facets {:whitespace "collapse", :fractiondigits "0", :maxinclusive "120", :mininclusive "0"}} 
       {:name "skills",     :multiplicty [1 1], :elements 
          [{ :name "skill", :multiplicty [0 4], :type "string"}]} 
       {:name "languages"   :multiplicty [1 1], :elements 
             [{:name "language", :multiplicty [0 :unbounded], :type "string" }]}
      ] 
    }
    
#Usage

For a schema file x.xsd on the class path, call
    (schema-element "x.xsd")

## License

Copyright © 2013  

Distributed under the Eclipse Public License, the same as Clojure.
