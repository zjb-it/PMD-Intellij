package com.intellij.plugins.bodhi.pmd.listener;

import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileStatusNotification;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public class CompileNotification implements CompileStatusNotification {

    private Consumer<Integer> consumer;
    private Function<Integer,Integer> function1;

    public CompileNotification(Consumer<Integer> consumer) {
        this.consumer = consumer;
    }

    public CompileNotification(Function<Integer,Integer> function1) {
        this.function1 = function1;
    }

    @Override
    public void finished(boolean aborted, int errors, int warnings, @NotNull CompileContext compileContext) {
        consumer.accept(errors);
        function1.apply(errors);
    }
}
