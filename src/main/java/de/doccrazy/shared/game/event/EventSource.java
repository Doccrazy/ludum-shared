package de.doccrazy.shared.game.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EventSource {
    private final List<Event> eventQueue = new ArrayList<>();

    public void postEvent(Event event) {
        eventQueue.add(event);
    }

    public void pollEvents(Class<? extends Event> type, Consumer<? super Event> consumer) {
        eventQueue.removeIf(new Predicate<Event>() {
            @Override
            public boolean test(Event event) {
                if (type.isInstance(event)) {
                    consumer.accept(event);
                    return true;
                }
                return false;
            }
        });
    }
}
