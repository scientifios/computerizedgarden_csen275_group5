module CSEN275Garden {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens CSEN275Garden to javafx.fxml;
    opens CSEN275Garden.controller to javafx.fxml;
    opens CSEN275Garden.model to javafx.base;
    opens CSEN275Garden.ui to javafx.fxml;
    
    exports CSEN275Garden;
    exports CSEN275Garden.controller;
    exports CSEN275Garden.model;
    exports CSEN275Garden.system;
    exports CSEN275Garden.simulation;
    exports CSEN275Garden.util;
    exports CSEN275Garden.ui;
}

