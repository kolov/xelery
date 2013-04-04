(ns xelery.lsinput
  :import [org.w3c.dom.ls LSInput])

(defn lsinput[data]
  (reify org.w3c.dom.ls.LSInput
  (getBaseURI [this] nil)
  (getByteStream [this] (.getBytes data "UTF-8"))
  (getCertifiedText [this] nil)
  (getCharacterStream [this] nil)
  (getEncoding [this] nil)
  (getPublicId [this] nil)
  (getStringData [this] data)
  (getSystemId [this] nil)
  (setBaseURI [this])
  (setByteStream [this])
  (setCertifiedText [this])
  (setCharacterStream [this])
  (setEncoding [this])
  (setPublicId [this])
  (setStringData [this])
  (setSystemId [this])))