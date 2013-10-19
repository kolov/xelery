(ns xelery.core
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.pprint :refer [pprint]])
  )

(defn log [& args] (apply println args))

; Some constants
(def SIMPLE_TYPE com.sun.org.apache.xerces.internal.xs.XSTypeDefinition/SIMPLE_TYPE)
(def COMPLEX_TYPE com.sun.org.apache.xerces.internal.xs.XSTypeDefinition/COMPLEX_TYPE)
(def ELEMENT_DECLARATION com.sun.org.apache.xerces.internal.xs.XSConstants/ELEMENT_DECLARATION)

(def ns-schema "http://www.w3.org/2001/XMLSchema")
(def FACETS {0 :none, 1 :length , 2 :minlength,
             4 :maxlengthH ,8 :pattern, 16 :whitespace, 32 :maxinclusive,
             64 :maxexclusive ,128 :minexclusive, 256 :mininclusive,
             512 :totaldigits 1024 :fractiondigits,2048 :enumeration})


(defn lsinput [^java.lang.String data]
  (reify org.w3c.dom.ls.LSInput
    (getBaseURI [this] (throw (Exception. "getBaseURI")))
    (getByteStream [this]  (throw (Exception. "getByteStream")))
    (getCertifiedText [this] false)
    (getCharacterStream [this]  (throw (Exception. "getCharacterStream")))
    (getEncoding [this]  (throw (Exception. "getEncoding")))
    (getPublicId [this]  (throw (Exception. "getPublicId")))
    (getStringData [this] data)
    (getSystemId [this]  (throw (Exception. "getSystemId")))
    (setBaseURI [this v] (throw (Exception. "unexpected: setBaseURI")))
    (setByteStream [this v] (throw (Exception. "setByteStream")))
    (setCertifiedText [this v] (throw (Exception. "setCertifiedText")))
    (setCharacterStream [this v]  (throw (Exception. "setCharacterStream")))
    (setEncoding [this v]  (throw (Exception. "setEncoding")))
    (setPublicId [this v]  (throw (Exception. "setPublicId")))
    (setStringData [this v]  (throw (Exception. "setStringData")))
    (setSystemId [this v]  (throw (Exception. "setSystemId")))
    ))

(defn resource-location [f]
  "gets the locaton of a resource on the classpath"
  (if-let [loc (clojure.java.io/resource f)] (.getFile loc)))

(defn loader []
  "Reads schema from XSD esource on the classpath"
  (System/setProperty org.w3c.dom.bootstrap.DOMImplementationRegistry/PROPERTY
    "com.sun.org.apache.xerces.internal.dom.DOMXSImplementationSourceImpl")
  (let [registry (org.w3c.dom.bootstrap.DOMImplementationRegistry/newInstance)
        impl (.getDOMImplementation registry "XS-Loader")
        result (.createXSLoader impl nil)]
   
    (.setParameter result
      "http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager"
      (com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager.))
  result))

(defmulti read-schema class)
(defmethod read-schema java.io.File [f] (.loadURI (loader) (.getPath f)))
(defmethod read-schema String [s] (.load (loader) (lsinput s)))

           ` \


(defn components
  "Makes a sequence of components in a schema"
  ([sc n] (if-let [c (.getComponents sc n)] (for [i (range (.getLength c))] (.item c i))))
  ([sc] (components sc ELEMENT_DECLARATION)))


(declare read-element)
(declare model-group-elements)

(defn- make-all-facets [fl]
  "make facets map from XSObjectList containing facets"
  (apply array-map (flatten
                     (for [i (range (.getLength fl))]
                       (let [f (.item fl i)] [(-> f .getFacetKind FACETS) (.getLexicalFacetValue f)])))))


(defn- make-facets [fl]
  (let [facets (make-all-facets fl)] (-> facets #(if (= (:whitespace %) "preserve") (dissoc % :whitespace ) %))))

(defmulti type-def (fn [_ td] (class td)))
(defmethod type-def com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition
  [m td]
  (if-let [particle (.getParticle td)]
    (let [model-group (.getTerm particle)]
      (assoc m :type :complex :elements (model-group-elements model-group)))
    ))


(defmethod type-def com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
  [m td] (let [isBase (= ns-schema (.getNamespace td))]
           (if isBase
             (assoc m :type (keyword (.getTypeName td)))
             (merge m {:type (-> td .getBaseType .getTypeName keyword) :typeName (-> td .getName)}
               (if-let [facets (-> td .getFacets make-all-facets)] {:facets facets})
               (if-let [pattern (.getLexicalPattern td)]
                 (let [len (.getLength pattern)] (if (> len 0) {:pattern (.item pattern 0)})))
               (if-let [enum (.getActualEnumeration td)]
                 (let [len (.getLength enum)] (if (> len 0) {:enumvals (set (for [i (range len)] (.item enum i)))
                                                             :type :enum})))
               ))))

(defn- make-multiplicity [particleDecl]
  [(.getMinOccurs particleDecl)
   (if (.getMaxOccursUnbounded particleDecl) :unbounded (.getMaxOccurs particleDecl))])

(defn- model-group-elements [mgi]
  (let [fParticles (.getParticles mgi)
        n (.getLength fParticles)]
    (vec (for [i (range n)]
           (let [particleDecl (.item fParticles i)
                 fValue (.fValue particleDecl)]
             (-> fValue read-element (assoc :multiplicity (make-multiplicity particleDecl))))))))

(defn read-element [eld]
  (let [m {:name (.getName eld)}]
    (type-def m (.getTypeDefinition eld))))

(defn schema-element [x]
  "Returns element definition of the root element of the schema file"
  (-> x read-schema components first read-element))

(defn parse-resource[r] (schema-element (java.io.File. (resource-location r))))
(defn print-sample [] (clojure.pprint/pprint (parse-resource "schema1.xsd")))



