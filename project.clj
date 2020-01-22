(defproject alita "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [edu.cmu.sphinx/sphinx4-core "5prealpha-SNAPSHOT"]
                 [edu.cmu.sphinx/sphinx4-data "5prealpha-SNAPSHOT"]]
  :mirrors {"sonatype" {:name "sonatype"
                        :url "https://oss.sonatype.org/content/repositories/snapshots"}
            "central" {:name "central"
                       :url "https://repo1.maven.org/maven2/"}}
  :main ^:skip-aot alita.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
