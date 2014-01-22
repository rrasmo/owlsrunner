
;temporary object deftemplate needed for the defqueries
(deftemplate MAIN::object 
	"$PROTEGE-OBJECTS$" 
	(slot is-a (type SYMBOL)) 
	(slot is-a-name (type STRING)) 
	(slot OBJECT (type OBJECT)) 
	(multislot rdfs:label) 
	(multislot rdfs:comment)
)
	 


; functions to get and set inputs and outputs in the IOSets
(deffunction get-input (?perform ?input)
	(bind ?result (run-query* findInputSetByPerformName ?perform))
	(while (?result next)
		(bind ?inputSet (?result getObject obj))
	)
	(return (slot-get ?inputSet ?input))
)

(deffunction get-output (?perform ?output)
	(bind ?result (run-query* findOutputSetByPerformName ?perform))
	(while (?result next)
		(bind ?outputSet (?result getObject obj))
	)
	(return (slot-get ?outputSet ?output))
)

(deffunction get-local (?perform ?local)
	(bind ?result (run-query* findLocalSetByPerformName ?perform))
	(while (?result next)
		(bind ?localSet (?result getObject obj))
	)
	(return (slot-get ?localSet ?local))
)

(deffunction set-input (?perform ?input ?value)
	(bind ?result (run-query* findInputSetByPerformName ?perform))
	(while (?result next)
		(bind ?inputSet (?result getObject obj))
	)
	(slot-set ?inputSet ?input ?value)
)

(deffunction set-output (?perform ?output ?value)
	(bind ?result (run-query* findOutputSetByPerformName ?perform))
	(while (?result next)
		(bind ?outputSet (?result getObject obj))		
	)
	(slot-set ?outputSet ?output ?value)
)

(deffunction set-local (?perform ?local ?value)
	(bind ?result (run-query* findLocalSetByPerformName ?perform))
	(while (?result next)
		(bind ?localSet (?result getObject obj))		
	)
	(slot-set ?localSet ?local ?value)
)

(defquery findInputSetByPerformName
	(declare (variables ?perform))
	(object (rdfs:label ?perform) (rdfs:comment "InputSet") (OBJECT ?obj))
)

(defquery findOutputSetByPerformName
	(declare (variables ?perform))
	(object (rdfs:label ?perform) (rdfs:comment "OutputSet") (OBJECT ?obj))
)

(defquery findLocalSetByPerformName
	(declare (variables ?perform))
	(object (rdfs:label ?perform) (rdfs:comment "LocalSet") (OBJECT ?obj))
)



;function to auto-execute nodes
(deffunction execute (?node)
	(call ?*runner* scheduleNodeAutoExecution ?node)
)

;function to restart Jess environment
(deffunction restart ()
	(call ?*runner* restartJess)
)

