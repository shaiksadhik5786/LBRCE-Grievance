package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.ApiDtos.AssignmentRequest;

class ComplaintWorkflowControllerContractTest {
    @Test
    void studentCreateOnlyBindsSubjectDescriptionAndOptionalFile() throws Exception {
        Method create = ComplaintWorkflowController.class.getMethod("create", Authentication.class,
                String.class, String.class, MultipartFile.class);

        assertEquals(4, create.getParameterCount());
        assertFalse(create.getParameters()[0].isAnnotationPresent(RequestParam.class));
        assertFalse(create.getParameters()[0].isAnnotationPresent(RequestPart.class));
        assertTrue(create.getParameters()[1].isAnnotationPresent(RequestParam.class));
        assertTrue(create.getParameters()[2].isAnnotationPresent(RequestParam.class));
        assertTrue(create.getParameters()[3].isAnnotationPresent(RequestPart.class));
        assertEquals("hasRole('STUDENT')", create.getAnnotation(PreAuthorize.class).value());
    }

    @Test
    void classificationAndAssignmentRemainAdminOnly() throws Exception {
        Method assign = ComplaintWorkflowController.class.getMethod("assign", Integer.class,
                Authentication.class, AssignmentRequest.class);

        assertEquals("hasRole('ADMIN')", assign.getAnnotation(PreAuthorize.class).value());
        assertTrue(Arrays.stream(assign.getParameterAnnotations())
                .flatMap(Arrays::stream)
                .noneMatch(annotation -> annotation.annotationType() == RequestParam.class
                        || annotation.annotationType() == RequestPart.class));
    }
}
