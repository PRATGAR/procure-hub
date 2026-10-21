package com.tradepost.procure.admin.service;

import com.tradepost.procure.common.InvalidRequestException;
import java.util.Map;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TaskListener {

    private final Map<String, MaintenanceTask> tasks;

    public TaskListener(Map<String, MaintenanceTask> tasks) {
        this.tasks = tasks;
    }

    @EventListener
    public void onTaskRequested(TaskRequested event) {
        MaintenanceTask task = tasks.get(event.name());
        if (task == null) {
            throw new InvalidRequestException("unknown maintenance task");
        }
        event.affected().set(task.run(event.cutoff()));
    }
}
