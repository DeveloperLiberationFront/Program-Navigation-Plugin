package dataTool;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class TestSwt{
private Shell shell;

public TestSwt() {
    Display display = new Display();
    shell = new Shell(display, SWT.SHELL_TRIM);
    shell.setSize(200, 400);
    

    
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 3;
    shell.setLayout(gridLayout);
    System.out.println("Width:" + shell.getBounds().width);
    Button button = new Button(shell, SWT.PUSH);
    button.setText("B1");
    System.out.println("Bounds: " + button.getBounds());
    System.out.println("computeSize: " + button.computeSize(100, SWT.DEFAULT));
    System.out.println("computeSize: " + button.computeSize(40, SWT.DEFAULT));
    System.out.println("computeSize: " + button.computeSize(SWT.DEFAULT, 100));
    System.out.println("computeSize: " + button.computeSize(SWT.DEFAULT, 20));
    System.out.println("computeSize: " + button.computeSize(SWT.DEFAULT, 15));
    System.out.println("computeSize: " + button.computeSize(100, 200));
    System.out.println("computeSize(Default): " + button.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    System.out.println("Button Size:" + button.getBounds().width);
    
    System.out.println("Width:" + shell.getBounds().width);
    new Button(shell, SWT.PUSH).setText("Wide Button 2");
    
    System.out.println("Width:" + shell.getBounds().width);
    new Button(shell, SWT.PUSH).setText("Button 3");
    
    System.out.println("Width:" + shell.getBounds().width);
    new Button(shell, SWT.PUSH).setText("B4");
    
    System.out.println("Width:" + shell.getBounds().width);
    new Button(shell, SWT.PUSH).setText("Button 5");
    
    
    System.out.println("Width:" + shell.getBounds().width);
    shell.open();

//    System.out.println("Button Size:" + b1.getBounds().width);
    
    while(!shell.isDisposed()) {
        if(!display.readAndDispatch()) display.sleep();
    }

    display.dispose();
}

public static void main(String[] args) {
    new TestSwt();
}

}