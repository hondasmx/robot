package ru.tinkoff.piapi.robot.db.repositories;

public interface ErrorsRepository {

    void add(String exceptionClass, String message);
}
