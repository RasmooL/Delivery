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

(assert (goal (waypoint 6)))
(assert (goal (waypoint 10)))
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