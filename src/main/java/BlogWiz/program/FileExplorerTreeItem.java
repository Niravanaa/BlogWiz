package BlogWiz.program;

import javafx.scene.control.TreeItem;

public class FileExplorerTreeItem extends TreeItem<String> {
    public boolean isDirectory;

    public FileExplorerTreeItem(String value, boolean isDirectory) {
        super(value);
        this.isDirectory = isDirectory;
    }
}
