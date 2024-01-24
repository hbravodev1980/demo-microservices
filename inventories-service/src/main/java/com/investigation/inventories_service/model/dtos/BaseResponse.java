package com.investigation.inventories_service.model.dtos;

import java.util.List;

public record BaseResponse(List<String> errors) {

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

}