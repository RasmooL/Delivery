(load-package jessmw.pkg)
(SMRConnect jess_conf.xml)
(defrule movefwd
    (movefwd)
    (smr0.mrc.mrc.odometry (x ?x)(y ?y))
    =>
    (SMRTalk "drive " ?x " " ?y " 0 \"rad\"" )
)
(facts)