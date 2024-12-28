package ru.netology.diploma_project.models.responses;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ListOfFilesResponse {

    List<FileResponse> list = new ArrayList<>();
}
