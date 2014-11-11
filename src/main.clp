(load-package jessmw.pkg)
(load-package routeplanner.pkg)
(SMRConnect jess_conf.xml)

(deftemplate plan
    (slot movenum)
    (slot theta))
(assert (plan (movenum 1)(theta 0)))
(deftemplate goal
    (slot waypoint))
;(defclass odo jessmw.Odometry)
;(deftemplate smr0.mrc.mrc.odometry
;    (slot dist)
;    (slot distLeft)
;    (slot distRight)
;    (slot lastupdated)
;    (slot robot)
;    (slot theta)
;    (slot velocity)
;    (slot OBJECT)
;    (slot x)
;    (slot y))

(assert (goal (waypoint 2)))
;(assert (goal (waypoint 3)))
;(assert (goal (waypoint 4)))
(assert (goal (waypoint 5)))
;(assert (goal (waypoint 6)))
(assert (goal (waypoint 7)))
(assert (goal (waypoint 8)))
;(assert (goal (waypoint 9)))
;(assert (goal (waypoint 12)))
;(assert (goal (waypoint 15)))
;(assert (move 2 2))
(get-route "waypoints" 1)

(defrule move-xy
    ?m<-(move ?x ?y)
    ?d<-(do-move ?fx ?fy ?th)
    =>
    (printout t "move " ?fx " " ?fy " => " ?x " " ?y crlf)
    ;(bind ?ox (SMRTalk "eval $odox"))
    ;(bind ?oy (SMRTalk "eval $odoy"))
	(SMRTalk (str-cat "turn " ?th " \"rad\""))
    (SMRTalk (str-cat "drive :(abs($odox - " ?x ") < 0.2 & abs($odoy - " ?y ") < 0.2)")); | ($irdistfrontmiddle < 1 ))" ))
    (SMRTalk "stop")

    (retract ?m ?d)
)

(defrule do-plan
    ?p<-(plan (movenum ?movenum)(theta ?fth))
    ?m0<-(move-plan ?movenum ?x ?y)
    ?m1<-(move-plan ?movenum1&:(eq ?movenum1 (- ?movenum 1)) ?fx ?fy)
    (not (do-move ? ? ?))
    =>
    (bind ?th (SMRTalk (str-cat "eval atan2(" (- ?y ?fy) "," (- ?x ?fx) ") - " ?fth)))
    (printout t "plan " ?movenum ": " ?fx " " ?fy " " ?fth " => " ?x " " ?y " " ?th crlf)
    (modify ?p (movenum (+ ?movenum 1))(theta (+ ?fth ?th)))
    (assert (move ?x ?y))
    (assert (do-move ?fx ?fy ?th))
    (retract ?m1)
)

(run)