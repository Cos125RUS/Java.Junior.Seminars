package ru.geekbrains.lesson3.task2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ToDoListApp {

    public static final String FILE_JSON = "tasks.json";
    public static final String FILE_BIN = "tasks.bin";
    public static final String FILE_XML = "tasks.xml";

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final XmlMapper xmlMapper = new XmlMapper();


    public static void addNewTask(Scanner scanner, List<ToDo> tasks) {
        System.out.println("Введите название новой задачи:");
        String newTaskTitle = scanner.nextLine();
        tasks.add(new ToDo(newTaskTitle));
        saveTasksToFile(FILE_JSON, tasks);
        saveTasksToFile(FILE_BIN, tasks);
        saveTasksToFile(FILE_XML, tasks);
        System.out.println("Новая задача добавлена.");
    }

    public static void saveTasksToFile(String fileName, List<ToDo> tasks) {
        try {
            if (fileName.endsWith(".json")) {
                objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
                objectMapper.writeValue(new File(fileName), tasks);
            } else if (fileName.endsWith(".bin")) {
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
                    oos.writeObject(tasks);
                }
            } else if (fileName.endsWith(".xml")) {
                //String s = xmlMapper.writeValueAsString(tasks);
                xmlMapper.writeValue(new File(fileName), tasks);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<ToDo> loadTasksFromFile(String fileName) {
        List<ToDo> tasks = new ArrayList<>();

        File file = new File(fileName);
        if (file.exists()) {
            try {
                if (fileName.endsWith(".json")) {
                    tasks = objectMapper.readValue(file, objectMapper.getTypeFactory().constructCollectionType(List.class, ToDo.class));
                } else if (fileName.endsWith(".bin")) {
                    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                        tasks = (List<ToDo>) ois.readObject();
                    }
                } else if (fileName.endsWith(".xml")) {
                    tasks = xmlMapper.readValue(file, xmlMapper.getTypeFactory().constructCollectionType(List.class, ToDo.class));
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return tasks;
    }

    public static void markTaskAsDone(Scanner scanner, List<ToDo> tasks) {
        System.out.println("Введите порядковый номер задачи для отметки как выполненной:");
        String input = scanner.nextLine();

        try {
            int taskNumber = Integer.parseInt(input) - 1;
            if (taskNumber >= 0 && taskNumber < tasks.size()) {
                tasks.get(taskNumber).setDone(true);
                saveTasksToFile(FILE_JSON, tasks);
                saveTasksToFile(FILE_BIN, tasks);
                saveTasksToFile(FILE_XML, tasks);
                System.out.println("Задача отмечена как выполненная.");
            } else {
                System.out.println("Некорректный номер задачи. Попробуйте снова.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Некорректный ввод. Попробуйте снова.");
        }
    }

    public static void displayTasks(List<ToDo> tasks) {
        System.out.println("Список задач:");
        for (int i = 0; i < tasks.size(); i++) {
            ToDo task = tasks.get(i);
            String status = task.isDone() ? "[x]" : "[ ]";
            System.out.println((i + 1) + ". " + status + " " + task.getTitle());
        }
    }

}
