package com.example.demo.service;

import java.util.List;
import com.example.demo.dto.request.StudentRegistrationRequestDto;
import com.example.demo.dto.response.StudentRegistrationResponse;
import com.example.demo.entity.StudentRegistrationRequest.Status;

public interface StudentRegistrationService {
    StudentRegistrationResponse submit(StudentRegistrationRequestDto request);
    List<StudentRegistrationResponse> list(Status status, Integer academicYearId, String department,
            String rollId, String name);
    StudentRegistrationResponse get(Integer id);
    StudentRegistrationResponse approve(Integer id);
    StudentRegistrationResponse reject(Integer id, String reason);
}
