(load-package jessmw.pkg)
(load-package routeplanner.pkg)
(SMRConnect jess_conf.xml)
(facts)

(deftemplate goal
    (slot x)
    (slot y))
(defclass odo jessmw.Odometry)
(deftemplate smr0.mrc.mrc.odometry
    (slot dist)
    (slot distLeft)
    (slot distRight)
    (slot lastupdated)
    (slot robot)
    (slot theta)
    (slot velocity)
    (slot OBJECT)
    (slot x)
    (slot y))

(assert (goal (x 1)(y 4)))
(assert (move 1 0))

(defrule move-xy
    ?m<-(move ?x ?y)
    (smr0.mrc.mrc.odometry (x ?ox) (y ?oy))
    =>
    (if (< ?ox ?x) then
        (SMRTalk "fwd 0.1")
     else
        (printout t "Done: x=" ?ox "  y=" ?oy crlf)
        (retract ?m)
        (halt)
    )
)

(run-until-halt)

;(defrule move-xy-1
;    (move ?x ?y)
;    =>
;    ; Get current position
;    ; Compare angles
;    ; If angles not same, go to turn function
;    ; Else, go to move function
;)