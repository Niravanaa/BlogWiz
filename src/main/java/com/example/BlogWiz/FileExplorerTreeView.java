package com.example.BlogWiz;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class FileExplorerTreeView extends TreeView<String> {
    public FileExplorerTreeView() {
        TreeItem<String> rootItem = new TreeItem<>("Root");

        setRoot(rootItem);

        // Initially, populate the tree with a placeholder item
        rootItem.getChildren().add(new TreeItem<>("Loading..."));
    }
}