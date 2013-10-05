(defproject com.akolov/xelery "0.4.3-SNAPSHOT"
  :description "XML Schema Reader fro Clojure"
  :min-lein-version "2.0.0"
  :url "https://github.com/kolov/xelery"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :lein-release {:deploy-via :clojars}
  :profiles {:dev {:resource-paths ["dev-data"]}
             :user {:plugins [[lein-clojars "0.9.1"]]}}
  :dependencies [[org.clojure/clojure "1.4.0"]]

  :deploy-repositories [["releases" {:url "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
                                     :creds :gpg}
                         "snapshots" {:url "https://oss.sonatype.org/content/repositories/snapshots/"
                                      :creds :gpg}]]
  :scm {:url "https://github.com/kolov/xelery.git"}
  :pom-addition [:developers [:developer [:name "Assen Kolov"]
                              [:email "assen.kolov@gmail.com"]
                              ]]
  )
