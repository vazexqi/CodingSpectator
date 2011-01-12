/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.replaying;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import edu.illinois.codingspectator.codingtracker.helpers.FileHelper;
import edu.illinois.codingspectator.codingtracker.operations.OperationDeserializer;
import edu.illinois.codingspectator.codingtracker.operations.UserOperation;


/**
 * 
 * @author Stas Negara
 * 
 */
public class OperationSequenceView extends ViewPart {

	public static final String ID= "edu.illinois.codingspectator.codingtracker.replaying.views.OperationSequenceView";

	private static final SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss:SS");

	private static final Color whiteColor= Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);

	private static final Color yellowColor= Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);

	private TableViewer viewer;

	private OperationSequenceFilter filter= new OperationSequenceFilter();

	private Action replayAction;

	private Text operationTextPane;

	private Iterator<UserOperation> userOperationsIterator;

	private UserOperation currentUserOperation;

	public OperationSequenceView() {
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		createViewer(parent);
		createOperationTextPane(parent);
	}

	private void createOperationTextPane(Composite parent) {
		final int textPaneRowsCount= 10;
		operationTextPane= new Text(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY | SWT.MULTI | SWT.BORDER);
		GC gc= new GC(operationTextPane);
		int textPaneHeight= textPaneRowsCount * gc.getFontMetrics().getHeight();
		GridData gridData= new GridData();
		gridData.heightHint= textPaneHeight;
		gridData.grabExcessHorizontalSpace= true;
		gridData.horizontalAlignment= GridData.FILL;
		operationTextPane.setLayoutData(gridData);
	}

	private void createViewer(Composite parent) {
		viewer= new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.addFilter(filter);
		createViewerColumns();
		createViewerToolBar();
		layoutViewer();
		addViewerListeners();
	}

	private void createViewerColumns() {
		TableViewerColumn descriptionColumn= createColumn("Operation description", 200);
		descriptionColumn.setLabelProvider(new StyledCellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				cell.setText(((UserOperation)cell.getElement()).getDescription());
				updateCellAppearance(cell);
			}
		});
		TableViewerColumn dateColumn= createColumn("Date", 150);
		dateColumn.setLabelProvider(new StyledCellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				cell.setText(dateFormat.format(((UserOperation)cell.getElement()).getDate()));
				updateCellAppearance(cell);
			}
		});
	}

	private void updateCellAppearance(ViewerCell cell) {
		if (cell.getElement() == currentUserOperation) {
			cell.setBackground(yellowColor);
			cell.scrollIntoView();
		} else {
			cell.setBackground(whiteColor);
		}
	}

	private TableViewerColumn createColumn(String title, int width) {
		TableViewerColumn viewerColumn= new TableViewerColumn(viewer, SWT.NONE);
		TableColumn tableColumn= viewerColumn.getColumn();
		tableColumn.setText(title);
		tableColumn.setWidth(width);
		tableColumn.setResizable(true);
		tableColumn.setMoveable(false);
		return viewerColumn;
	}

	private void createViewerToolBar() {
		IToolBarManager toolBarManager= getViewSite().getActionBars().getToolBarManager();
		toolBarManager.add(createLoadOperationSequenceAction());
		toolBarManager.add(new Separator());
		toolBarManager.add(createFilterAction("Text Changes", "Display text change operations", OperationSequenceFilter.FilteredOperations.TEXT_CHANGES));
		toolBarManager.add(createFilterAction("Refactorings", "Display refactoring operations", OperationSequenceFilter.FilteredOperations.REFACTORINGS));
		toolBarManager.add(createFilterAction("Snapshots", "Display snapshot-producing operations", OperationSequenceFilter.FilteredOperations.SNAPSHOTS));
		toolBarManager.add(createFilterAction("Others", "Display all other operations", OperationSequenceFilter.FilteredOperations.OTHERS));
		toolBarManager.add(new Separator());
		toolBarManager.add(createReplayAction());
	}

	private IAction createLoadOperationSequenceAction() {
		Action action= new Action() {
			@Override
			public void run() {
				FileDialog fileDialog= new FileDialog(viewer.getControl().getShell(), SWT.OPEN);
				String selectedFilePath= fileDialog.open();
				if (selectedFilePath != null) {
					String operationsRecord= FileHelper.getFileContent(new File(selectedFilePath));
					List<UserOperation> userOperations= OperationDeserializer.getUserOperations(operationsRecord);
					userOperationsIterator= userOperations.iterator();
					advanceCurrentUserOperation();
					viewer.setInput(userOperations);
				}
			}
		};
		action.setText("Load");
		action.setToolTipText("Load operation sequence from a file");
		return action;
	}

	private IAction createFilterAction(String actionText, String actionToolTipText,
										final OperationSequenceFilter.FilteredOperations filteredOperations) {
		Action action= new Action() {
			@Override
			public void run() {
				filter.toggleFilteredOperations(filteredOperations);
				viewer.refresh();
			}
		};
		action.setText(actionText);
		action.setToolTipText(actionToolTipText);
		action.setChecked(true);
		return action;
	}

	private IAction createReplayAction() {
		replayAction= new Action() {
			@Override
			public void run() {
				try {
					currentUserOperation.replay();
				} catch (Exception e) {
					e.printStackTrace();
				}
				advanceCurrentUserOperation();
				viewer.refresh();
			}
		};
		replayAction.setText("Replay");
		replayAction.setToolTipText("Replay the current user operation");
		replayAction.setEnabled(false);
		return replayAction;
	}

	private void advanceCurrentUserOperation() {
		if (userOperationsIterator.hasNext()) {
			currentUserOperation= userOperationsIterator.next();
			replayAction.setEnabled(true);
		} else {
			currentUserOperation= null;
			replayAction.setEnabled(false);
		}
		viewer.setSelection(null, false);
		displayInOperationTextPane(currentUserOperation);
	}

	private void layoutViewer() {
		Table table= viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GridData gridData= new GridData();
		gridData.horizontalAlignment= GridData.FILL;
		gridData.verticalAlignment= GridData.FILL;
		gridData.grabExcessHorizontalSpace= true;
		gridData.grabExcessVerticalSpace= true;
		viewer.getControl().setLayoutData(gridData);
	}

	private void addViewerListeners() {
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				displayInOperationTextPane(((IStructuredSelection)event.getSelection()).getFirstElement());
			}
		});
	}

	private void displayInOperationTextPane(Object object) {
		if (object != null) {
			//remove \r chars, because they are displayed as little squares
			operationTextPane.setText(object.toString().replace("\r", ""));
		} else {
			operationTextPane.setText("");
		}
	}

}
