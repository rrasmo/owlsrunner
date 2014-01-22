

;set input for RootPerform: the SubscriptionId
(set-input "amazon:RootPerform" amazonSubscriptionId "0XBR60FWRRWR224WJZR2")


;set input for CartCreate: a list of 2 items, each with its quantity
(bind ?ca1 (make-instance of onto:CartAddition (onto:ASIN "0307337979") (onto:quantity 1)))
(bind ?ca2 (make-instance of onto:CartAddition (onto:ASIN "0596515812") (onto:quantity 2)))
(bind ?crwit (make-instance of onto:CartRequestWithItems (onto:cartAddition (list ?ca1 ?ca2))))
(set-input "amazon:CartCreatePerform" amazonCartRequestWithItems ?crwit)


;set a rule to run the process through the desired path: no searches, do the CartCreate and exit
(defrule exePath
	(enabled ?n&amazon:InitialSplit|amazon:CartCreatePerform|amazon:CartCreateProduce|amazon:ManageCartChoice_Start|amazon:FinishProduce|amazon:ManageCartChoice_End|amazon:ManageCartRepeatUntil)
=>
	(execute ?n)
)


