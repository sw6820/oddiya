package com.oddiya.plan.dto;

import com.oddiya.plan.entity.PlanDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanDetailResponse {
    private Long id;
    private Integer day;
    private String location;
    private String activity;

    public static PlanDetailResponse fromEntity(PlanDetail detail) {
        return PlanDetailResponse.builder()
                .id(detail.getId())
                .day(detail.getDay())
                .location(detail.getLocation())
                .activity(detail.getActivity())
                .build();
    }
}
