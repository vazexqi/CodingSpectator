/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.replaying;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
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

	private TableViewer viewer;

	private OperationSequenceFilter filter= new OperationSequenceFilter();

	private Text operationTextPane;

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
		descriptionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((UserOperation)element).getDescription();
			}
		});
		TableViewerColumn dateColumn= createColumn("Date", 150);
		dateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return dateFormat.format(((UserOperation)element).getDate());
			}
		});
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
				Object firstSelectionElement= ((IStructuredSelection)event.getSelection()).getFirstElement();
				if (firstSelectionElement != null) {
					//remove \r chars, because they are displayed as little squares
					operationTextPane.setText(firstSelectionElement.toString().replace("\r", ""));
				} else {
					operationTextPane.setText("");
				}
			}
		});
	}

}
