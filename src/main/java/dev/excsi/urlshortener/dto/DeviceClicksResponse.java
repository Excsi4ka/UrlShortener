package dev.excsi.urlshortener.dto;

import dev.excsi.urlshortener.entity.DeviceType;

public record DeviceClicksResponse(DeviceType deviceType, int clicks) {
}
