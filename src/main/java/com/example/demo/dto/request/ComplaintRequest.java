package com.example.demo.dto.request;

public class ComplaintRequest {

    private Integer academicYearId;
    private Integer studentId;
    private String complaintType;
    private String subject;
    private String description;
    private String imageName;
    private String imagePath;
    private String imageType;

    public ComplaintRequest() {
    }

    public Integer getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(Integer academicYearId) {
        this.academicYearId = academicYearId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getComplaintType() {
        return complaintType;
    }

    public void setComplaintType(String complaintType) {
        this.complaintType = complaintType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }
}