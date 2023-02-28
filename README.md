# Tracking UseCase #

Tracking a system involves 
* building a rich context, by
  * mixing the system's events with other relevant ones
  * churning them through the process of correlation, transformation and stateful processing &
* drawing distilled inferences thereof..

<img width="1134" alt="Screen Shot 2023-02-28 at 9 46 58 AM" src="https://user-images.githubusercontent.com/107064168/221904812-b498f288-12af-4e40-89d1-a754e01dfc91.png">

*WHAT?*

The demo aims to track baggages @ airport until completion of the journey. 

*HOW?*

A baggage state/context is built from the following disparate events for detecting bags missing the flights that carry the affected passengers. 

Events          | Source
-------------   | -------------
Checkouts       | emanating from a checkout processing system
RFIDs           | emanating from sensors as bags move about prior to loading & after unloading
Flight arrivals | emanating from a flight monitoring system

*WHY?*

By proactively detecting missed events, precious time can be bought for fast remedial actions & mitigating situations.

<img width="1361" alt="Screen Shot 2023-02-28 at 11 18 46 AM" src="https://user-images.githubusercontent.com/107064168/221928648-d1630a11-e604-4113-bd67-3e4e2847d63d.png">

1. @ post-checkin: From the checkin event, a passenger's luggage event is created & stored in data-store (denormalized form). A LuggageState, is also created alongside, that gets incrementally updated across the different event streams throughout the journey & provides a consolidated view of the passenger (represented here as his/her baggages & tracked by his/her ticket) until conclusion of the journey.
2. @ sensor tracking: Enroute to the flight or baggage area or transfers, sesors monitor the bags by reading tags & sending out RFIDs. RFIDs are skinny events carrying just a tagId. It gets enriched many times within the system so correct correlation can be applied for building up the context for every passenger (ticket) on the journey.
3. @ flight departure: When the flight departs, the depart event queries HZ-storage for all bags that didn't make the flight.The missed bags along with necessary ticket info are published out for proactive remediation & mitigation.    
