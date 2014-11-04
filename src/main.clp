(load-package jessmw.pkg)
(load-package routeplanner.pkg)
;(SMRConnect jess_conf.xml)

(deftemplate goal
    (slot x)
    (slot y))

(assert (goal (x 1)(y 4)))
(get-route)