package com.socialnetwork.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaptchaDto {

    private String secret; //Секретный код, который нужно ввести
    private String image; //Base64 строка с изображение капчи
}
