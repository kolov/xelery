(defproject com.akolov/xelery "0.2.0i-SNAPSHOT"
  :description "XML Schema Reader"
  :url "https://github.com/kolov/xelery"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :lein-release {:deploy-via :clojars}
  :profiles {:dev {:resource-paths ["dev-data"]}
              :user {:plugins [[lein-clojars "0.9.1"]]}}
  :dependencies [[org.clojure/clojure "1.4.0"]]
 
)
