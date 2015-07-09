package flow

import groovy.transform.Canonical
import groovyx.javafx.beans.FXBindable
import javafx.beans.property.SimpleStringProperty;

@Canonical
class LoadItem {
    @FXBindable String name
    @FXBindable String color
    @FXBindable String status
}
