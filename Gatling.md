# Gatling

## MongoDB

rampUsers(10) during (10.seconds),
constantUsersPerSec(1) during (10.seconds)
> min response time (ms)                                                             |         6 |         6 |         -
> max response time (ms)                                                             |        23 |        23 |         -
> mean response time (ms)                                                            |        13 |        13 |         -

rampUsers(10) during (10.seconds),
constantUsersPerSec(1) during (10.seconds)
> min response time (ms)                                                             |         9 |         9 |    10,006
> max response time (ms)                                                             |    10,012 |        18 |    10,012
> mean response time (ms)                                                            |       710 |        13 |    10,009

## Slick

### With journal_mode=OFF

rampUsers(10) during (10.seconds),
constantUsersPerSec(1) during (10.seconds)
> min response time (ms)                                                             |         8 |         8 |    10,008
> max response time (ms)                                                             |    10,024 |     3,656 |    10,024
> mean response time (ms)                                                            |     4,818 |       299 |    10,015

rampUsers(10) during (10.seconds),
constantUsersPerSec(1) during (10.seconds)
> min response time (ms)                                                             |         8 |         8 |    10,007
> max response time (ms)                                                             |    10,015 |        20 |    10,015
> mean response time (ms)                                                            |     5,363 |        17 |    10,013

### With journal_mode=WAL

rampUsers(1) during (10.seconds),
constantUsersPerSec(1) during (10.seconds)
> min response time (ms)                                                             |        15 |        15 |         -
> max response time (ms)                                                             |        27 |        27 |         -
> mean response time (ms)                                                            |        19 |        19 |         -

rampUsers(10) during (10.seconds),
constantUsersPerSec(2) during (10.seconds)
> min response time (ms)                                                             |         6 |         6 |         -
> max response time (ms)                                                             |        29 |        29 |         -
> mean response time (ms)                                                            |        12 |        12 |         -