package ru.netology.diploma_project.models.entity.errors;

public record UnauthorizedError() {
    public static final String message = "Unauthorized error";
    public static final int id = 2;
}
