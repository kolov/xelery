(defproject com.akolov/xelery "0.3.0"
  :description "XML Schema Reader"
  :min-lein-version "2.0.0"
  :url "https://github.com/kolov/xelery"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :lein-release {:deploy-via :clojars}
  :profiles {:dev {:resource-paths ["dev-data"]}
              :user {:plugins [[lein-clojars "0.9.1"]]}}
  :dependencies [[org.clojure/clojure "1.4.0"]]
 
)
