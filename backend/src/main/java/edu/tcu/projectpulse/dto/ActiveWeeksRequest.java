package edu.tcu.projectpulse.dto;

import java.util.Set;

public record ActiveWeeksRequest(
        Set<Integer> inactiveWeekNumbers
) {
}
