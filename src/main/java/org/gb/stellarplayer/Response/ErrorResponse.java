package org.gb.stellarplayer.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.net.http.HttpResponse;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ErrorResponse {
    private HttpStatus status;
    private String message;

}
