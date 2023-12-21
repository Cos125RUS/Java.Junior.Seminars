package ru.gb.lesson2.hw;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TestProcessor {

    /**
     * Данный метод находит все void методы без аргументов в классе, и запускеет их.
     * <p>
     * Для запуска создается тестовый объект с помощью конструткора без аргументов.
     */
    public static void runTest(Class<?> testClass) {
        final Constructor<?> declaredConstructor;
        try {
            declaredConstructor = testClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Для класса \"" + testClass.getName() + "\" не найден конструктор без аргументов");
        }

        final Object testObj;
        try {
            testObj = declaredConstructor.newInstance();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Не удалось создать объект класса \"" + testClass.getName() + "\"");
        }

        List<Method> methods = new ArrayList<>();
        Method beforeEach = null, afterEach = null;
        for (Method method : testClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(BeforeEach.class)) {
                checkTestMethod(method);
                beforeEach = method;
            } else if (method.isAnnotationPresent(AfterEach.class))
                afterEach = method;
            else if (method.isAnnotationPresent(Test.class) && !method.isAnnotationPresent(Skip.class)) {
                checkTestMethod(method);
                methods.add(method);
            }
        }
        methods.sort((o1, o2) -> {
            if (o1.getAnnotation(Test.class).order() > o2.getAnnotation(Test.class).order())
                return 1;
            if (o1.getAnnotation(Test.class).order() < o2.getAnnotation(Test.class).order())
                return -1;
            return 0;
        });

        Method finalBeforeEach = beforeEach;
        Method finalAfterEach = afterEach;
        methods.forEach(it -> runTest(it, testObj, finalBeforeEach, finalAfterEach));
    }

    private static void checkTestMethod(Method method) {
        if (!method.getReturnType().isAssignableFrom(void.class) || method.getParameterCount() != 0) {
            throw new IllegalArgumentException("Метод \"" + method.getName() + "\" должен быть void и не иметь аргументов");
        }
    }

    private static void runTest(Method testMethod, Object testObj) {
        try {
            testMethod.invoke(testObj);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Не удалось запустить тестовый метод \"" + testMethod.getName() + "\"");
        } catch (AssertionError e) {

        }
    }

    private static void runTest(Method testMethod, Object testObj, Method beforMethod,
                                Method afterMethod) {
        if (beforMethod == null && afterMethod == null)
            runTest(testMethod, testObj);
        else if (afterMethod == null) {
            runTest(beforMethod, testObj);
            runTest(testMethod, testObj);
        } else if (beforMethod == null){
            runTest(testMethod, testObj);
            runTest(afterMethod, testObj);
        } else {
            runTest(beforMethod, testObj);
            runTest(testMethod, testObj);
            runTest(afterMethod, testObj);
        }
    }
}
