(ns xelery.core
  (:require
    [clojure.pprint :refer [pprint]])
  (:import (org.w3c.dom DOMErrorHandler DOMError)
           (java.io File StringReader)))

(defn log [& args] (apply println args))

; Some constants
(def SIMPLE_TYPE com.sun.org.apache.xerces.internal.xs.XSTypeDefinition/SIMPLE_TYPE)
(def COMPLEX_TYPE com.sun.org.apache.xerces.internal.xs.XSTypeDefinition/COMPLEX_TYPE)
(def ELEMENT_DECLARATION com.sun.org.apache.xerces.internal.xs.XSConstants/ELEMENT_DECLARATION)


(def ns-schema "http://www.w3.org/2001/XMLSchema")
(def FACETS {0   :none, 1 :length, 2 :minlength,
             4   :maxlengthH, 8 :pattern, 16 :whitespace, 32 :maxinclusive,
             64  :maxexclusive, 128 :minexclusive, 256 :mininclusive,
             512 :totaldigits 1024 :fractiondigits, 2048 :enumeration})


(defn lsinput [^java.lang.String data]
  (reify org.w3c.dom.ls.LSInput
    (getBaseURI [this] (do (log "getBaseURI") nil))
    (getByteStream [this] (do (log "getByteStream") nil))
    (getCertifiedText [this] false)
    (getCharacterStream [this] (do (log "getCharacterStream") (StringReader. data)))
    (getEncoding [this] (do (log "getEncoding") nil))
    (getPublicId [this] (do (log "getPublicId") nil))
    (getStringData [this] (do (log "getEncoding") nil))
    (getSystemId [this] (do (log "getSystemId") nil))
    (setBaseURI [this v] (do (log "unexpected: setBaseURI") nil))
    (setByteStream [this v] (do (log "setByteStream") nil))
    (setCertifiedText [this v] (do (log "setCertifiedText") nil))
    (setCharacterStream [this v] (do (log "setCharacterStream") nil))
    (setEncoding [this v] (do (log "setEncoding") nil))
    (setPublicId [this v] (do (log "setPublicId") nil))
    (setStringData [this v] (do (log "setStringData") nil))
    (setSystemId [this v] (do (log "setSystemId") nil))
    ))

(def schema-error-handler
  (reify DOMErrorHandler
    (^boolean handleError [_ ^DOMError err]
      (throw (Exception. (.getMessage err)))))
  )

(defn resource-location [f]
  "gets the locaton of a resource on the classpath"
  (if-let [loc (clojure.java.io/resource f)] (.getFile loc)))

(defn loader []
  "Reads schema from XSD resource on the classpath"
  (System/setProperty org.w3c.dom.bootstrap.DOMImplementationRegistry/PROPERTY
    "com.sun.org.apache.xerces.internal.dom.DOMXSImplementationSourceImpl")
  (let [registry (org.w3c.dom.bootstrap.DOMImplementationRegistry/newInstance)
        impl     (.getDOMImplementation registry "XS-Loader")
        result   (.createXSLoader impl nil)]

    (try
      (.setParameter result
        "http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager"
        (.newInstance (Class/forName "com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager")))
      (catch ClassNotFoundException e (println "Class com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager not found")))

    (try
      (.setParameter result
        "http://apache.org/xml/properties/security-manager"
        (.newInstance (Class/forName "com.sun.org.apache.xerces.internal.utils.XMLSecurityManager")))
      (catch ClassNotFoundException _ (println "Class com.sun.org.apache.xerces.internal.utils.XMLSecurityManager not
       found")))

    (try
      (.setParameter result
        com.sun.org.apache.xerces.internal.impl.Constants/DOM_ERROR_HANDLER
        schema-error-handler))

    result))

(defmulti read-schema class)
(defmethod read-schema File [f] (.loadURI (loader) (.getPath f)))
(defmethod read-schema String [s] (.load (loader) (lsinput s)))



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
  (let [facets (make-all-facets fl)] (-> facets #(if (= (:whitespace %) "preserve") (dissoc % :whitespace) %))))

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
                                                             :type     :enum})))
               ))))

(defn- make-multiplicity [particleDecl]
  [(.getMinOccurs particleDecl)
   (if (.getMaxOccursUnbounded particleDecl) :unbounded (.getMaxOccurs particleDecl))])

(defn- model-group-elements [mgi]
  (let [fParticles (.getParticles mgi)
        n          (.getLength fParticles)]
    (vec (for [i (range n)]
           (let [particleDecl (.item fParticles i)
                 fValue       (.fValue particleDecl)]
             (-> fValue read-element (assoc :multiplicity (make-multiplicity particleDecl))))))))

(defn read-element [eld]
  (let [m {:name (.getName eld)}]
    (if (instance?  com.sun.org.apache.xerces.internal.impl.xs.XSModelGroupImpl eld)
      (assoc m :type :complex :elements (model-group-elements eld))
      (type-def m (.getTypeDefinition eld)))))

(defn schema-element [x]
  "Returns element definition of the root element of the schema file"
  (if-let [schema (read-schema x)]
    (some-> schema components first read-element)
    (do (log (str "Parsing " (class x) " returned nil")))))

(defn parse-resource [r] (schema-element (File. (resource-location r))))
(defn print-sample [] (clojure.pprint/pprint (parse-resource "schema1.xsd")))

