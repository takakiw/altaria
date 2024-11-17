package com.altaria.space.listener;

import com.altaria.space.mapper.SpaceMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AddNoteCountListener {

    @Autowired
    private SpaceMapper spaceMapper;

    @RabbitListener(queues = "update-note-count-queue")
    public void updateNoteCount(String message) {
        String[] split = message.split(":");
        Long uid = Long.parseLong(split[0]);
        Integer count = Integer.parseInt(split[1]);
        int i = spaceMapper.incrementNoteCount(uid, count);
    }
}