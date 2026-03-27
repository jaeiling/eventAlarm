package com.example.eventalarm.dto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public class EventCreateDto {

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    private String postType = "EVENT"; // EVENT(행사) / NOTICE(공지)

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime eventDateTime; // 공지글이면 null 가능

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime eventEndDateTime; // 선택 - 여러 날 행사 종료일시

    private String location; // 공지글이면 null 가능

    private String locationAddress;
    private String description;
    private String fee;
    private String bankAccount;
    private String link; // 외부 링크 (선택)

    // 이미지 (최대 5장, 선택)
    private List<MultipartFile> images;

    // 대표 이미지 인덱스 (0-based, 기본 0)
    private int thumbnailIndex = 0;

    // ── Getters & Setters ──

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPostType() { return postType != null ? postType : "EVENT"; }
    public void setPostType(String postType) { this.postType = postType; }

    public LocalDateTime getEventDateTime() { return eventDateTime; }
    public void setEventDateTime(LocalDateTime eventDateTime) { this.eventDateTime = eventDateTime; }

    public LocalDateTime getEventEndDateTime() { return eventEndDateTime; }
    public void setEventEndDateTime(LocalDateTime eventEndDateTime) { this.eventEndDateTime = eventEndDateTime; }

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

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public List<MultipartFile> getImages() { return images; }
    public void setImages(List<MultipartFile> images) { this.images = images; }

    public int getThumbnailIndex() { return thumbnailIndex; }
    public void setThumbnailIndex(int thumbnailIndex) { this.thumbnailIndex = thumbnailIndex; }
}
