package dataTool;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class ToolItemDropDownMenu {

  public static void main(String[] args) {
    Display display = new Display();
    Shell shell = new Shell(display);
    ToolBar toolBar = new ToolBar(shell, SWT.BORDER | SWT.VERTICAL);

    ToolItem item = new ToolItem(toolBar, SWT.DROP_DOWN);
    item.setText("One");

    DropdownSelectionListener listenerOne = new DropdownSelectionListener(item);
    listenerOne.add("Option One for One");
    listenerOne.add("Option Two for One");
    listenerOne.add("Option Three for One");
    item.addSelectionListener(listenerOne);

    toolBar.pack();

    shell.pack();
    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
  }
}

class DropdownSelectionListener extends SelectionAdapter {
  private ToolItem dropdown;

  private Menu menu;

  public DropdownSelectionListener(ToolItem dropdown) {
    this.dropdown = dropdown;
    menu = new Menu(dropdown.getParent().getShell());
  }

  public void add(String item) {
    MenuItem menuItem = new MenuItem(menu, SWT.NONE);
    menuItem.setText(item);
    menuItem.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        MenuItem selected = (MenuItem) event.widget;
        dropdown.setText(selected.getText());
      }
    });
  }

  public void widgetSelected(SelectionEvent event) {
    if (event.detail == SWT.ARROW) {
      ToolItem item = (ToolItem) event.widget;
      Rectangle rect = item.getBounds();
      Point pt = item.getParent().toDisplay(new Point(rect.x, rect.y));
      menu.setLocation(pt.x, pt.y + rect.height);
      menu.setVisible(true);
    } else {
      System.out.println(dropdown.getText() + " Pressed");
    }
  }
}