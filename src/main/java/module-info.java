module edu.scu.csen275.smartgarden {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens edu.scu.csen275.smartgarden to javafx.fxml;
    opens edu.scu.csen275.smartgarden.controller to javafx.fxml;
    opens edu.scu.csen275.smartgarden.model to javafx.base;
    opens edu.scu.csen275.smartgarden.ui to javafx.fxml;
    
    exports edu.scu.csen275.smartgarden;
    exports edu.scu.csen275.smartgarden.controller;
    exports edu.scu.csen275.smartgarden.model;
    exports edu.scu.csen275.smartgarden.system;
    exports edu.scu.csen275.smartgarden.simulation;
    exports edu.scu.csen275.smartgarden.util;
    exports edu.scu.csen275.smartgarden.ui;
}

