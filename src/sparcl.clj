(ns sparcl
	(:gen-class)
	(:use compojure)
	(:import (java.io StringReader)
			 (com.hp.hpl.jena.query QueryExecutionFactory QueryFactory)
			 (com.hp.hpl.jena.rdf.model ModelFactory)))

(defn exec-sparql
	"Execute SPARQL query against model and return a ResultSet"
	[sparql model] 
	(.execSelect 
		(. QueryExecutionFactory create 
			(. QueryFactory create sparql) 
			model) ))

(defn create-model [rdf]
	(let [model (ModelFactory/createDefaultModel)]	
		(.read model (StringReader. rdf) nil)
		model))
	
(defn data-html [results]
	(for [solution (iterator-seq results)]
		[:tr
			(for [var-name (iterator-seq (.varNames solution))]
				[:td (.toString (.get solution var-name))])]))
	
(defn vars-html [results] 
	[:tr
		(for [binding (.getResultVars results)] 
			[:th binding])])
		
(defn results-html [results]
	[:table {:border "1"}
		(vars-html results) 
		(data-html results)])

(defn exec-html [rdf sparql results]
	(html [:html
			[:head
				[:title "SPARCL: SPARQL Query Tester"]]
			[:body
				(form-to [:post "/"]
					[:h2 "RDF:"]
					[:textarea {:name "rdf" :rows 10 :cols 80} rdf]
					[:h2 "SPARQL Query:"]
					[:textarea {:name "sparql" :rows 10 :cols 80} sparql]
					[:p (submit-button "Execute")]
					[:h2 "Results:"]
					results)]]))

(defn str-exists? [s] 
	(not (or (nil? s) (= (.length s) 0))))

(defn strs-exist? [strs]
	(every? str-exists? strs))

(defn exec-page [rdf sparql]
	(exec-html rdf sparql
		(if (strs-exist? [rdf sparql]) 
			(results-html (exec-sparql sparql (create-model rdf)))
			[:p "No query executed."])))
			
(defroutes sparcl
	(GET "/" (exec-page nil nil))
	(POST "/" (exec-page (params :rdf) (params :sparql)))
	(ANY "*" 404))

(defn -main [& args]
	(run-server {:port 8080}
		"/*" (servlet sparcl)))
