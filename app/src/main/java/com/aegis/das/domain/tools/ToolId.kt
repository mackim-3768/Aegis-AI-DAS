package com.aegis.das.domain.tools

enum class ToolId(
    val kind: ToolKind,
    val toolName: String
) {
    GET_DRIVER_DROWSINESS_STATUS(ToolKind.CONTEXT, "get_driver_drowsiness_status"),
    GET_STEERING_GRIP_STATUS(ToolKind.CONTEXT, "get_steering_grip_status"),
    GET_DRIVER_VITAL_SIGNS(ToolKind.CONTEXT, "get_driver_vital_signs"),
    GET_DRIVER_GAZE_DIRECTION(ToolKind.CONTEXT, "get_driver_gaze_direction"),
    GET_DRIVER_STRESS_INDEX(ToolKind.CONTEXT, "get_driver_stress_index"),
    GET_VEHICLE_SPEED(ToolKind.CONTEXT, "get_vehicle_speed"),
    GET_LKA_STATUS(ToolKind.CONTEXT, "get_lka_status"),
    GET_DRIVING_DURATION_STATUS(ToolKind.CONTEXT, "get_driving_duration_status"),
    GET_RECENT_WARNING_HISTORY(ToolKind.CONTEXT, "get_recent_warning_history"),
    GET_SENSOR_HEALTH_STATUS(ToolKind.CONTEXT, "get_sensor_health_status"),
    GET_LANE_DEPARTURE_STATUS(ToolKind.CONTEXT, "get_lane_departure_status"),
    GET_FORWARD_COLLISION_RISK(ToolKind.CONTEXT, "get_forward_collision_risk"),
    GET_DRIVING_ENVIRONMENT(ToolKind.CONTEXT, "get_driving_environment"),
    GET_ROAD_SURFACE_FRICTION(ToolKind.CONTEXT, "get_road_surface_friction"),
    GET_BLIND_SPOT_COLLISION_RISK(ToolKind.CONTEXT, "get_blind_spot_collision_risk"),
    GET_EXTERNAL_ENVIRONMENTAL_HAZARDS(ToolKind.CONTEXT, "get_external_environmental_hazards"),
    GET_V2X_TRAFFIC_INFO(ToolKind.CONTEXT, "get_v2x_traffic_info"),
    GET_V2X_EMERGENCY_VEHICLE_PROXIMITY(ToolKind.CONTEXT, "get_v2x_emergency_vehicle_proximity"),
    GET_REAR_OCCUPANT_STATUS(ToolKind.CONTEXT, "get_rear_occupant_status"),
    GET_PASSENGER_SEAT_OCCUPANCY(ToolKind.CONTEXT, "get_passenger_seat_occupancy"),
    GET_CABIN_AIR_QUALITY(ToolKind.CONTEXT, "get_cabin_air_quality"),
    GET_CABIN_CO2_CONCENTRATION(ToolKind.CONTEXT, "get_cabin_co2_concentration"),
    GET_EV_BATTERY_THERMAL_STATUS(ToolKind.CONTEXT, "get_ev_battery_thermal_status"),
    GET_TRAILER_SWAY_STATUS(ToolKind.CONTEXT, "get_trailer_sway_status"),
    GET_VEHICLE_SYSTEM_INTRUSION_STATUS(ToolKind.CONTEXT, "get_vehicle_system_intrusion_status"),

    TRIGGER_STEERING_VIBRATION(ToolKind.ACTION, "trigger_steering_vibration"),
    TRIGGER_DROWSINESS_ALERT_SOUND(ToolKind.ACTION, "trigger_drowsiness_alert_sound"),
    TRIGGER_CLUSTER_VISUAL_WARNING(ToolKind.ACTION, "trigger_cluster_visual_warning"),
    TRIGGER_HUD_WARNING(ToolKind.ACTION, "trigger_hud_warning"),
    TRIGGER_VOICE_PROMPT(ToolKind.ACTION, "trigger_voice_prompt"),
    UPDATE_AMBIENT_MOOD_LIGHTING(ToolKind.ACTION, "update_ambient_mood_lighting"),
    RELEASE_REFRESHING_SCENT(ToolKind.ACTION, "release_refreshing_scent"),
    ESCALATE_WARNING_LEVEL(ToolKind.ACTION, "escalate_warning_level"),
    REQUEST_SAFE_MODE(ToolKind.ACTION, "request_safe_mode"),
    LOG_SAFETY_EVENT(ToolKind.ACTION, "log_safety_event"),
    TRIGGER_NAVIGATION_NOTIFICATION(ToolKind.ACTION, "trigger_navigation_notification"),
    TRIGGER_REST_RECOMMENDATION(ToolKind.ACTION, "trigger_rest_recommendation"),
    PRE_TENSION_SAFETY_BELTS(ToolKind.ACTION, "pre_tension_safety_belts"),
    ACTIVATE_HAZARD_WARNING_SIGNALS(ToolKind.ACTION, "activate_hazard_warning_signals"),
    EXECUTE_EMERGENCY_STOP_PULL_OVER(ToolKind.ACTION, "execute_emergency_stop_pull_over"),
    ADJUST_SEAT_BOLSTER_FIRMNESS(ToolKind.ACTION, "adjust_seat_bolster_firmness"),
    CONTROL_WINDOW_AND_SUNROOF(ToolKind.ACTION, "control_window_and_sunroof"),
    TRIGGER_EMERGENCY_CALL(ToolKind.ACTION, "trigger_emergency_call"),
    TRIGGER_GROUND_PROJECTION_WARNING(ToolKind.ACTION, "trigger_ground_projection_warning"),
    EMIT_EXTERNAL_PEDESTRIAN_ALERT(ToolKind.ACTION, "emit_external_pedestrian_alert"),
    DEPLOY_ACTIVE_PEDESTRIAN_PROTECTION(ToolKind.ACTION, "deploy_active_pedestrian_protection"),
    ACTIVATE_CABIN_PURIFICATION(ToolKind.ACTION, "activate_cabin_purification"),
    SWITCH_CABIN_AIR_CIRCULATION(ToolKind.ACTION, "switch_cabin_air_circulation"),
    ACTIVATE_WELLNESS_MASSAGE(ToolKind.ACTION, "activate_wellness_massage"),
    SET_VALET_MODE_LIMITATIONS(ToolKind.ACTION, "set_valet_mode_limitations");

    companion object {
        fun fromName(name: String): ToolId? = values().firstOrNull { it.toolName == name }

        val contextTools: List<ToolId> = values().filter { it.kind == ToolKind.CONTEXT }
        val actionTools: List<ToolId> = values().filter { it.kind == ToolKind.ACTION }
    }
}
