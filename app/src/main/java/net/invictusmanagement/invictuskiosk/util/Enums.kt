package net.invictusmanagement.invictuskiosk.util

enum class FilterOption(val displayName: String) {
    FIRST_NAME("First Name"),
    LAST_NAME("Last Name"),
    UNIT_NUMBER("Unit Number")
}

enum class ConnectionState(val displayName: String){
    CONNECTING("Connecting..."),
    CONNECTED("Connected"),
    DISCONNECTED("Disconnected"),
    FAILED("Failed")
}

enum class SignalRConnectionState(val displayName: String){
    CONNECTING("Connecting..."),
    CONNECTED("Connected"),
}

enum class IntroButtons(val value: String) {
    RESIDENTS("residents"),
    LEASING_OFFICE("leasingOffice"),
    PROMOTIONS("promotions"),
    SELF_TOUR("selftour"),
    VACANCIES("vacancies"),
    KEYS("keys");
}
