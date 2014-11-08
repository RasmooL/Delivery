(load-package jessmw.pkg)
(load-package routeplanner.pkg)
;(SMRConnect jess_conf.xml)

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
(assert (goal (waypoint 3)))
(assert (goal (waypoint 4)))
(assert (goal (waypoint 5)))
(assert (goal (waypoint 6)))
(assert (goal (waypoint 7)))
(assert (goal (waypoint 8)))
(assert (goal (waypoint 9)))
(assert (goal (waypoint 12)))
(assert (goal (waypoint 15)))
;(assert (move 2 2))
(get-route "waypoints" 1)

(defrule move-xy
    ?m<-(move ?x ?y)
    =>
    (bind ?ox (SMRTalk "eval $odox"))
    (bind ?oy (SMRTalk "eval $odoy"))
    (bind ?th (SMRTalk (str-cat "eval atan2(" (- ?y ?oy) "," (- ?x ?ox) ") -  $odoth")))
	(SMRTalk (str-cat "turn " ?th " \"rad\""))
    (SMRTalk (str-cat "drive :(abs($odox - " ?x ") < 0.1 & abs($odoy - " ?y ") < 0.1 | ($irdistfrontmiddle < 1 ))" ))
    (SMRTalk "stop")

    (retract ?m)
)

;(run)