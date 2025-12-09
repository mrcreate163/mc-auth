package com.socialnetwork.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Данные капчи для защиты от ботов")
public class CaptchaDto {

    @Schema(
            description = "Секретный код капчи, который сохраняется на сервере",
            example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
    )
    private String secret; //Секретный код, который нужно ввести

    @Schema(
            description = "Изображение капчи в формате Base64",
            example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA..."
    )
    private String image; //Base64 строка с изображение капчи
}
