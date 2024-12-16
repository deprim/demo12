package org.example.demo12;


import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Employee {
    private final int id;
    private final StringProperty name;
    private final StringProperty position;
    private final StringProperty contact;

    /**
     * Конструктор для создания нового объекта Employee.
     *
     * @param id       Идентификатор сотрудника.
     * @param name     Имя сотрудника.
     * @param position Должность сотрудника.
     * @param contact  Контактная информация сотрудника.
     */
    public Employee(int id, String name, String position, String contact) {
        this.id = id;
        this.name = new SimpleStringProperty(name);
        this.position = new SimpleStringProperty(position);
        this.contact = new SimpleStringProperty(contact);
    }

    // Геттеры
    public int getId() {
        return id;
    }

    public String getName() {
        return name.get();
    }

    public String getPosition() {
        return position.get();
    }

    public String getContact() {
        return contact.get();
    }

    // Свойства для таблиц JavaFX
    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty positionProperty() {
        return position;
    }

    public StringProperty contactProperty() {
        return contact;
    }

    // Сеттеры
    public void setName(String name) {
        this.name.set(name);
    }

    public void setPosition(String position) {
        this.position.set(position);
    }

    public void setContact(String contact) {
        this.contact.set(contact);
    }

    @Override
    public String toString() {
        return name.get();
    }
}
