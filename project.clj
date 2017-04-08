(defproject com.akolov/xelery "0.4.5"
  :description "XML Schema Reader for Clojure"
  :min-lein-version "2.0.0"
  :url "https://github.com/kolov/xelery"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :lein-release {:deploy-via :clojars}
  :profiles {:dev {:resource-paths ["dev-data"]
                   :plugins        [[test2junit "1.0.1"]]}}
  :dependencies [[org.clojure/clojure "1.5.1"]]


  :scm {:url "https://github.com/kolov/xelery.git"}
  :pom-addition [:developers [:developer [:name "Assen Kolov"]
                              [:email "assen.kolov@gmail.com"]
                              ]]
  :repositories [
                 ["snapshots" "http://nexus.akolov.com/content/repositories/snapshots"]
                 ["releases" {
                              :url           "http://nexus.akolov.com/content/repositories/releases"
                              :sign-releases false
                              }
                  ]]
  )
