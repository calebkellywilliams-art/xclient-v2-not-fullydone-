package com.example.xclient;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Module {
    public enum Category {
        MOVEMENT("Movement"), COMBAT("Combat"), PLAYER("Player"), RENDER("Render"), CLIENT("Client");
        public final String label;
        Category(String label) { this.label = label; }
    }

    public final String name;
    public final Category category;
    public final List<Setting> settings = new ArrayList<>();
    public boolean enabled;
    public boolean expanded;
    public Consumer<Boolean> onChange;

    public Module(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    public void toggle() { setEnabled(!enabled); }

    public void setEnabled(boolean on) {
        enabled = on;
        if (onChange != null) onChange.accept(on);
    }
}
