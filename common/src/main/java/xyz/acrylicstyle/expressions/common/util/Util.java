package xyz.acrylicstyle.expressions.common.util;

import org.jetbrains.annotations.NotNull;
import xyz.acrylicstyle.util.expression.CompileData;
import xyz.acrylicstyle.util.expression.ExpressionParser;
import xyz.acrylicstyle.util.expression.instruction.DummyInstTypeInfo;
import xyz.acrylicstyle.util.expression.instruction.Instruction;
import xyz.acrylicstyle.util.expression.instruction.InstructionSet;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

public class Util {
    public static @NotNull Stream<String> getSuggestionsFull(@NotNull Stream<String> variables, @NotNull CompileData compileData, @NotNull String input) throws Exception {
        if (!input.contains(".")) {
            return variables;
        }
        InstructionSet instructionSet = ExpressionParser.compile(input.substring(0, input.lastIndexOf('.')), compileData);
        Instruction instruction = instructionSet.lastOrNull();
        if (instruction instanceof DummyInstTypeInfo) {
            DummyInstTypeInfo typeInfo = (DummyInstTypeInfo) instruction;
            return getTokens(typeInfo.getClazz()).map(s -> input.substring(0, input.lastIndexOf('.') + 1) + s);
        }
        return Stream.empty();
    }

    public static @NotNull Stream<String> getSuggestionsPartial(@NotNull Stream<String> variables, @NotNull CompileData compileData, @NotNull String input) throws Exception {
        if (!input.contains(".")) {
            return variables.filter(s -> s.startsWith(input));
        }
        String token = input.substring(input.lastIndexOf('.') + 1);
        String[] args = input.split(" ");
        String last = args[args.length - 1];
        InstructionSet instructionSet = ExpressionParser.compile(input.substring(0, input.lastIndexOf('.')), compileData);
        Instruction instruction = instructionSet.lastOrNull();
        if (instruction instanceof DummyInstTypeInfo) {
            DummyInstTypeInfo typeInfo = (DummyInstTypeInfo) instruction;
            return getTokens(typeInfo.getClazz())
                    .filter(s -> s.toLowerCase(Locale.ROOT).startsWith(token.toLowerCase(Locale.ROOT)))
                    .map(s -> last.substring(0, last.lastIndexOf('.') + 1) + s);
        }
        return Stream.empty();
    }

    private static Stream<String> getTokens(@NotNull Class<?> type) {
        List<String> tokens = new ArrayList<>();
        for (Class<?> clazz : getSupers(type)) {
            for (Field field : clazz.getDeclaredFields()) {
                tokens.add(field.getName());
            }
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().length() >= 4
                        && method.getName().startsWith("get")
                        && Character.isUpperCase(method.getName().charAt(3))
                        && method.getParameterCount() == 0) {
                    tokens.add(method.getName().substring(3, 4).toLowerCase(Locale.ROOT) + method.getName().substring(4));
                }
                if (method.getParameterCount() == 0) {
                    tokens.add(method.getName() + "()");
                    tokens.add(method.getName());
                } else {
                    tokens.add(method.getName() + "(");
                }
            }
        }
        return tokens.stream().distinct();
    }

    private static @NotNull Set<@NotNull Class<?>> getSupers(@NotNull Class<?> type) {
        Set<Class<?>> list = new LinkedHashSet<>();
        list.add(type);
        if (type.getSuperclass() != null) {
            list.add(type.getSuperclass());
            list.addAll(getSupers(type.getSuperclass()));
        }
        list.addAll(Arrays.asList(type.getInterfaces()));
        for (Class<?> anInterface : type.getInterfaces()) {
            list.addAll(getSupers(anInterface));
        }
        return list;
    }
}
