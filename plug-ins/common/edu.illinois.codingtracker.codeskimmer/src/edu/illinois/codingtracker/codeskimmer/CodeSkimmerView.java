package edu.illinois.codingtracker.codeskimmer;

import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import edu.illinois.codingtracker.codeskimmer.CodeSkimmerConstants.VisualizationType;
import edu.illinois.codingtracker.operations.UserOperation;

/**
 * 
 * @author Connor Simmons
 *
 */
public class CodeSkimmerView extends ViewPart {

	// controller
	private final CodeSkimmerController skimController;

	// Playback toolbar icons
	private static final ImageDescriptor PLAY_ICON = Activator
			.getImageDescriptor("icons/play.png");
	private static final ImageDescriptor STOP_ICON = Activator
			.getImageDescriptor("icons/stop.png");
	private static final ImageDescriptor PAUSE_ICON = Activator
			.getImageDescriptor("icons/pause.png");
	private static final ImageDescriptor STEP_ICON = Activator
			.getImageDescriptor("icons/step.png");
	private static final ImageDescriptor REWIND_ICON = Activator
			.getImageDescriptor("icons/rewind.png");
	private static final ImageDescriptor FAST_FORWARD_ICON = Activator
			.getImageDescriptor("icons/fast_forward.png");

	// Handle to the view parent
	Composite cParent;

	// Playback toolbar buttons
	private IAction rewindButton;
	private IAction playButton;
	private IAction pauseButton;
	private IAction stepButton;
	private IAction fastForwardButton;
	private IAction loadButton;
	private IAction resetButton;

	// Visualization Controls
	private Combo visualizationSelectCombo;
	private Text operationRangeStart;
	private Text operationRangeEnd;
	private Button applyOperationRangeButton;
	private Button resetOperationRangeButton;

	// Visualization widgets
	private Composite visualizationWrapper;
	private OperationViewWidget currentVisualization;

	// Tabbed control area
	private TabFolder tabFolder;
	private Button captureCodeFilterButton;
	private Button clearCodeFiltersButton;
	private Text codeFilterText;

	private Combo usernameFilterCombo;
	private Button addUsernameFilterButton;
	private Button clearUsernameFiltersButton;
	private Text usernameFilterText;

	private Text infoPane;

	private ArrayList<UserOperation> operations;
	private ArrayList<Filter> filters;

	public CodeSkimmerView() {
		skimController = new CodeSkimmerController(this);
		operations = new ArrayList<UserOperation>();
		filters = new ArrayList<Filter>();
	}

	@Override
	public void setFocus() {
		currentVisualization.setFocus();
	}

	@Override
	public void createPartControl(Composite parent) {
		createPlaybackToolBar();

		parent.setLayout(new GridLayout());
		cParent = new Composite(parent, SWT.NONE);

		createViewer();
		skimController.addViewActions();
		skimController.updateView();
	}

	private void createViewer() {
		cParent.setLayout(new GridLayout(2, false));
		cParent.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
				true));

		// create visualization selector
		createVisualizationControl();

		// create wrapper for diffrent visualizations
		createVisualizationWrapper();

		// create right tab control area
		createTabPane();

		// create initial visualization
		createSequenceVisualization();
	}

	private void createVisualizationControl() {
		Composite outerC = new Composite(cParent, SWT.BORDER);
		outerC.setLayout(new GridLayout(4, false));
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		outerC.setLayoutData(gridData);

		visualizationSelectCombo = new Combo(outerC, SWT.READ_ONLY);
		visualizationSelectCombo
				.setItems(CodeSkimmerConstants.VISUALIZATION_OPTION_NAMES);
		visualizationSelectCombo.select(0);

		Composite rangeSelectC = new Composite(outerC, SWT.NONE);
		rangeSelectC.setLayout(new GridLayout(4, false));

		Label startLabel = new Label(rangeSelectC, SWT.NONE);
		startLabel.setText("Start:");
		operationRangeStart = new Text(rangeSelectC, SWT.NONE);
		operationRangeStart
				.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));
		Label endLabel = new Label(rangeSelectC, SWT.NONE);
		endLabel.setText("End:");
		operationRangeEnd = new Text(rangeSelectC, SWT.NONE);
		operationRangeEnd.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));

		applyOperationRangeButton = new Button(outerC, SWT.NONE);
		applyOperationRangeButton.setText("Apply Range to View");

		resetOperationRangeButton = new Button(outerC, SWT.NONE);
		resetOperationRangeButton.setText("Reset View Range");
	}

	public void enableApplyOperationRangeButton(boolean enabled) {
		applyOperationRangeButton.setEnabled(enabled);
	}

	public void setApplyOperationRangeButtonListener(Listener listener) {
		applyOperationRangeButton.addListener(SWT.Selection, listener);
	}

	public void enableResetOperationRangeButton(boolean enabled) {
		resetOperationRangeButton.setEnabled(enabled);
	}

	public void setResetOperationRangeButtonListener(Listener listener) {
		resetOperationRangeButton.addListener(SWT.Selection, listener);
	}

	public long getIntensityFirstSelection() {
		if (currentVisualization instanceof IntensityWidget) {
			return ((IntensityWidget) currentVisualization).getFirstSelection();
		}
		return -1;
	}

	public long getIntensitySecondSelection() {
		if (currentVisualization instanceof IntensityWidget) {
			return ((IntensityWidget) currentVisualization)
					.getSecondSelection();
		}
		return -1;
	}

	private void createVisualizationWrapper() {
		visualizationWrapper = new Composite(cParent, SWT.BORDER);
		visualizationWrapper.setLayout(new GridLayout());
		visualizationWrapper.setLayoutData(new GridData(GridData.FILL,
				GridData.FILL, true, true));
	}

	private void createTabPane() {
		tabFolder = new TabFolder(cParent, SWT.NONE);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.END;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.heightHint = CodeSkimmerConstants.WIDGET_HEIGHT;
		gridData.widthHint = CodeSkimmerConstants.TAB_PANE_WIDTH;
		tabFolder.setLayoutData(gridData);

		TabItem tab1 = new TabItem(tabFolder, SWT.NONE);
		tab1.setText("Code Filter");
		tab1.setControl(createCodeFilterTab(tabFolder));

		TabItem tab2 = new TabItem(tabFolder, SWT.NONE);
		tab2.setText("User Filter");
		tab2.setControl(createUsernameFilterTab(tabFolder));

		TabItem tab3 = new TabItem(tabFolder, SWT.NONE);
		tab3.setText("Replay Info");
		tab3.setControl(createInfoTab(tabFolder));
	}

	private Composite createCodeFilterTab(TabFolder folder) {
		Composite codeFilterPane = new Composite(folder, SWT.BORDER);

		GridLayout gridLayout = new GridLayout(2, false);
		codeFilterPane.setLayout(gridLayout);
		GridData gridData;

		captureCodeFilterButton = new Button(codeFilterPane, SWT.PUSH);
		captureCodeFilterButton.setText("Capture Selection");

		clearCodeFiltersButton = new Button(codeFilterPane, SWT.PUSH);
		clearCodeFiltersButton.setText("Clear Selections");

		codeFilterText = new Text(codeFilterPane, SWT.READ_ONLY | SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		codeFilterText.setLayoutData(gridData);

		return codeFilterPane;
	}

	public void setVisualizationSelectListener(SelectionAdapter listener) {
		visualizationSelectCombo.addSelectionListener(listener);
	}

	public CodeSkimmerConstants.VisualizationType getVisualizationSelection() {
		String selectString = visualizationSelectCombo
				.getItem(visualizationSelectCombo.getSelectionIndex());
		if (selectString
				.equals(CodeSkimmerConstants.VISUALIZATION_OPTION_NAMES[0]))
			return CodeSkimmerConstants.VisualizationType.SEQUENCE;
		else
			return CodeSkimmerConstants.VisualizationType.INTENSITY;
	}

	private void updateOperationRangeText() {
		if (operations != null && operations.size() > 0) {
			operationRangeStart.setText(Long.toString(operations.get(0)
					.getTime()));
			operationRangeEnd.setText(Long.toString(operations.get(
					operations.size() - 1).getTime()));
		} else {
			operationRangeStart.setText("");
			operationRangeEnd.setText("");
		}
	}

	private void createNewVisualization(VisualizationType type) {
		if (currentVisualization != null && !currentVisualization.isDisposed()) {
			currentVisualization.dispose();
		}
		switch (type) {
		case SEQUENCE:
			currentVisualization = new SequenceWidget(visualizationWrapper,
					SWT.NONE);
			break;
		case INTENSITY:
			currentVisualization = new IntensityWidget(visualizationWrapper,
					SWT.NONE);
			break;
		}

		updateVisualization();
		visualizationWrapper.layout(true);
	}

	public void createSequenceVisualization() {
		createNewVisualization(VisualizationType.SEQUENCE);
	}

	public void createIntensityVisualization() {
		createNewVisualization(VisualizationType.INTENSITY);
	}

	public void updateVisualization() {
		setUserOperations(operations);
		setFilters(filters);
	}

	private void createPlaybackToolBar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars()
				.getToolBarManager();
		toolBarManager.add(createRewindButton());
		toolBarManager.add(createPlayButton());
		toolBarManager.add(createPauseButton());
		toolBarManager.add(createStepButton());
		toolBarManager.add(createFastForwardButton());
		toolBarManager.add(new Separator());
		toolBarManager.add(createLoadButton());
		toolBarManager.add(createResetButton());
	}

	private IAction createRewindButton() {
		rewindButton = new Action("", REWIND_ICON) {
			@Override
			public void run() {
				skimController.rewindButtonAction();
			}
		};
		rewindButton.setToolTipText("Rewind operation sequence to beginning");
		return rewindButton;
	}

	public void enableRewindButton(boolean enabled) {
		rewindButton.setEnabled(enabled);
	}

	private IAction createPlayButton() {
		playButton = new Action("", PLAY_ICON) {
			@Override
			public void run() {
				skimController.playButtonAction();
			}
		};
		playButton.setToolTipText("Playback code changes");
		return playButton;
	}

	public void enablePlayButton(boolean enabled) {
		playButton.setEnabled(enabled);
	}

	public void setPlayButtonIconToPlay() {
		playButton.setImageDescriptor(PLAY_ICON);
	}

	public void setPlayButtonIconToStop() {
		playButton.setImageDescriptor(STOP_ICON);
	}

	private IAction createPauseButton() {
		pauseButton = new Action("", PAUSE_ICON) {
			@Override
			public void run() {
				skimController.pauseButtonAction();
			}
		};
		pauseButton.setToolTipText("Pause/Resume the current replaying");
		pauseButton.setChecked(false);
		return pauseButton;
	}

	public void enablePauseButton(boolean enabled) {
		pauseButton.setEnabled(enabled);
		if (!enabled) {
			pauseButton.setChecked(false);
		}
	}

	private IAction createStepButton() {
		stepButton = new Action("", STEP_ICON) {
			@Override
			public void run() {
				skimController.stepButtonAction();
			}
		};
		stepButton.setToolTipText("Execute the current operation");
		return stepButton;
	}

	public void enableStepButton(boolean enabled) {
		stepButton.setEnabled(enabled);
	}

	private IAction createFastForwardButton() {
		fastForwardButton = new Action("", FAST_FORWARD_ICON) {
			@Override
			public void run() {
				skimController.fastForwardButtonAction();
			}
		};
		fastForwardButton
				.setToolTipText("Fast forward to currently selected operation");
		return fastForwardButton;
	}

	public void enableFastForwardButton(boolean enabled) {
		fastForwardButton.setEnabled(enabled);
	}

	private IAction createLoadButton() {
		loadButton = new Action("Load") {
			@Override
			public void run() {
				skimController.loadButtonAction();
			}
		};
		loadButton.setToolTipText("Load operation sequence from a file");
		return loadButton;
	}

	public void enableLoadButton(boolean enabled) {
		loadButton.setEnabled(enabled);
	}

	private IAction createResetButton() {
		resetButton = new Action("Reset") {
			@Override
			public void run() {
				skimController.resetButtonAction();
			}
		};
		resetButton
				.setToolTipText("Clears workspace and all loaded operations");
		return resetButton;
	}

	public void enableResetButton(boolean enabled) {
		resetButton.setEnabled(enabled);
	}

	public void setCaptureCodeFilterButtonListener(Listener listener) {
		captureCodeFilterButton.addListener(SWT.Selection, listener);
	}

	public void setClearCodeFiltersButtonListener(Listener listener) {
		clearCodeFiltersButton.addListener(SWT.Selection, listener);
	}

	public void enableCaptureCodeFilterButton(boolean enabled) {
		captureCodeFilterButton.setEnabled(enabled);
	}

	public void enableClearCodeFiltersButton(boolean enabled) {
		clearCodeFiltersButton.setEnabled(enabled);
	}

	private Composite createUsernameFilterTab(TabFolder folder) {
		Composite usernameFilterPane = new Composite(folder, SWT.BORDER);

		GridLayout gridLayout = new GridLayout(3, false);
		usernameFilterPane.setLayout(gridLayout);
		GridData gridData;

		usernameFilterCombo = new Combo(usernameFilterPane, SWT.READ_ONLY);

		addUsernameFilterButton = new Button(usernameFilterPane, SWT.PUSH);
		addUsernameFilterButton.setText("Select User");

		clearUsernameFiltersButton = new Button(usernameFilterPane, SWT.PUSH);
		clearUsernameFiltersButton.setText("Clear Selections");

		usernameFilterText = new Text(usernameFilterPane, SWT.READ_ONLY
				| SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.horizontalSpan = 3;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		usernameFilterText.setLayoutData(gridData);

		return usernameFilterPane;
	}

	public String getSelectedUsername() {
		return usernameFilterCombo.getItem(usernameFilterCombo
				.getSelectionIndex());
	}

	public void setUsernames(ArrayList<String> usernames) {
		if (usernames.size() > 0) {
			String[] userArray = new String[usernames.size()];
			usernameFilterCombo.setItems(usernames.toArray(userArray));
			usernameFilterCombo.select(0);
		}
	}

	public void setAddUsernameFilterButtonListener(Listener listener) {
		addUsernameFilterButton.addListener(SWT.Selection, listener);
	}

	public void setClearUsernameFiltersButtonListner(Listener listener) {
		clearUsernameFiltersButton.addListener(SWT.Selection, listener);
	}

	public void enableUsernameFilterCombo(boolean enabled) {
		usernameFilterCombo.setEnabled(enabled);
	}

	public void enableAddUsernameFilterButton(boolean enabled) {
		addUsernameFilterButton.setEnabled(enabled);
	}

	public void enableClearUsernameFiltersButton(boolean enabled) {
		clearUsernameFiltersButton.setEnabled(enabled);
	}

	private Composite createInfoTab(TabFolder folder) {
		Composite infoTab = new Composite(folder, SWT.BORDER);
		infoTab.setLayout(new GridLayout());

		infoPane = new Text(infoTab, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.READ_ONLY | SWT.MULTI);

		infoPane.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		return infoTab;
	}

	public void setFilters(ArrayList<Filter> userFilters) {
		if (userFilters == null) {
			filters = new ArrayList<Filter>();
		} else {
			this.filters = userFilters;
		}

		// update current visualization
		currentVisualization.setFilters(filters);

		// udpate text areas
		String codeFilterString = "";
		String usernameFilterString = "";
		for (int i = 0; i < filters.size(); i++) {
			Filter sel = filters.get(i);
			if (sel instanceof CodeFilter) {
				CodeFilter s = (CodeFilter) sel;
				codeFilterString += s.toString() + "\n";
			} else if (sel instanceof UsernameFilter) {
				UsernameFilter s = (UsernameFilter) sel;
				usernameFilterString += s.getUsername() + "\n";
			}
		}

		codeFilterText.setText(codeFilterString);
		usernameFilterText.setText(usernameFilterString);

		codeFilterText.redraw();
		usernameFilterText.redraw();
	}

	public void setUserOperations(ArrayList<UserOperation> userOperations) {
		if (userOperations == null) {
			operations = new ArrayList<UserOperation>();
		} else {
			operations = userOperations;
		}

		currentVisualization.setUserOperations(userOperations);
		updateOperationRangeText();
	}

	public void setCurrentOperation(UserOperation op) {
		currentVisualization.setCurrentOperation(op);
		if (op != null) {
			infoPane.setText(op.toString());
		}
		infoPane.redraw();
	}

	public void setVisualizationMouseListener(MouseListener listener) {
		currentVisualization.setMouseClickListener(listener);
	}

	public void handleMouseClickEvent(MouseEvent e) {
		currentVisualization.handleMouseClickEvent(e);
		UserOperation op = getSelectedOperation();
		if (op != null) {
			infoPane.setText(op.toString());
			infoPane.redraw();
		}
	}

	public UserOperation getSelectedOperation() {
		if (currentVisualization instanceof SequenceWidget) {
			return ((SequenceWidget) currentVisualization)
					.getSelectedOperation();
		} else {
			return null;
		}
	}

	public void clearSelection() {
		if (currentVisualization != null) {
			currentVisualization.clearSelection();
		}
	}

	public void showPopupMessage(String message) {
		MessageBox messageBox = new MessageBox(this.getSite().getShell());
		messageBox.setMessage(message);
		messageBox.open();
	}

	public String getFileDialogSelection() {
		FileDialog fileDialog = new FileDialog(this.getSite().getShell(),
				SWT.OPEN);
		return fileDialog.open();
	}

	public String getUsernameDialogSelection() {
		UsernameDialog dialog = new UsernameDialog(this.getSite().getShell());
		dialog.open();
		return UsernameDialog.getUsername();
	}

	public Display getViewDisplay() {
		return this.getSite().getShell().getDisplay();
	}
}
