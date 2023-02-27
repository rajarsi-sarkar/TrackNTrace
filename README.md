Tracking any system involves 
- building a context from different unrelated events & 
- drawing inference from

In this demo simulation, a baggage state/context is built from considering the following disparate events
- Checkouts, 											emanating from a checkout processing system
- RFIDs,                          emanating from sensors as bags move about prior to loading & after unloading
- Flight arrivals & departures,   emanating from a flight monitoring system,
to detect bags, that miss flights carrying the affected passengers, and notify airlines for proactive action taking

(What)
This demo aims to detect bags, that miss flights carrying the affected passengers, and notify airlines for proactive action taking
(How)
It build a baggage state/context from considering the following disparate events
- Checkouts, 											emanating from a checkout processing system
- RFIDs,                          emanating from sensors as bags move about prior to loading & after unloading
- Flight arrivals & departures,   emanating from a flight monitoring system,
(Why)
to detect bags, that miss flights carrying the affected passengers, and 
notify airlines for remedial actions proactively. 

<img width="874" alt="Screen Shot 2023-02-27 at 3 18 11 PM" src="https://user-images.githubusercontent.com/107064168/221687952-fcbb5ba8-4464-4303-beff-a25f49913b74.png">

1. @ checkin, the passenger's luggages get stored in IMDG in denormalized form, for processing speed. LuggageStateStore, that gets created alongwith, gets incrementally updated across the different event streams & provides a consolidated view of a passenger's luggages (tracked by his/her ticket) until conclusion of his/her journey.
2. @ sensor tracking, enroute to the flight or baggage claim or transfers. Its a skinny event carrying only a tagId on it. Its gets enriched multiple times, so it can be correctly correlated to the passenger (by the ticket) and his/her luggage status continuously updated.
3. @ flight departure, HZ-Platform is queried for all bags with OnSchedule status set to false to detect missed bags if any.
The missed bags along with necessary passenger info can be published internally as notifications for proactive remediation.    
