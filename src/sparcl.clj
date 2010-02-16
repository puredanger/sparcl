(ns sparcl
	(:gen-class)
	(:use compojure)
	(:import [com.hp.hpl.jena.query QueryExecutionFactory QueryFactory]))

(defn exec-sparql
	"Execute SPARQL query against model and return a ResultSet"
	[sparql model] 
	(.execSelect 
		(. QueryExecutionFactory create 
			(. QueryFactory create sparql) 
			model) ))

(defn exec [rdf sparql]
	(html [:html [:body [:h1 test]]])
)
			
(defroutes sparcl
	(GET "/" (exec nil nil))
	(POST "/" (exec (params :rdf) (params :sparql)))
	(ANY "*" 404))

(defn -main [& args]
	(run-server {:port 8080}
		"/*" (servlet sparcl)))
