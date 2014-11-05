(load-package jessmw.pkg)
(load-package routeplanner.pkg)
;(SMRConnect jess_conf.xml)

(deftemplate goal
    (slot x)
    (slot y))

(assert (goal (x 1)(y 4)))
(get-route)

(defrule move-xy-1
    (move ?x ?y)
    =>
    ; Get current position
    ; Compare angles
    ; If angles not same, go to turn function
    ; Else, go to move function
)
