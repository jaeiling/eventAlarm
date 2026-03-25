package com.example.eventalarm.dto;

import jakarta.validation.constraints.NotBlank;

public class DepartmentPageCreateDto {

    @NotBlank(message = "대학교명을 입력해주세요.")
    private String universityName;

    @NotBlank(message = "학과명을 입력해주세요.")
    private String departmentName;

    public String getUniversityName() { return universityName; }
    public void setUniversityName(String universityName) { this.universityName = universityName; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
}
