package com.example.thandbag.dto.chat.chatroom;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CreateRoomRequestDto {

    private Long pubId;
    private Long subId;

}
