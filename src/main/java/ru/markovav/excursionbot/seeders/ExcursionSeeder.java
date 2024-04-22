package ru.markovav.excursionbot.seeders;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.markovav.excursionbot.models.Hint;
import ru.markovav.excursionbot.models.Route;
import ru.markovav.excursionbot.models.Task;
import ru.markovav.excursionbot.repositories.HintRepository;
import ru.markovav.excursionbot.repositories.RouteRepository;
import ru.markovav.excursionbot.repositories.TaskRepository;

@Component
@RequiredArgsConstructor
public class ExcursionSeeder {
    private final RouteRepository routeRepository;
    private final HintRepository hintRepository;
    private final TaskRepository taskRepository;

    public void seed() {
        makeRoute();
    }

    private void makeHints(Task task) {
        var hint1 = Hint.builder()
                .text("Hint 1")
                .index(1)
                .task(task)
                .build();
        var hint2 = Hint.builder()
                .text("Hint 2")
                .index(2)
                .task(task)
                .build();

        hintRepository.save(hint1);
        hintRepository.save(hint2);
    }

    private void makeTasks(Route route) {
        var task1 = Task.builder()
                .name("Task 1")
                .text("Description 1")
                .index(1)
                .route(route)
                .build();

        var task2 = Task.builder()
                .name("Task 2")
                .text("Description 2")
                .index(2)
                .route(route)
                .build();

        taskRepository.save(task1);
        taskRepository.save(task2);

        makeHints(task2);
    }

    private void makeRoute() {
        var route = Route.builder()
                .name("Route 1")
                .welcome_message("Welcome message")
                .build();

        routeRepository.save(route);

        makeTasks(route);
    }
}
