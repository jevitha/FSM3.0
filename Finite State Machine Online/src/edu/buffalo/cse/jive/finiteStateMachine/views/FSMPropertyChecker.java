package edu.buffalo.cse.jive.finiteStateMachine.views;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;

/*
 * Program: FiniteStateMachine.java
 * Author: Swaminathan J, Amrita University, India
 * Description: This is a Eclipse plug-in that constructs finite state
 * 				machine given execution trace and key variables. The user
 * 				can load the trace and select the key variables of his
 * 				interest. Rendering of the diagram by PlantUML.
 * Execution: Run As ... Eclipse Application
 * Updated By Shashank Raghunath
 * Added Property Validation and Refactored the code
 */

/*
 * Draw little diagram, when you find an error. Till then disable. 
 */

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import edu.buffalo.cse.jive.finiteStateMachine.FSMConstants;
import edu.buffalo.cse.jive.finiteStateMachine.models.Event;
import edu.buffalo.cse.jive.finiteStateMachine.models.InputFileParser;
import edu.buffalo.cse.jive.finiteStateMachine.models.ListenJob;
import edu.buffalo.cse.jive.finiteStateMachine.models.NetworkEventParser;
import edu.buffalo.cse.jive.finiteStateMachine.models.TransitionBuilder;
import edu.buffalo.cse.jive.finiteStateMachine.monitor.Monitor;
import edu.buffalo.cse.jive.finiteStateMachine.monitor.OfflineMonitor;
import edu.buffalo.cse.jive.finiteStateMachine.monitor.OnlineMonitor;
import edu.buffalo.cse.jive.finiteStateMachine.parser.Parser;
import edu.buffalo.cse.jive.finiteStateMachine.parser.TopDownParser;
import edu.buffalo.cse.jive.finiteStateMachine.parser.expression.expression.Expression;
import edu.buffalo.cse.jive.finiteStateMachine.parser.expression.value.StringValueExpression;
import edu.buffalo.cse.jive.finiteStateMachine.parser.expression.value.ValueExpression;
import edu.buffalo.cse.jive.finiteStateMachine.util.FSMUtil;
import edu.buffalo.cse.jive.finiteStateMachine.util.TemporaryDataTransporter;
// import edu.buffalo.cse.jive.finiteStateMachine.views.FSMAbstractionGranularity.State;
import edu.buffalo.cse.jive.finiteStateMachine.models.State;
import edu.buffalo.cse.jive.finiteStateMachine.monitor.OnlineMonitor;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

/**
 * @author Shashank Raghunath
 * @email sraghuna@buffalo.edu
 * 
 * Feature - Online verification 
 * @author Jevitha K P
 * @email jevitha@gmail.com
 * 
 */
/**
 * The view of the FSM.
 *
 */
public class FSMPropertyChecker extends ViewPart {
//public class FSMPropertyChecker {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "edu.buffalo.cse.jive.finiteStateMachine.views.FiniteStateMachine";
	public static Label errorText;

	private IStatusLineManager statusLineManager;
	private Display display;
	private ScrolledComposite rootScrollComposite;
	private Composite mainComposite;
	private Label fileLabel;
	private Label propertyFileLabel;
	private Text fileText;
	private Text propertyFileText;
	private Combo attributeList;
	private Button browseButton;
	private Button propertyButton;
	private Button validateButton;
	private Button exportButton;
	private Composite imageComposite;
	private Image image;
	public boolean horizontal;
	public boolean vertical;

	private Label kvLabel;
	private Text kvText;
	private Label paLabel;
	private Text paText;

	// For abstraction
	private Label absLabel;
	private Text absText;
	private Label absSpace;
	private Label absSyntax;
	// For abstraction

	private Button addButton;
	private Button resetButton;
	private Button drawButton;
	private Button transitionCount;
	private int count;

	private Label kvSyntax;
	private Label kvSpace;

	private Browser browser; // For svg support
	private Label canvasLabel;
	private Label byLabel;
	private Label statusLabel;
	private Text hcanvasText;
	private Text vcanvasText;
	private Label propertyLabel;
	private Text propertyText;
	private BlockingQueue<Event> incomingEvents;
	private SvgGenerator svgGenerator;
	private TransitionBuilder transitionBuilder;

	private Monitor monitor;
	private Button ssChkBox;
	private Button startButton;
	private Button prevButton;
	private Button nextButton;
	private Button endButton;
	private Button startButton2;
	private Button prevButton2;
	private Button nextButton2;
	private Button endButton2;

	private Button[] granularity;
	private Label grLabel;
	
	private Button[] abstractionType;
	private Label absTypeLabel;

	private boolean dataAbstraction = true;
	
	//	private Job job;
	//	private ServerSocket server;
	/*
	private Label portLabel;
	private Text portText;
	private Button listenButton;
	private Button runtimeModelButton;

	private NetworkEventParser networkEventParser;
	*/
	//inserted by jevitha
	// uncomment the below constructor and main funciton lines to enable stand-alone version of property checker 
	//public FSMPropertyChecker() {

	//public static void main(String[] args) {
	public void createIntegratedPartControl(Composite parent) {
//		Display display = new Display(); // uncomment this line for stand-alone version
		display = parent.getDisplay(); // comment this line for stand-alone version
//		Shell shell = new Shell(display);
//		shell.setLayout(new FillLayout());
//		shell.setText("JIVE Property Checker");
		
//		final TabFolder tabFolder = new TabFolder(shell, SWT.BORDER|SWT.BOTTOM);
		
	//	final TabFolder tabFolder = new TabFolder(shell, SWT.BORDER);
		final TabFolder tabFolder = new TabFolder(parent, SWT.BORDER);
		TabItem item = new TabItem(tabFolder, SWT.NONE);
		item.setText("Offline Verification");
		item.setToolTipText("This tab provides options for Offline Verification using JIVE Events log file ");
		Composite offlineComposite = new Composite(tabFolder, SWT.NONE);
		item.setControl(offlineComposite);
		
		TabItem item1 = new TabItem(tabFolder, SWT.NONE);
		item1.setText("Online Verification");
		item1.setToolTipText("This tab provides options for Online Verification by listening for JIVE Events on a specific port");
		Composite onlineComposite = new Composite(tabFolder, SWT.NONE);
		item1.setControl(onlineComposite);
		tabFolder.setSelection(item1);
		
		createOfflineControl(offlineComposite);
		createOnlineControl(onlineComposite);
//		FSMPropertyChecker fsm = new FSMPropertyChecker();
//		fsm.createOfflineControl(offlineComposite);
//		fsm.createOnlineControl(onlineComposite);
		
//		shell.setSize(800,600);
//		shell.pack();
//		shell.open();
//		while (!shell.isDisposed()) {
//			if (!display.readAndDispatch()) {
//				display.sleep();
//			}
//		}
//		display.dispose();
	}

	private void createOnlineControl(Composite onlineComposite) {
		new FSMOnlinePropertyChecker().createControls(onlineComposite);
		
	}
	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 * It's Eclipse SWT 3.x! Deal with it. As I did :D
	 */
	public void createPartControl(Composite parent) {
		createIntegratedPartControl(parent);
		
	}
	
	public void createOfflineControl(Composite parent) {
		//statusLineManager = getViewSite().getActionBars().getStatusLineManager();
		statusLineManager = new StatusLineManager();
		display = parent.getDisplay();

		GridLayout layoutParent = new GridLayout();
		layoutParent.numColumns = 1;
		parent.setLayout(layoutParent);

		rootScrollComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL );
		rootScrollComposite.setLayout(new GridLayout(2, false));
		rootScrollComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		rootScrollComposite.setExpandHorizontal(true);
		rootScrollComposite.setExpandVertical(true);
		rootScrollComposite.setAlwaysShowScrollBars(true);


		mainComposite = new Composite(rootScrollComposite, SWT.NONE);
		rootScrollComposite.setContent(mainComposite);

		mainComposite.setLayout(new GridLayout(1, false));
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		//mainComposite.setSize(mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		/*
		Composite listenComposite = new Composite(mainComposite, SWT.NONE);
		listenComposite.setLayout(new GridLayout(4, false));
		listenComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		listenButton = new Button(listenComposite, SWT.PUSH);
		listenButton.setText("Listen");

		runtimeModelButton = new Button(listenComposite, SWT.PUSH);
		runtimeModelButton.setText("View Runtime Model");

		portLabel = new Label(listenComposite, SWT.FILL);
		portLabel.setText("Port Number : ");

		portText = new Text(listenComposite, SWT.FILL);
		portText.setText("5000");
		GridData portGd = new GridData();
		portGd.widthHint = 300;
		portText.setLayoutData(portGd); 
		*/

		Composite browseComposite = new Composite(mainComposite, SWT.NONE);
		browseComposite.setLayout(new GridLayout(4, false));
		browseComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		browseButton = new Button(browseComposite, SWT.PUSH);
		browseButton.setText("Browse ");

		fileLabel = new Label(browseComposite, SWT.FILL);
		fileLabel.setText("CSV File : ");

		fileText = new Text(browseComposite, SWT.READ_ONLY);
		fileText.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
		GridData gd = new GridData();
		gd.widthHint = 550;
		fileText.setLayoutData(gd);

		// Key Attributes Composite

		Composite kvComposite = new Composite(mainComposite, SWT.NONE);
		kvComposite.setLayout(new GridLayout(2, false));
		kvComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		kvLabel = new Label(kvComposite, SWT.FILL);
		kvLabel.setText("Key Attributes");

		kvText = new Text(kvComposite, SWT.BORDER | SWT.FILL);
		GridData gd5 = new GridData();
		gd5.widthHint = 800;
		kvText.setLayoutData(gd5);

		kvSpace = new Label(kvComposite, SWT.FILL);
		kvSpace.setText("                     ");

		kvSyntax = new Label(kvComposite, SWT.FILL);
		kvSyntax.setText("   class:index->field,......,class:index->field");

//		// Choice composite
//		Composite evComposite0 = new Composite(mainComposite, SWT.NONE);
//		evComposite0.setLayout(new GridLayout(8, false));
//		evComposite0.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));


		// Draw composite
		Composite evComposite = new Composite(mainComposite, SWT.NONE);
		evComposite.setLayout(new GridLayout(11, false));
		evComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		 attributeList = new Combo(evComposite, SWT.SIMPLE | SWT.BORDER);
//		attributeList = new Combo(evComposite0, SWT.DROP_DOWN | SWT.BORDER | SWT.H_SCROLL );
		//		GridData gd6 = new GridData();
		//		gd6.widthHint = 300;
		//		attributeList.setLayoutData(gd6);
		attributeList.add("										");

//		addButton = new Button(evComposite0, SWT.PUSH);
		addButton = new Button(evComposite, SWT.PUSH);
		addButton.setText("Add");
		addButton.setToolTipText("Adds the key attribute selected");

//		resetButton = new Button(evComposite0, SWT.PUSH);
		resetButton = new Button(evComposite, SWT.PUSH);
		resetButton.setText("Reset");
		resetButton.setToolTipText("Clears the key attributes");


//		drawButton = new Button(evComposite0, SWT.PUSH);
		drawButton = new Button(evComposite, SWT.PUSH);
		drawButton.setText("Draw");
		drawButton.setToolTipText("Draws the state diagram");

//		validateButton = new Button(evComposite0, SWT.PUSH);
		validateButton = new Button(evComposite, SWT.PUSH);
		validateButton.setText("Validate");
		validateButton.setToolTipText("Validates and draws the state diagram");


//		exportButton = new Button(evComposite0, SWT.PUSH);
		exportButton = new Button(evComposite, SWT.PUSH);
		exportButton.setText("Export");
		exportButton.setToolTipText("Exports the state diagram");

		ssChkBox = new Button(evComposite, SWT.CHECK);
		ssChkBox.setSelection(false);
		ssChkBox.setText("Step-by-step");

		startButton = new Button(evComposite, SWT.PUSH);
		startButton.setText("Start");
		startButton.setToolTipText("Start state");
		startButton.setEnabled(false);

		prevButton = new Button(evComposite, SWT.PUSH);
		prevButton.setText("Prev");
		prevButton.setToolTipText("Previous state");
		prevButton.setEnabled(false);

		nextButton = new Button(evComposite, SWT.PUSH);
		nextButton.setText("Next");
		nextButton.setToolTipText("Next state");
		nextButton.setEnabled(false);

		endButton = new Button(evComposite, SWT.PUSH);
		endButton.setText(" End ");
		endButton.setToolTipText("End state");
		endButton.setEnabled(false);

		// Granularity composite
		Composite grComposite = new Composite(mainComposite, SWT.NONE);
		grComposite.setLayout(new GridLayout(10, false));
		grComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));


		grLabel = new Label(grComposite, SWT.FILL);
		grLabel.setText("Granularity");

		granularity = new Button[2];

		granularity[0] = new Button(grComposite, SWT.RADIO);
		granularity[0].setSelection(true);
		granularity[0].setText("Field");

		granularity[1] = new Button(grComposite, SWT.RADIO);
		granularity[1].setSelection(false);
		granularity[1].setText("Method");

		transitionCount = new Button(grComposite, SWT.CHECK);
		transitionCount.setSelection(true);
		transitionCount.setText("Count transitions");
		
		Composite paComposite = new Composite(mainComposite, SWT.NONE);
		paComposite.setLayout(new GridLayout(2, false));
		paComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		paLabel = new Label(paComposite, SWT.FILL);
		paLabel.setText("Abbreviations");

		paText = new Text(paComposite, SWT.BORDER | SWT.FILL);
		GridData gd5b = new GridData();
		gd5b.widthHint = 800;
		paText.setLayoutData(gd5b);
		
		// Abstraction composite
		Composite absComposite = new Composite(mainComposite, SWT.NONE);
		absComposite.setLayout(new GridLayout(5, false));
		absComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		absTypeLabel = new Label(absComposite, SWT.FILL);
		absTypeLabel.setText("Abstraction Type");
						
		abstractionType = new Button[2];

		abstractionType[0] = new Button(absComposite, SWT.RADIO);
		abstractionType[0].setSelection(true);
		abstractionType[0].setText("Data");
						

		abstractionType[1] = new Button(absComposite, SWT.RADIO);
		abstractionType[1].setSelection(false);
		abstractionType[1].setText("Path");

		// Predicate abstraction
		Composite abstractComposite = new Composite(mainComposite, SWT.NONE);
		abstractComposite.setLayout(new GridLayout(2, false));
		abstractComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		absLabel = new Label(abstractComposite, SWT.FILL);
		absLabel.setText("Abstraction Criteria");

		absText = new Text(abstractComposite, SWT.BORDER|SWT.FILL);
		GridData absGd = new GridData();
		absGd.widthHint = 800;
		absText.setLayoutData(absGd);

		absSpace = new Label(abstractComposite, SWT.FILL);
		absSpace.setText("                     ");

		absSyntax = new Label(abstractComposite, SWT.FILL);
		absSyntax.setText("Comma-separated entries each of which may be =n, <n, >n, #n, \n"
				+ "[a:b:..:c] or left empty, e.g., =5,,>3,[2:5:8],#true,<4.17,=str");
		// Predicate abstraction

		//Browse property
		Composite browseProperty = new Composite(mainComposite, SWT.NONE);
		browseProperty.setLayout(new GridLayout(5, false));
		browseProperty.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		propertyButton = new Button(browseProperty, SWT.PUSH);
		propertyButton.setText("Load Property");

		propertyFileLabel = new Label(browseProperty, SWT.FILL);
		propertyFileLabel.setText("Property File : ");

		propertyFileText = new Text(browseProperty, SWT.READ_ONLY);
		propertyFileText.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
		GridData propertyGd = new GridData();
		propertyGd.widthHint = 550;
		propertyFileText.setLayoutData(propertyGd);
		//Browse property

		Composite grammarView = new Composite(mainComposite, SWT.NONE);
		grammarView.setLayout(new GridLayout(3, false));
		grammarView.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		propertyLabel = new Label(grammarView, SWT.FILL);
		propertyLabel.setText("Properties       ");

		propertyText = new Text(grammarView, SWT.V_SCROLL);
		GridData grid = new GridData();
		grid.widthHint = 800;
//		grid.heightHint = 200;
		grid.heightHint = 100;
		propertyText.setLayoutData(grid);

		// Error Composite
		Composite errorComposite = new Composite(mainComposite, SWT.NONE);
		errorComposite.setLayout(new GridLayout(1, false));
		errorText = new Label(errorComposite, SWT.NONE);
		GridData errorGD = new GridData();
		errorGD.widthHint = 800;
		errorText.setText("                                                                ");
		errorText.setLayoutData(errorGD);

		// Image composite

		imageComposite = new Composite(mainComposite, SWT.NONE);
		imageComposite.setLayout(new GridLayout(1, false));
		imageComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		browser = new Browser(imageComposite, SWT.NONE);

		// Ev2 composite
		Composite ev2Composite = new Composite(mainComposite, SWT.NONE);
		ev2Composite.setLayout(new GridLayout(8, false));
		ev2Composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		startButton2 = new Button(ev2Composite, SWT.PUSH);
		startButton2.setText("Start");
		startButton2.setToolTipText("Start state");
		startButton2.setEnabled(false);

		prevButton2 = new Button(ev2Composite, SWT.PUSH);
		prevButton2.setText("Prev");
		prevButton2.setToolTipText("Previous state");
		prevButton2.setEnabled(false);

		nextButton2 = new Button(ev2Composite, SWT.PUSH);
		nextButton2.setText("Next");
		nextButton2.setToolTipText("Next state");
		nextButton2.setEnabled(false);

		endButton2 = new Button(ev2Composite, SWT.PUSH);
		endButton2.setText(" End ");
		endButton2.setToolTipText("End state");
		endButton2.setEnabled(false);

		// Added for SVG support test

		canvasLabel = new Label(ev2Composite, SWT.FILL);
		canvasLabel.setText("Canvas dimension");

		hcanvasText = new Text(ev2Composite, SWT.BORDER | SWT.FILL);
		GridData hcd = new GridData();
		hcd.widthHint = 40;
		hcanvasText.setLayoutData(hcd);
		hcanvasText.setText("1000");

		byLabel = new Label(ev2Composite, SWT.FILL);
		byLabel.setText("   X    ");

		vcanvasText = new Text(ev2Composite, SWT.BORDER | SWT.FILL);
		GridData vcd = new GridData();
		vcd.widthHint = 40;
		vcanvasText.setLayoutData(vcd);
		vcanvasText.setText("500");

		statusLabel = new Label(ev2Composite, SWT.FILL);
		statusLabel.setText("StatusUpdate:");
	
		
		// Initialize the SVGGenerator
		svgGenerator = new SvgGenerator(hcanvasText, vcanvasText, browser, imageComposite, rootScrollComposite,
				mainComposite, display);

		//inserted by jevitha
		GridData browserLData = new GridData();
		browserLData.widthHint = Integer.parseInt(hcanvasText.getText());
		browserLData.heightHint = Integer.parseInt(vcanvasText.getText());
		browser.setLayoutData(browserLData);
		//browser.setText(svg);
		browser.setText("");
		imageComposite.pack();
		rootScrollComposite.setMinSize(mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));


		addButton.setEnabled(false);
		drawButton.setEnabled(false);
		validateButton.setEnabled(false);
		exportButton.setEnabled(false);

		abstractionType[0].addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(dataAbstraction == false) {
					dataAbstraction = true;
//					System.out.println("Data abstraction : "+dataAbstraction);
				}
				
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
			
		});
		
		abstractionType[1].addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(dataAbstraction == true) {
					dataAbstraction = false;
//					System.out.println("Data abstraction : "+dataAbstraction);		
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		
		validateButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				validateButtonAction(e);
			}
		});
		/*
		listenButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				listenButtonAction(e);
			}
		});

		runtimeModelButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				runtimeModelButtonAction(e);
			}
		});*/

		browseButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				browseButtonAction(e);
			}
		});

		propertyButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				loadPropertyButtonAction(e);
			}
		});

		exportButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				exportButtonAction(e);
			}
		});

		attributeList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				keyAttributeAction(e);
			}
		});

		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addButtonAction(e);
			}
		});

		drawButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				drawButtonAction(e);
			}
		});

		resetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				resetButtonAction(e);
			}
		});

		ssChkBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ssChkBoxAction(e);
			}
		});

		startButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				startButtonAction(e);
			}
		});

		prevButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				prevButtonAction(e);
			}
		});

		nextButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				nextButtonAction(e);
			}
		});

		endButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				endButtonAction(e);
			}
		});

		startButton2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				startButtonAction(e);
			}
		});

		prevButton2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				prevButtonAction(e);
			}
		});

		nextButton2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				nextButtonAction(e);
			}
		});

		endButton2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				endButtonAction(e);
			}
		});
	}

	/** Phew... */

	/**
	 * Reads Key Attributes and Abbreviations if present
	 * 
	 * @param attributes
	 * @param abbreviations
	 * @return set of keyAttributes
	 * 
	 */
	private Set<String> readKeyAttributes(Text attributes, Text abbreviations) {
		if (attributes != null && attributes.getText().length() > 0) {
			Set<String> keyAttributes = new LinkedHashSet<String>();
			String selected = attributes.getText();
			for (String attribute : selected.split(",")) {
				keyAttributes.add(attribute.trim());
			}

			String[] attrArray = new String[keyAttributes.size()];
			attrArray = keyAttributes.toArray(attrArray);
			if (abbreviations != null && abbreviations.getText().length() > 0) {
				Event.abbreviations.clear();
				String[] tokens = abbreviations.getText().split(",");
				if (tokens == null || tokens.length == 0)
					throw new IllegalArgumentException("Invalid Abbreviations");
				for (int i=0;i<tokens.length;i++) {
					String abbreviation = tokens[i];
					if(abbreviation.isEmpty() || abbreviation.length()==0)
						throw new IllegalArgumentException("Invalid Abbreviations");
					String term = abbreviation.trim();
					Event.abbreviations.put(attrArray[i], term);
				}
			}
			return keyAttributes;
		}
		throw new IllegalArgumentException("Please add atleast one attribute");
	}

	/**
	 * Reads property text and parses them into a list of expressions
	 * 
	 * @param propertyText
	 * @return List of expressions
	 * @throws Exception
	 */
	private List<Expression> parseExpressions(Text propertyText) throws Exception {
		if (propertyText != null && propertyText.getText().length() > 0) {
			Parser parser = new TopDownParser();
			String properties = propertyText.getText().trim();
			return parser.parse(properties.split(";"));
		}
		throw new IllegalArgumentException("Please enter properties to validate");
	}

	/**
	 * Validates and Draws the State Machine
	 * 
	 * @param e
	 */
	private void validateButtonAction(SelectionEvent e) {
		errorText.setText("                                                                ");
		try {
			count = Integer.MAX_VALUE;
			TemporaryDataTransporter.shouldHighlight = false;
			TemporaryDataTransporter.F_success_states = new HashSet<State>();
			Set<String> keyAttributes = readKeyAttributes(kvText, paText);
			List<Expression> expressions = null;
			try {
				expressions = parseExpressions(propertyText);
			} catch (Exception e3) {
				errorText.setText(e3.getMessage());
//				e3.printStackTrace();
			}
			if (expressions != null && expressions.size() > 0) {
				monitor = new OfflineMonitor(keyAttributes, incomingEvents, granularity[1].getSelection(), dataAbstraction);
				monitor.run();
				abstraction();

				//validation
				if (monitor.validate(expressions)) {
					errorText.setText("All properties satisfied.                                 ");
				}

				transitionBuilder = new TransitionBuilder(monitor.getRootState(), monitor.getStates(), transitionCount.getSelection(), monitor.getSeqState(), count);
				transitionBuilder.build();
				System.out.println(transitionBuilder.getTransitions());
				svgGenerator.generate(transitionBuilder.getTransitions());
				exportButton.setEnabled(true);
			}
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
			errorText.setText(e1.getMessage());
		} catch (ClassCastException e2) {
			errorText.setText("Type mismatch in properties");
			e2.printStackTrace();
		} catch (Exception e2) {
			errorText.setText("Unexpected error evaluating properties");
			e2.printStackTrace();
		}
	}

	/*
	private void listenButtonAction(SelectionEvent e) {
		//monitor = null;
		OnlineMonitor onlineMonitor=null;
		int portNum = 5000;
		
		if((networkEventParser == null))
			networkEventParser = new NetworkEventParser();
		
		if (image != null) {
			if (!image.isDisposed()) {
				System.out.println("Image disposedx");
				image.dispose();
			}
		}
		statusLineManager.setMessage("");
		attributeList.removeAll();
		errorText.setText("                                                                ");	
		
		String currentState = listenButton.getText();

		if(currentState.equalsIgnoreCase("Listen")) {
			String portNumber = portText.getText();
			if(portNumber.equals("")) {
				System.err.println("Port Number Missing");
				errorText.setText("Missing Port Number");
				portText.setFocus();
				MessageDialog.openError(new Shell(Display.getCurrent()), 
						"Missing Port Number", "Please enter the port number to listen for the JIVE events.");
				return;
			}
			else
				portNum = Integer.valueOf(portNumber);
		 

			String keyAttr = kvText.getText(); // From the UI
			if (keyAttr.equals("")) {
				System.err.println("Key Attributes Missing");
				errorText.setText("Missing Key Attributes.");
				kvText.setFocus();
				MessageDialog.openError(new Shell(Display.getCurrent()), 
						"Missing Key Attributes", "Please enter the key attributes to generate the runtime model.");
				return;
			}
			updateUI("Started Listening");
			listenButton.setText("Stop");
			statusLineManager.setMessage("Started Listening on port : " + portNum);
			networkEventParser.startListeningForEvents(this, portNum);
			Set<String> allAttributes = networkEventParser.getAllFields();
			this.incomingEvents = networkEventParser.getEvents();

			for (String attribute : allAttributes) {
				attributeList.add(attribute);
			}
			addButton.setEnabled(true);
			drawButton.setEnabled(true);
			validateButton.setEnabled(true);
			runtimeModelButtonAction();
			//			Job job = new Job("CheckForEventJob") {
			//
			//				protected IStatus run(IProgressMonitor monitor) {
			//					System.out.println("Waiting for events Job");
			//					while(incomingEvents.isEmpty()) {
			//						System.out.println("Waiting for events ...");
			//						try {
			//							Thread.sleep(1000);
			//						} catch (InterruptedException e) {
			//							// TODO Auto-generated catch block
			//							e.printStackTrace();
			//						}
			//					}
			//					System.out.println("Receiving events . Invoking Runtime model...");
			//					return Status.OK_STATUS;
			//					//monitor.beginTask("Monitoring Started", 10);
			//				}
			//			};
			//					
			//			job.setUser(true);
			//			job.schedule();
		}
		else {
			try {	
				statusLineManager.setMessage("Stopped Listening on port : " + portNum);
				networkEventParser.stopListeningForEvents();
				if(monitor != null &&  monitor instanceof OnlineMonitor) {
					onlineMonitor = (OnlineMonitor) monitor;
					onlineMonitor.stopMonitoring();
				}
				listenButton.setText("Listen");
				updateUI("Stopped Listening");
				return;
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}	
	}

	private void runtimeModelButtonAction(SelectionEvent e) {	
		runtimeModelButtonAction();
	}

	private void runtimeModelButtonAction() {	

		try {
			errorText.setText("                                                                ");
			Set<String> keyAttributes = readKeyAttributes(kvText, paText);
			if(incomingEvents==null && networkEventParser != null)
				this.incomingEvents = networkEventParser.getEvents();
			//			else {
			//				System.err.println("Please select the events log file or listen for events by clicking Listen button.");
			//				MessageDialog.openError(new Shell(Display.getCurrent()), 
			//						"No Events", "Please select the events log file or listen for events by clicking Listen button.");
			//			}

			List<Expression> expressions = null;
			try {
				expressions = parseExpressions(propertyText);
			} catch (Exception e3) {
				errorText.setText(e3.getMessage());
				//				MessageDialog.openError(new Shell(Display.getCurrent()), 
				//						"No Properties", "Please enter the properties to validate on the model.");
				//				propertyText.setFocus();
				//e3.printStackTrace();
			}
		
			String paString = null, propString = null;
			if(propertyText !=null)
				propString = propertyText.getText();
			
			if(absText!=null)
				paString = absText.getText();
			
			if(monitor == null)
				monitor = new OnlineMonitor(keyAttributes, incomingEvents, propString, 
						paString, transitionCount.getSelection(),svgGenerator);
			monitor.run();

		

			//					transitionBuilder = new TransitionBuilder(monitor.getRootState(), monitor.getStates(), transitionCount.getSelection(), monitor.getSeqState(), count);
			//					transitionBuilder.build();
			//					svgGenerator.generate(transitionBuilder.getTransitions());
			//					statusLineManager.setMessage("Finite State Model for " + kvText.getText());
			//					exportButton.setEnabled(true);	


			exportButton.setEnabled(true);






			//			else {
			//				MessageDialog.openError(new Shell(Display.getCurrent()), 
			//						"Missing Events", "Please listen on specific port for JIVE events to generate the runtime model.");
			//				return;
			//				
			//			}
		} 
		catch (IllegalArgumentException e1) {
			errorText.setText(e1.getMessage());
		}

		//processAction(count);
	}
	
	
	public void updateUI(String message) {

		if(message == null)
			return;

		display.asyncExec ( new Runnable () {

			@Override
			public void run ()
			{
				statusLabel.setText(message);
				statusLabel.setToolTipText(message);
				if(message.contains("Client Disconnected") ||
						message.contains("Stopped Listening") ) {
					listenButton.setText("Listen");
				}
			}
		} );
	}*/

	/**
	 * Reads input file and processes all attributes and events
	 * 
	 * @param e
	 */
	private void browseButtonAction(SelectionEvent e) {
		if (image != null) {
			if (!image.isDisposed()) {
				System.out.println("Image disposedx");
				image.dispose();
			}
		}

		statusLineManager.setMessage(null);
		FileDialog fd = new FileDialog(new Shell(Display.getCurrent(), SWT.OPEN));
		fd.setText("Open CSV File");
		String[] filterExtensions = { "*.csv" };
		fd.setFilterExtensions(filterExtensions);

		String fileName = fd.open();
		if (fileName == null)
			return;
		fileText.setText(fileName);
		attributeList.removeAll();
		kvText.setText("");
		paText.setText("");
		absText.setText("");
		propertyText.setText("");
		errorText.setText("                                                                ");
		monitor = null;
		InputFileParser inputFileParser = new InputFileParser(fileName);
		Set<String> allAttributes = inputFileParser.getAllFields();
		this.incomingEvents = inputFileParser.getEvents();
		for (String attribute : allAttributes) {
			attributeList.add(attribute);
		}
		addButton.setEnabled(true);
		drawButton.setEnabled(true);
		validateButton.setEnabled(true);
		statusLineManager.setMessage("Loaded " + fileName);
	}

	/**
	 * Reads property file and loads in the property text area
	 */
	private void loadPropertyButtonAction(SelectionEvent e){
		if (image != null) {
			if (!image.isDisposed()) {
				System.out.println("Image disposedx");
				image.dispose();
			}
		}

		statusLineManager.setMessage(null);
		FileDialog fd = new FileDialog(new Shell(Display.getCurrent(), SWT.OPEN));
		fd.setText("Open txt File");
		String[] filterExtensions = { "*.txt" };
		fd.setFilterExtensions(filterExtensions);

		String fileName = fd.open();
		if (fileName == null)
			return;
		propertyFileText.setText(fileName);
		try {
			Path path = Paths.get(fileName);
			Stream<String> lines = Files.lines(path);
			String content = lines.collect(Collectors.joining("\n"));
			System.out.println("Property Content : \n"+content);
			lines.close();

			propertyText.setText(content);
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
		statusLineManager.setMessage("Loaded " + fileName);
	}

	/**
	 * Exports the state machine into an svg file
	 * 
	 * @param e
	 */
	private void exportButtonAction(SelectionEvent e) {
		SourceStringReader reader = new SourceStringReader(transitionBuilder.getTransitions());
		FileDialog fd = new FileDialog(new Shell(Display.getCurrent()), SWT.SAVE);
		fd.setText("Export As");
		String[] filterExtensions = { "*.svg", "*.png" };
		fd.setFilterExtensions(filterExtensions);
		String fileName = fd.open();
		int extn = fd.getFilterIndex();
		System.out.println("Filename:" +fileName);
		System.out.println("Filter index:" + extn);
		System.out.println("Filter index name:" + filterExtensions[extn]);

		if (fileName != null) {
			try {
				//reader.outputImage(new File(fd.open()));
				/*Updated by Jevitha
				Updated from new File to new FileOutputStream due to error displayed
				 */
				if(fd.getFilterIndex() == 0)
					reader.outputImage(new FileOutputStream(fileName), new FileFormatOption(FileFormat.SVG));
				else
					reader.outputImage(new FileOutputStream(fileName),new FileFormatOption(FileFormat.PNG));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void keyAttributeAction(SelectionEvent e) {
		String keyVar = attributeList.getText();
		System.out.println(keyVar);
		statusLineManager.setMessage("Selected key attribute: " + keyVar);
	}

	/**
	 * Adds attribute to key attributes
	 * 
	 * @param e
	 */
	private void addButtonAction(SelectionEvent e) {

		String keyVar = attributeList.getText();
		if (!keyVar.equals("")) {
			if (kvText.getText().equals(""))
				kvText.setText(keyVar);
			else
				kvText.setText(kvText.getText() + "," + keyVar);
			System.out.println("Adding key attribute ... " + keyVar);
		}
		attributeList.setText("");
	}

	protected void validateAbstractedState() {
		monitor.setStates(new HashMap<>());
		List<State> seqStates = monitor.getSeqState();
		int s = 0;
		monitor.setStates(new HashMap<>());
		State previousState = monitor.getRootState();
		Map<State, Set<State>> states = new LinkedHashMap<>();

		while (s<seqStates.size()) {
			State newState = seqStates.get(s);
			if(s==0) {
				states.put(newState, new LinkedHashSet<State>());
				monitor.setRootState(newState);
			} else {
				states.get(previousState).add(newState);
				if (!states.containsKey(newState))
					states.put(newState, new LinkedHashSet<State>());
			}
			previousState = newState;
			s++;
		}

		monitor.setStates(states);
	}

	public void abstraction() {
		List<State> seqStates = monitor.getSeqState();
		String paStr = absText.getText().trim();
		if (paStr.equals(""))
			return;

		//If abstraction is given and in the property any future variable is present like a'
		//then we can't do abstraction on that
		Pattern p = Pattern.compile(FSMConstants.REGEX);
		Matcher m = p.matcher(propertyText.getText()); 
		if(m.find()) {
			throw new IllegalArgumentException("The primed variable inside Property is not compatible with Abstraction");
		}

		p = Pattern.compile(FSMConstants.OR_REGEX);
		m = p.matcher(propertyText.getText()); 
		if(m.find()) {
			throw new IllegalArgumentException("Property checking with abstraction : Replace OR expression with AND");
		}

		String[] paEntries = paStr.split(",");
		boolean reductionFlag = false;
		int s = 0;
		
		if(dataAbstraction) {
			while (s<seqStates.size()) {
				List<ValueExpression> valueList = new ArrayList<>(seqStates.get(s).getVector().values());
				List<String> keyList = new ArrayList<>(seqStates.get(s).getVector().keySet());

				for (int k=0; k<paEntries.length && k<valueList.size(); k++) {
					if ( !paEntries[k].trim().equals("") ) {
						String absVal = FSMUtil.applyAbstraction(valueList.get(k).toString(), paEntries[k]);
						if (absVal.equals("")) {
							reductionFlag = true;
							break;
						}
						else {
							if(s!=0) {
								Map<String, ValueExpression> map = seqStates.get(s).getVector();
								map.put(keyList.get(k), new StringValueExpression(absVal));
							} else {
								State initial = seqStates.get(s).copy();
								Map<String, ValueExpression> map = initial.getVector();
								map.put(keyList.get(k), new StringValueExpression(absVal));
								seqStates.set(0, initial);
								//monitor.getRootState().getVector().put(keyList.get(k), new StringValueExpression(absVal));
							}
						}
					}
				}	
				if (reductionFlag) {
					State hState = seqStates.get(s).copy();
					//hState.hashed = true; //check this
					seqStates.set(s, hState);
					reductionFlag = false;
					s++;
				}
				else
					s++;
			}
			
		}
		else { // Path Abstraction - Jevitha 
			
			List<State> pathAbstSeqStates = new ArrayList<>();
			//boolean startState = false;
			while (s<seqStates.size()) {
				List<ValueExpression> valueList = new ArrayList<>(seqStates.get(s).getVector().values());
				List<String> keyList = new ArrayList<>(seqStates.get(s).getVector().keySet());

				for (int k=0; k<paEntries.length; k++) {
					if ( !paEntries[k].trim().equals("") && 
							valueList.get(0).toString().equalsIgnoreCase(paEntries[k].trim()) ) {
						
						String absVal = paEntries[k].trim();
						
						//if(startState) {
							//Map<String, ValueExpression> map = seqStates.get(s).getVector();
							//map.put(keyList.get(0), new StringValueExpression(absVal));
							pathAbstSeqStates.add(seqStates.get(s).copy());
						/*} else {
							State initial = seqStates.get(s).copy();
							//Map<String, ValueExpression> map = initial.getVector();
							//map.put(keyList.get(0), new StringValueExpression(absVal));
							pathAbstSeqStates.add(initial);
							startState = true;
							//monitor.getRootState().getVector().put(keyList.get(k), new StringValueExpression(absVal));
						}*/
					}
				}	
				s++;
			}
			seqStates.retainAll(pathAbstSeqStates);

		}
		validateAbstractedState();
	}

	private void processAction(int count) {
		errorText.setText("                                                                ");
		try {
			Set<String> keyAttributes = readKeyAttributes(kvText, paText);
			monitor = new OfflineMonitor(keyAttributes, incomingEvents, granularity[1].getSelection(), dataAbstraction);
			monitor.run();
			System.out.println("Monitor Stats : "+monitor.getSeqState().size()+" "+monitor.getStates().size());
			if(monitor.getStates().size() > 0) {
				//apply abstraction here, for validate button action check to add this or not
				abstraction();
				transitionBuilder = new TransitionBuilder(monitor.getRootState(), monitor.getStates(), transitionCount.getSelection(), monitor.getSeqState(), count);
				transitionBuilder.build();
				System.out.println(transitionBuilder.getTransitions());
				svgGenerator.generate(transitionBuilder.getTransitions());
				statusLineManager.setMessage("Finite State Model for " + kvText.getText());
				exportButton.setEnabled(true);
			}
			else {

				MessageDialog.openError(new Shell(Display.getCurrent()), 
						"Missing Events", "Please select the JIVE events log file to generate the model.");
				return;

			}
		} 
		catch (IllegalArgumentException e1) {
			errorText.setText(e1.getMessage());
		}
	}

	/**
	 * Builds and draws the State Machine
	 * 
	 * @param e
	 */
	private void drawButtonAction(SelectionEvent e) {
		count = Integer.MAX_VALUE;
		processAction(count);
	}

	/**
	 * Resets key attributes, abbreviations, properties and errors
	 * 
	 * @param e
	 */
	private void resetButtonAction(SelectionEvent e) {
		kvText.setText("");
		paText.setText("");
		propertyText.setText("");
		errorText.setText("                                                                ");
	}

	private void ssChkBoxAction(SelectionEvent e) {
		if (ssChkBox.getSelection()) {

			drawButton.setEnabled(false);
			prevButton.setEnabled(false); prevButton2.setEnabled(false);
			startButton.setEnabled(true); startButton2.setEnabled(true);
		}
		else {
			drawButton.setEnabled(true);
			startButton.setEnabled(false); startButton2.setEnabled(false);
			prevButton.setEnabled(false); prevButton2.setEnabled(false);
			nextButton.setEnabled(false); nextButton2.setEnabled(false);
			endButton.setEnabled(false); endButton2.setEnabled(false);
		}
	}

	private void startButtonAction(SelectionEvent e) {
		count = 0;
		processAction(count);
		prevButton.setEnabled(false); prevButton2.setEnabled(false);
		nextButton.setEnabled(true); nextButton2.setEnabled(true);
		endButton.setEnabled(true); endButton2.setEnabled(true);
	}

	private void prevButtonAction(SelectionEvent e) {
		if (count > 0) {
			nextButton.setEnabled(true); nextButton2.setEnabled(true);
		}
		processAction(--count);
		if (count <= 0){
			prevButton.setEnabled(false); prevButton2.setEnabled(false);
		}
	}

	private void nextButtonAction(SelectionEvent e) {
		if (count < monitor.getSeqState().size() - 1) {
			prevButton.setEnabled(true); prevButton2.setEnabled(true);
		}
		processAction(++count);
		if (count >= monitor.getSeqState().size() - 1) {
			nextButton.setEnabled(false); nextButton2.setEnabled(false);
		}
	}

	private void endButtonAction(SelectionEvent e) {
		count = monitor.getSeqState().size() - 1;
		processAction(Integer.MAX_VALUE);
		prevButton.setEnabled(true); prevButton2.setEnabled(true);
		nextButton.setEnabled(false); nextButton2.setEnabled(false);
	}

		@Override
		public void setFocus() {
	
		}
}