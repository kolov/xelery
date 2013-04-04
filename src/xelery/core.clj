(ns xelery.core
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip] )
(:use [xelery.lsinput]))

; Some constants
(def SIMPLE_TYPE com.sun.org.apache.xerces.internal.xs.XSTypeDefinition/SIMPLE_TYPE)
(def COMPLEX_TYPE com.sun.org.apache.xerces.internal.xs.XSTypeDefinition/COMPLEX_TYPE)
(def ELEMENT_DECLARATION
    com.sun.org.apache.xerces.internal.xs.XSConstants/ELEMENT_DECLARATION)
(def ns-schema "http://www.w3.org/2001/XMLSchema")
(def FACETS { 0 :none, 1 :length , 2 :minlength,
4 :maxlengthH ,8 :pattern, 16 :whitespace, 32 :maxinclusive,
64 :maxexclusive ,128 :minexclusive, 256 :mininclusive,
512 :totaldigits 1024 :fractiondigits,2048 :enumeration})


(defn lsinput[ ^java.lang.String data]
  (reify org.w3c.dom.ls.LSInput
  (getBaseURI [this] nil)
  (getByteStream [this]   nil)
  (getCertifiedText [this]   false)
  (getCharacterStream [this]   nil)
  (getEncoding [this]   nil)
  (getPublicId [this]   nil)
  (getStringData [this]   data)
  (getSystemId [this]   nil)
  (setBaseURI [this baseURI])
  (setByteStream[this baseURI])
  (setCertifiedText[this baseURI])
  (setCharacterStream[this baseURI])
  (setEncoding[this baseURI])
  (setPublicId[this baseURI])
  (setStringData[this baseURI])
  (setSystemId[this baseURI])
   ))

(defn resource-location[f]
  "gets the locaton of a resource on the classpath"
  (-> (clojure.java.io/resource f) .getFile ))

(defn loader[]
  "Reads schema from XSD esource on the classpath"
  (System/setProperty org.w3c.dom.bootstrap.DOMImplementationRegistry/PROPERTY
      "com.sun.org.apache.xerces.internal.dom.DOMXSImplementationSourceImpl")
  (let [registry (org.w3c.dom.bootstrap.DOMImplementationRegistry/newInstance)
        impl (.getDOMImplementation registry "XS-Loader")]
       (.createXSLoader impl nil)))

(defmulti read-schema class)
(defmethod read-schema java.io.File [r]    (.loadURI (loader) r))
(defmethod read-schema String [s]          (.load (loader) (lsinput s)))


(defn components
  "Makes a sequence of components in a schema"
  ([sc n] (let[c (.getComponents sc n)] (for[i (range (.getLength c))] (.item c i))))
    ([sc] (components sc ELEMENT_DECLARATION)))


(declare read-element)
(declare model-group-elements)

(defn- make-facets [fl]
  "make facets map from XSObjectList containing facets"
  (apply array-map (flatten
  (for[i (range (.getLength fl))]
     (let[ f (.item fl i)] [ (-> f .getFacetKind FACETS) (.getLexicalFacetValue f)])))))

(defmulti type-def (fn[_ td] (class td)))
(defmethod type-def com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition
  [m td] (let[model-group (-> td .getParticle .getTerm)] 
     (assoc m :elements (model-group-elements model-group ))))
(defmethod type-def com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition
  [m td] (let [isBase  (= ns-schema (.getNamespace td))]
         (if isBase (assoc m :type (.getTypeName td))
                        (assoc m :type (-> td .getBaseType .getTypeName ) 
                                 :facets (-> td .getFacets make-facets )))))
 
(defn- make-multiplicity [particleDecl]
  [ (.getMinOccurs particleDecl) 
     (if (.getMaxOccursUnbounded particleDecl)  :unbounded (.getMaxOccurs particleDecl)) ])

(defn- model-group-elements[mgi]
  (let [fParticles (.getParticles mgi)
        n (.getLength fParticles)]
    (vec (for [i (range n)] 
          (let[particleDecl (.item fParticles i)
               fValue (.fValue particleDecl)] 
      (-> fValue read-element (assoc :multiplicty (make-multiplicity particleDecl))))))))

(defn read-element [eld]
 (let[ m {:name (.getName eld)}]
  (type-def m (.getTypeDefinition eld))))

(defn schema-element[x]
  "Returns element definition of the root element of the schema file"
  (-> x read-schema components first read-element))