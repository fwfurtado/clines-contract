import org.springframework.cloud.contract.spec.Contract

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

final BASE_URL = "flights"

final EXISTING_FLIGHT_ID = 1
final NON_EXISTING_FLIGHT_ID = 2

final EXISTING_ARRIVAL_ID = "GRU"
final NON_EXISTING_ARRIVAL_ID = "XRF"

final EXISTING_DEPARTURE_ID = "CGH"
final NON_EXISTING_DEPARTURE_ID = "FRX"

final EXISTING_AIRCRAFT_ID = "AC123AX"
final NON_EXISTING_AIRCRAFT_ID = "BC123AX"

final FUTURE_FLIGHT_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().plusDays(1))
final PAST_FLIGHT_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().minusDays(1))

final POSITIVE_FLIGHT_PRICE = new Random().doubles(1, 1, 1000).findFirst().orElseThrow()
final NEGATIVE_FLIGHT_PRICE = new Random().doubles(1, -1000, 0).findFirst().orElseThrow()

[
        Contract.make {
            name "should return 404 when flight does not exists"

            request {
                method GET()

                url "/${BASE_URL}/${NON_EXISTING_FLIGHT_ID}"
            }

            response {
                status NOT_FOUND()

                bodyMatchers {
                    jsonPath('$', byRegex('^$'))
                }
            }
        },

        Contract.make {
            name "should return an existing flight by id"

            request {
                method GET()

                url "/${BASE_URL}/${EXISTING_FLIGHT_ID}"
            }

            response {
                status OK()

                headers {
                    contentType applicationJson()
                }

                body(
                        "id": fromRequest().path(1),
                        "time": value(regex("([0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9]).\\d+")),
                        "price": value(anyDouble()),
                        "aircraft": "AC123AX",
                        "departure": "GRU",
                        "arrival": "CGH",

                )
            }
        },

        Contract.make {
            name "should return a list of existing flights"

            request {
                method GET()
                url BASE_URL
            }

            response {
                status OK()

                headers {
                    contentType applicationJson()
                }

                body([
                        [
                                "id"       : EXISTING_FLIGHT_ID,
                                "time"     : value(regex("([0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9]).\\d+")),
                                "price"    : value(anyDouble()),
                                "aircraft" : "AC123AX",
                                "departure": "GRU",
                                "arrival"  : "CGH",
                        ]
                ])
            }
        },

        Contract.make {
            name "should register a new flight"

            request {
                method POST()
                url BASE_URL

                headers {
                    contentType applicationJson()
                }

                body(
                        "time": FUTURE_FLIGHT_TIME,
                        "price": POSITIVE_FLIGHT_PRICE,
                        "aircraftId": EXISTING_AIRCRAFT_ID,
                        "departureId": EXISTING_DEPARTURE_ID,
                        "arrivalId": EXISTING_ARRIVAL_ID,
                )
            }

            response {
                status CREATED()

                headers {
                    header(location(), "/${BASE_URL}/${NON_EXISTING_FLIGHT_ID}")
                }

                bodyMatchers {
                    jsonPath('$', byRegex('^$'))
                }
            }
        },
        Contract.make {
            name "should return 400 when try to register a new flight with nullable fields"

            request {
                method POST()
                url BASE_URL

                headers {
                    contentType applicationJson()
                }

                body(
                        "time": null,
                        "price": null,
                        "aircraftId": null,
                        "departureId": null,
                        "arrivalId": null,
                )
            }

            response {
                status BAD_REQUEST()
                body(
                        "errors": [
                                ["field": "time", "message": "must not be null"],
                                ["field": "price", "message": "must not be null"],
                                ["field": "aircraftId", "message": "must not be blank"],
                                ["field": "departureId", "message": "must not be blank"],
                                ["field": "arrivalId", "message": "must not be blank"],
                        ]
                )
            }
        },

        Contract.make {
            name "should return 400 when try to register a new flight with invalids aircraft and departure and arrival"

            request {
                method POST()
                url BASE_URL

                headers {
                    contentType applicationJson()
                }

                body(
                        "time": FUTURE_FLIGHT_TIME,
                        "price": POSITIVE_FLIGHT_PRICE,
                        "aircraftId": NON_EXISTING_AIRCRAFT_ID,
                        "departureId": NON_EXISTING_DEPARTURE_ID,
                        "arrivalId": NON_EXISTING_ARRIVAL_ID,
                )
            }

            response {
                status BAD_REQUEST()
                body(
                        "errors": [
                                ["message": "Invalid Aircraft"],
                                ["message": "Invalid Departure"],
                                ["message": "Invalid Arrival"],
                        ]
                )
            }

        },

        Contract.make {
            name "should return 400 when try to register a new flight with a time in past"

            request {
                method POST()
                url BASE_URL

                headers {
                    contentType applicationJson()
                }

                body(
                        "time": PAST_FLIGHT_TIME,
                        "price": POSITIVE_FLIGHT_PRICE,
                        "aircraftId": EXISTING_AIRCRAFT_ID,
                        "departureId": EXISTING_DEPARTURE_ID,
                        "arrivalId": EXISTING_ARRIVAL_ID,
                )
            }

            response {
                status BAD_REQUEST()
                body(
                        "errors": [
                                ["message": "must be a date in the present or in the future", "field": "time"],
                        ]
                )
            }

        },

        Contract.make {
            name "should return 400 when try to register a new flight with a negative price"

            request {
                method POST()
                url BASE_URL

                headers {
                    contentType applicationJson()
                }

                body(
                        "time": FUTURE_FLIGHT_TIME,
                        "price": NEGATIVE_FLIGHT_PRICE,
                        "aircraftId": EXISTING_AIRCRAFT_ID,
                        "departureId": EXISTING_DEPARTURE_ID,
                        "arrivalId": EXISTING_ARRIVAL_ID,
                )
            }

            response {
                status BAD_REQUEST()
                body(
                        "errors": [
                                ["message": "must be greater than 0", "field": "price"],
                        ]
                )
            }

        },
]