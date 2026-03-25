package com.example.eventalarm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class EventCreateDto {

    @NotBlank(message = "행사명을 입력해주세요.")
    private String title;

    @NotNull(message = "행사 일시를 입력해주세요.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime eventDateTime;

    @NotBlank(message = "장소를 입력해주세요.")
    private String location;

    private String locationAddress; // 지도 검색용 주소

    private String description;

    private String fee;

    private String bankAccount;

    // ── Getters & Setters ──

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDateTime getEventDateTime() { return eventDateTime; }
    public void setEventDateTime(LocalDateTime eventDateTime) { this.eventDateTime = eventDateTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getLocationAddress() { return locationAddress; }
    public void setLocationAddress(String locationAddress) { this.locationAddress = locationAddress; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFee() { return fee; }
    public void setFee(String fee) { this.fee = fee; }

    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }
}
