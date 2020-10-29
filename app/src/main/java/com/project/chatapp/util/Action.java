package com.project.chatapp.util;

public interface Action<T> {
    void call(T t);
}
