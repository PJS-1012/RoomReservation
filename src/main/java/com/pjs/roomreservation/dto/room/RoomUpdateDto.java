package com.pjs.roomreservation.dto.room;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회의실 정보 변경 DTO")
public class RoomUpdateDto {
    @NotBlank(message = "회의실 이름을 입력하세요")
    @Size(max = 100, message = "회의실 이름을 100자 이하여야 합니다.")
    @Schema(description = "변경될 회의실 이름", example = "Room1")
    private String name;

    @NotBlank(message = "회의실 위치를 입력하세요")
    @Size(max = 100, message = "회의실 위치는 100자 이하여야 합니다.")
    @Schema(description = "회의실 위치", example = "location1")
    private String location;

    @Min(value = 1, message = "수용 인원은 1명 이상이어야 합니다.")
    @Schema(description = "회의실 수용 인원", example = "4")
    private int capacity;


    public RoomUpdateDto() {}

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public int getCapacity() {
        return capacity;
    }
}
