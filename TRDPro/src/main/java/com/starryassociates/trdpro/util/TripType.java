package com.starryassociates.trdpro.util;

public enum TripType {
    ENTITLEMENT_TRAVEL("ENTITLEMENT TRAVEL", true),
    ET_EXTENDED_TDY_14_PLUS_DAYS("ET-EXTENDED TDY-14+ DAYS", true),
    ET_ESCORT_TRAVEL("ET-Escort Travel", true),
    FT_FOREIGN_TRAVEL("FT-FOREIGN TRAVEL", true),
    IN_INVITATIONAL_TRAV("IN-INVITATIONAL TRAV", true),
    INVITATIONAL_TRAVEL("INVITATIONAL TRAVEL", true),
    IPA_TRAVEL("IPA - Travel", true),
    LT_LONG_TERM_DETAIL("LT - Long Term Detail", true),
    NC_NO_COST_AUTH("NC-NO COST AUTH", true),
    NEPG_NO_COST_TO_GOVT("NEPG - NO COST TO GOV'T", true),
    NO_COST_AUTH("NO COST AUTH", true),
    NO_COST_LOA("NO COST LOA", true),
    PT_PATIENT_TRAVEL("PT-PATIENT TRAVEL", true),
    SINGLE_TRIP("SINGLE TRIP", true),
    SP_SPONSOR("SP-SPONSOR", true),
    SP_SPONSORED("SP-SPONSORED", true),
    SPONSORED("SPONSORED", true),
    TD_TEMP_DUTY_TRAVEL("TD - Temp Duty Travel", true),
    TD_SINGLE_TRIP_TDY("TD-SINGLE TRIP (TDY)", true),
    TEMPORARY_DUTY_TDY("TEMPORARY DUTY (TDY)", true),
    TRIP_BY_TR("TRIP BY TR", true),
    TEMPLATE("Template", false),
    SINGLE_TRIP_TDY("SINGLE TRIP (TDY)", true);

    private final String displayName;
    private boolean enabled;

    TripType(String displayName, boolean enabled) {
        this.displayName = displayName;
        this.enabled = enabled;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
