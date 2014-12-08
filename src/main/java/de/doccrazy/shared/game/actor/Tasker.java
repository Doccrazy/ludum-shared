package de.doccrazy.shared.game.actor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Simple task runner with functional interface; supports task chaining
 */
public class Tasker {
    private List<TaskDef> tasks = new ArrayList<>();

    /**
     * Run action every x seconds
     */
    public TaskDef every(float secs, Consumer<Void> action) {
        TaskDef def = new TaskDef(secs, action);
        tasks.add(def);
        return def;
    }

    /**
     * Run action once after x seconds
     */
    public OnceTaskDef in(float secs, Consumer<Void> action) {
        OnceTaskDef def = new OnceTaskDef(secs, action);
        tasks.add(def);
        return def;
    }

    /**
     * Run action continously during x seconds, passing the elapsed time
     */
    public OnceTaskDef during(float secs, Consumer<Float> action) {
        OnceTaskDef def = new OnceTaskDef(secs, null);
        def.continuousFunc = action;
        tasks.add(def);
        return def;
    }

    /**
     * Do nothing for x seconds (for chaining)
     */
    public OnceTaskDef wait(float secs) {
        return in(secs, Void -> {});
    }

    void update(float delta) {
        for (TaskDef task : tasks) {
            task.update(delta);
        }
        for (Iterator<TaskDef> it = tasks.iterator(); it.hasNext(); ) {
            TaskDef task = it.next();
            if (task.done) {
                it.remove();
            }
        }
    }


    public static class TaskDef {
        Consumer<Void> function;
        float time = 0f;
        float interval;
        boolean done;

        TaskDef(float interval, Consumer<Void> function) {
            super();
            this.interval = interval;
            this.function = function;
        }

        void update(float delta) {
            time += delta;
            afterIncTime();
            while (time > interval) {
                if (done) {
                    return;
                }
                exec();
                time -= interval;
            }
        }

        protected void afterIncTime() {
        }

        protected void exec() {
            if (function != null) {
                function.accept(null);
            }
        }

        public void done() {
            done = true;
        }
    }

    public static class OnceTaskDef extends TaskDef {
        TaskDef follow;
        private Consumer<Float> continuousFunc;
        boolean noDone;  //hack

        OnceTaskDef(float interval, Consumer<Void> function) {
            super(interval, function);
        }

        @Override
        protected void exec() {
            super.exec();
            if (follow == null) {
                if (!noDone) {
                    done = true;
                }
            } else {
                function = follow.function;
                time = 0f;
                interval = follow.interval;
                if (follow instanceof OnceTaskDef) {
                    continuousFunc = ((OnceTaskDef)follow).continuousFunc;
                    follow = ((OnceTaskDef)follow).follow;
                    noDone = false;
                } else {
                    continuousFunc = null;
                    follow = null;
                    noDone = true;   //followup is a regular task, do not stop after first exec()
                }
            }
        }

        @Override
        protected void afterIncTime() {
            if (!done && continuousFunc != null) {
                continuousFunc.accept(Math.min(time, interval));
            }
        }

        /**
         * After this finishes, run action every x seconds
         */
        public TaskDef thenEvery(float secs, Consumer<Void> action) {
            follow = new TaskDef(secs, action);
            return follow;
        }

        /**
         * After this finishes, run action once after x seconds
         */
        public OnceTaskDef then(float secs, Consumer<Void> action) {
            follow = new OnceTaskDef(secs, action);
            return (OnceTaskDef)follow;
        }

        /**
         * After this finishes, run action continously during x seconds, passing the elapsed time
         */
        public OnceTaskDef thenDuring(float secs, Consumer<Float> action) {
            follow = new OnceTaskDef(secs, null);
            ((OnceTaskDef)follow).continuousFunc = action;
            return (OnceTaskDef)follow;
        }

        /**
         * After this finishes, do nothing for x seconds (for chaining)
         */
        public OnceTaskDef thenWait(float secs) {
            return then(secs, Void -> {});
        }
    }
}
