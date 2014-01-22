

;set input for RootPerform: the SubscriptionId
(set-input "amazon:RootPerform" amazonSubscriptionId "0XBR60FWRRWR224WJZR2")


;set input for ItemSearch: search for books with the keywords "web services"
(bind ?sr (make-instance of onto:SearchRequest (onto:keywords "web services") (onto:searchIndex "Books")))
(set-input "amazon:ItemSearchPerform" amazonSearchRequest ?sr)


;rule that asserts a fact with the ASIN of the cheapest item of the SearchResult, every time a search occurs
(defrule findCheapestItem
	(object (:NAME "amazonItemSearchPerformOutputSet") (amazonSearchResult ?result&~nil))
=>
	(bind ?minASIN nil)
	(bind ?minAmount 0)	
	(foreach ?item (slot-get ?result onto:searchItem)
		(bind ?amount (slot-get (slot-get ?item onto:price) onto:amount))
		(if (or (< ?amount ?minAmount) (eq ?minASIN nil))
			then
				(bind ?minAmount ?amount)
				(bind ?minASIN (slot-get ?item onto:ASIN))
		)
	)
	(assert (cheapestItem ?minASIN))
)


;execute ItemSearch only if the cheapestItem doesn't exist
(defrule exeItemSearch
	(enabled amazon:ItemSearchPerform)
	(not (cheapestItem ?))
=>
	(execute amazon:ItemSearchPerform)
)


;execute the CartCreate only when a cheapestItem has been found, and then add it
(defrule exeCartCreate
	(enabled amazon:CartCreatePerform)
	(cheapestItem ?asin)
=>
	(bind ?ca1 (make-instance of onto:CartAddition (onto:ASIN ?asin) (onto:quantity 1)))
	(bind ?crwit (make-instance of onto:CartRequestWithItems (onto:cartAddition ?ca1)))
	(set-input "amazon:CartCreatePerform" amazonCartRequestWithItems ?crwit)
	(execute amazon:CartCreatePerform)
)



;set a rule to execute the nodes in the desired path, apart from ItemSearch and CartCreate
(defrule exePath
	(enabled ?n&amazon:InitialSplit|amazon:ItemSearchRepeatWhile|amazon:CartCreateProduce|amazon:ManageCartChoice_Start|amazon:FinishProduce|amazon:ManageCartChoice_End|amazon:ManageCartRepeatUntil)
=>
	(execute ?n)
)

