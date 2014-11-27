(load-package jessmw.pkg)
(load-package routeplanner.pkg)
(load-package GUI.pkg)
(SMRConnect jess_conf.xml)
(GUI)

(deftemplate plan
    (slot movenum)
    (slot theta))
(assert (plan (movenum 1)(theta 0)))

(deftemplate goal
    (slot waypoint))
;(defquery get-goals
;    ?goal<-(goal (waypoint ?wp)))
;(deffunction get-goals ()
;    (bind ?g (run-query* get-goals))
;    (return ?g))
;(deftemplate goals
;    (multislot waypoints))
;(assert (goals (waypoints 2 6 9)))
(deftemplate home
    (slot waypoint))
(assert (home (waypoint 1)))

;(deffunction assert-goals)

(assert (goal (waypoint 2)))
;(assert (goal (waypoint 3)))
;(assert (goal (waypoint 4)))
;(assert (goal (waypoint 5)))
(assert (goal (waypoint 6)))
;(assert (goal (waypoint 7)))
;(assert (goal (waypoint 8)))
(assert (goal (waypoint 9)))
;(assert (goal (waypoint 12)))
(assert (goal (waypoint 13)))
(assert (goal (waypoint 15)))
(defglobal ?*map* = "waypoints")
(get-route ?*map* 1)

(defglobal ?*cmdnum* = 2)
; Keep track of SMR command IDs 
(deffunction MyTalk (?str)
    (SMRTalk ?str)
    (++ ?*cmdnum*)
    (return (- ?*cmdnum* 1))
)

(defrule move-xy
    ?m<-(move ?node ?x ?y ?th ?danger)
    ?d<-(move-from ?fnode ?fx ?fy ?fth ?fdanger)
    =>
    (printout t "move " ?fx " " ?fy " => " ?x " " ?y crlf)
	(bind ?turnnum (MyTalk (str-cat "turn " ?th " \"rad\"")))
    (MyTalk (str-cat "drive :((($odox - " ?x ")*($odox - " ?x ") + ($odoy - " ?y ")*($odoy - " ?y ")) < 0.008)")); | ($irdistfrontmiddle < 1 ))" ))
    (bind ?stopnum (MyTalk "stop"))
    (if (or (eq ?danger 0)(eq ?fdanger 0)) then
        
     elif (and (eq ?danger 1)(eq ?fdanger 1)) then
        (assert (react-door ?fnode ?fx ?fy ?node ?x ?y ?turnnum))
     elif (and (eq ?danger 2)(eq ?fdanger 2)) then
        (assert (react-robot ?fnode ?fx ?fy ?node ?x ?y ?turnnum)) ; Reactive behavior for the robot can be implemented in about the same way as the door
    )
    
    (assert (position ?stopnum ?node))
    
    (retract ?m ?d)
)
(defrule check-goals ; When at goal, remove the goal fact
    ?g<-(goal (waypoint ?goal))
    (position ?stopnum ?goal)
    (CurrentCommand (id ?stopnum))
    =>
    (retract ?g)
)
(defrule do-pause
    ?p<-(pause)
    ?s<-(start)
    (position ?stopnum ?wp)
    (CurrentCommand (id ?stopnum))
    =>
    (MyTalk "flushcmds")
    (MyTalk "stop")
    (retract ?p ?s)
)

(defrule react-door-stop
	 ?d<-(react-door ?fnode ?fx ?fy ?node ?x ?y ?cmdid)
    (CurrentCommand (id ?cmdid))
    (laserbox.detections (value ?df ?dl ?dr ? ? ?))
    (not (door-stopped ?))
    =>
    (printout t "react-door-stop " ?fnode " " ?cmdid " " ?df " " ?dl " " ?dr crlf)
    (if (or (> ?df 10)(> ?dl 10)(> ?dr 10)) then
        (MyTalk "flushcmds")
        (MyTalk "stop")
        (assert (door-stopped ?cmdid))
    )
)
(defrule react-door-go
	(react-door ?fnode ?fx ?fy ?node ?x ?y ?cmdid)	
    (laserbox.detections (value ?df ?dl ?dr ? ? ?))
    ?d<-(door-stopped ?cmdid)
    (not (door-moving))
    (smr0.mrc.mrc.odometry (x ?ox)(y ?oy)(theta ?oth))
    =>
    (printout t "react-door-go " ?df " " ?dl " " ?dr crlf)
    (if (and (eq ?df 0.0)) then
        (printout t "in react-door-go " ?df " " ?dl " " ?dr crlf)
        (bind ?th (SMRTalk (str-cat "eval atan2(" (- ?y ?oy) "," (- ?x ?ox) ") - " ?oth)))
        (-- ?*cmdnum*)
		(MyTalk (str-cat "turn " ?th " \"rad\""))
	    (MyTalk (str-cat "drive :((($odox - " ?x ")*($odox - " ?x ") + ($odoy - " ?y ")*($odoy - " ?y ")) < 0.01)")); | ($irdistfrontmiddle < 1 ))" ))
	    (bind ?stopid (MyTalk "stop"))
        (assert (position ?stopid ?node))
        
        (assert (door-moving))
        (assert (door-replan ?stopid (+ ?oth ?th) ?node))
    	(retract ?d)
        (facts)
    )
)
(defrule react-door-replan
	?d<-(door-moving)
    ?r<-(door-replan ?stopid ?theta ?node)
	(CurrentCommand (id ?stopid))
    ?p<-(plan (theta ?fth))
	=>
    (printout t "react-door-replan " ?stopid " " ?theta " " ?node crlf)
	
	(modify ?p (movenum 1)(theta ?theta))
	(get-route ?*map* ?node)
    (retract ?d ?r)
)
(defrule print-id
    (CurrentCommand (id ?id))
    =>
    (printout t "command " ?id crlf))


(defrule do-plan
    (start)
    ?p<-(plan (movenum ?movenum)(theta ?fth))
    ?m0<-(move-plan ?movenum ?node ?x ?y ?danger)
    ?m1<-(move-plan ?movenum1&:(eq ?movenum1 (- ?movenum 1)) ?fnode ?fx ?fy ?fdanger)
    (not (do-move ? ? ?))
    =>
    (bind ?th (SMRTalk (str-cat "eval atan2(" (- ?y ?fy) "," (- ?x ?fx) ") - " ?fth)))
    (printout t "plan " ?movenum ": " ?fnode " " ?fx " " ?fy " " ?fth " => " ?node " " ?x " " ?y " " ?th crlf)
    (modify ?p (movenum (+ ?movenum 1))(theta (+ ?fth ?th)))
    (assert (move ?node ?x ?y ?th ?danger))
    (assert (move-from ?fnode ?fx ?fy ?fth ?fdanger))

    (retract ?m1)
)
(defrule remove-last-move-plan
    (plan (movenum ?movenum)(theta ?fth))
    ?m0<-(move-plan ?movenum ?node ?x ?y ?danger)
    (not (move-plan ?movenum1&:(eq ?movenum1 (- ?movenum 1)) ?fnode ?fx ?fy ?fdanger))
    =>
    (retract ?m0)
)

;(watch facts)
(run-until-halt)