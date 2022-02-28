package ru.tinkoff.piapi.robot.db.repositories;

public interface ErrorsRepository {

    void add(String exceptionType, String message);
}
