/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.replaying;


import java.text.SimpleDateFormat;
import java.util.List;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.InferredUnknownTransformationOperation;


/**
 * 
 * @author Stas Negara
 * 
 */
public class OperationSequenceView extends ViewPart {

	public static final String ID= "edu.illinois.codingtracker.replaying.views.OperationSequenceView";

	private static final SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss:SS");

	private static final Color whiteColor= Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);

	private static final Color yellowColor= Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);

	private static final Color redColor= Display.getCurrent().getSystemColor(SWT.COLOR_RED);

	private static final Color greenColor= Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);

	private static final Color darkGreenColor= Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN);

	private final OperationSequenceFilter operationSequenceFilter;

	private final UserOperationReplayer userOperationReplayer;

	private TableViewer tableViewer;

	private Text operationTextPane;

	private boolean shouldScrollToCurrentOperation= true;


	public OperationSequenceView() {
		operationSequenceFilter= new OperationSequenceFilter(this);
		userOperationReplayer= new UserOperationReplayer(this);
	}

	@Override
	public void setFocus() {
		tableViewer.getControl().setFocus();
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
		tableViewer= new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.addFilter(operationSequenceFilter);
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
		TableViewerColumn timestampColumn= createColumn("Timestamp", 90);
		timestampColumn.setLabelProvider(new StyledCellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				cell.setText(String.valueOf(((UserOperation)cell.getElement()).getTime()));
				updateCellAppearance(cell);
			}
		});
	}

	private void updateCellAppearance(ViewerCell cell) {
		Object cellElement= cell.getElement();
		cell.setBackground(whiteColor);
		if (userOperationReplayer.isPattern(cellElement)) {
			if (userOperationReplayer.isLastPatternElement(cellElement)) {
				cell.setBackground(darkGreenColor);
			} else {
				cell.setBackground(greenColor);
			}
			if (userOperationReplayer.isFirstPatternElement(cellElement)) {
				cell.scrollIntoView();
			}
		}
		if (userOperationReplayer.isBreakpoint(cellElement)) {
			cell.setBackground(redColor);
		}
		if (userOperationReplayer.isCurrentUserOperation(cellElement)) {
			cell.setBackground(yellowColor);
			if (shouldScrollToCurrentOperation) {
				cell.scrollIntoView();
			}
		}
	}

	private TableViewerColumn createColumn(String title, int width) {
		TableViewerColumn viewerColumn= new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tableColumn= viewerColumn.getColumn();
		tableColumn.setText(title);
		tableColumn.setWidth(width);
		tableColumn.setResizable(true);
		tableColumn.setMoveable(false);
		return viewerColumn;
	}

	private void createViewerToolBar() {
		userOperationReplayer.addToolBarActions();
		operationSequenceFilter.addToolBarActions();
	}

	private void layoutViewer() {
		Table table= tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GridData gridData= new GridData();
		gridData.horizontalAlignment= GridData.FILL;
		gridData.verticalAlignment= GridData.FILL;
		gridData.grabExcessHorizontalSpace= true;
		gridData.grabExcessVerticalSpace= true;
		tableViewer.getControl().setLayoutData(gridData);
	}

	private void addViewerListeners() {
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				displayInOperationTextPane(((IStructuredSelection)event.getSelection()).getFirstElement());
			}
		});
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				UserOperation userOperation= (UserOperation)((IStructuredSelection)event.getSelection()).getFirstElement();
				userOperationReplayer.toggleBreakpoint(userOperation);
				removeSelection();
				shouldScrollToCurrentOperation= false;
				updateTableViewerElement(userOperation);
				shouldScrollToCurrentOperation= true;
			}
		});
	}

	IToolBarManager getToolBarManager() {
		return getViewSite().getActionBars().getToolBarManager();
	}

	Shell getShell() {
		return tableViewer.getControl().getShell();
	}

	Display getDisplay() {
		return tableViewer.getControl().getDisplay();
	}

	void setTableViewerInput(Object object) {
		tableViewer.setInput(object);
	}

	void updateTableViewerElement(UserOperation userOperation) {
		if (userOperation != null) {
			tableViewer.update(userOperation, null);
		}
	}

	public void updateTableViewerElements(List<InferredUnknownTransformationOperation> patternOperations) {
		for (InferredUnknownTransformationOperation operation : patternOperations) {
			updateTableViewerElement(operation);
		}
	}

	void refreshTableViewer() {
		tableViewer.refresh();
	}

	OperationSequenceFilter getOperationSequenceFilter() {
		return operationSequenceFilter;
	}

	void setSelection(ISelection selection) {
		tableViewer.setSelection(selection, true);
	}

	void removeSelection() {
		tableViewer.setSelection(null, false);
	}

	void displayInOperationTextPane(Object object) {
		if (object != null) {
			//remove \r chars, because they are displayed as little squares
			operationTextPane.setText(object.toString().replace("\r", ""));
		} else {
			operationTextPane.setText("");
		}
	}

}
