package edu.illinois.codingtracker.codeskimmer;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

import edu.illinois.codingtracker.operations.UserOperation;

/**
 * 
 * @author Connor Simmons
 *
 */
public class IntensityWidget extends OperationViewWidget {

	private static final Color BACKGROUND_COLOR = new Color(null, 255, 255, 255);
	private static final Color LINE_COLOR = new Color(null, 0, 0, 0);
	private static final Color SELECT_COLOR = new Color(null, 180, 180, 0);
	private static final Color HIGHLIGHT_COLOR = new Color(null, 225, 100, 100);
	private static final Color HIGHLIGHT_FILTER = new Color(null, 20, 20, 20);

	private Canvas timelineCanvas;
	private ScrollBar timelineHBar;
	private ScrollBar timelineVBar;
	private Point timelineOrigin;

	private Image timelineImage;

	private long timestep;
	private int firstSelection;
	private int secondSelection;

	private long currentHighlightTime;

	public IntensityWidget(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new GridLayout());

		this.setLayout(new GridLayout(2, false));
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.BEGINNING;
		gridData.heightHint = CodeSkimmerConstants.WIDGET_HEIGHT + 40;
		this.setLayoutData(gridData);

		firstSelection = -1;
		secondSelection = -1;
		currentHighlightTime = -1;

		createTimelineCanvas();
	}

	private void createTimelineCanvas() {
		drawTimelineImage();
		timelineOrigin = new Point(0, 0);

		timelineCanvas = new Canvas(this, SWT.NO_BACKGROUND
				| SWT.NO_REDRAW_RESIZE | SWT.V_SCROLL | SWT.H_SCROLL);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.heightHint = CodeSkimmerConstants.WIDGET_HEIGHT;
		timelineCanvas.setLayoutData(gridData);

		timelineHBar = timelineCanvas.getHorizontalBar();
		timelineHBar.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				int hSelection = timelineHBar.getSelection();
				int destX = -hSelection - timelineOrigin.x;
				Rectangle rect = timelineImage.getBounds();
				timelineCanvas.scroll(destX, 0, 0, 0, rect.width, rect.height,
						false);
				timelineOrigin.x = -hSelection;
			}
		});
		timelineVBar = timelineCanvas.getVerticalBar();
		timelineVBar.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				int vSelection = timelineVBar.getSelection();
				int destY = -vSelection - timelineOrigin.y;
				Rectangle rect = timelineImage.getBounds();
				timelineCanvas.scroll(0, destY, 0, 0, rect.width, rect.height,
						false);
				timelineOrigin.y = -vSelection;
			}
		});
		timelineCanvas.addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event e) {
				GC gc = e.gc;
				gc.drawImage(timelineImage, timelineOrigin.x, timelineOrigin.y);
				Rectangle rect = timelineImage.getBounds();
				Rectangle client = timelineCanvas.getClientArea();
				int marginWidth = client.width - rect.width;
				if (marginWidth > 0) {
					gc.fillRectangle(rect.width, 0, marginWidth, client.height);
				}
				int marginHeight = client.height - rect.height;
				if (marginHeight > 0) {
					gc.fillRectangle(0, rect.height, client.width, marginHeight);
				}
			}
		});

		resizeTimelineCanvasScroll();
	}

	private void resizeTimelineCanvasScroll() {
		Rectangle rect = timelineImage.getBounds();
		Rectangle client = timelineCanvas.getClientArea();
		timelineHBar.setMaximum(rect.width);
		timelineVBar.setMaximum(rect.height);
		timelineHBar.setThumb(Math.min(rect.width, client.width));
		timelineVBar.setThumb(Math.min(rect.height, client.height));
		timelineHBar.setIncrement(50);
		timelineHBar.setPageIncrement(50);

		updateTimlineCanvasPosition();
	}

	private void updateTimlineCanvasPosition() {
		int hSelection = timelineHBar.getSelection();
		int vSelection = timelineVBar.getSelection();
		timelineOrigin.x = -Math.max(0, hSelection);
		timelineOrigin.y = -Math.max(0, vSelection);
		timelineCanvas.redraw();
	}

	private void drawTimelineImage() {
		if (timelineImage != null) {
			timelineImage.dispose();
		}

		int width = 1, height = 1;
		if (operations != null) {
			width = Math.max(CodeSkimmerConstants.MIN_GRAPH_WIDTH, this
					.getParent().getSize().x - 50);
			height = CodeSkimmerConstants.WIDGET_HEIGHT;
		}

		timelineImage = new Image(getDisplay(), width, height);
		GC gc = new GC(timelineImage);
		gc.setBackground(BACKGROUND_COLOR);
		gc.fillRectangle(0, 0, width, height);

		if (operations != null && operations.size() > 0) {
			timestep = Math.max((operations.get(operations.size() - 1)
					.getTime() - operations.get(0).getTime()) / width, 1); // timestep
																			// in
																			// milliseconds
			int[] numOperationsInTimestepWindow = new int[width];
			int startIndex = 0;
			int endIndex = 0;
			int max = 1;

			// first filter operations
			ArrayList<UserOperation> filtered;
			if (filters != null && filters.size() > 0) {
				filtered = Filter.filterOperations(filters, operations);
			} else {
				filtered = operations;
			}

			// figure out counts
			for (int i = 0; i < width; i++) {
				long curTime = operations.get(0).getTime() + i * timestep;

				// get startIndex
				while (startIndex < filtered.size()
						&& curTime - filtered.get(startIndex).getTime() > (CodeSkimmerConstants.DEFAULT_TIME_WINDOW / 2)) {
					startIndex++;
				}

				// get endIndex
				while (endIndex < filtered.size()
						&& filtered.get(endIndex).getTime() - curTime <= (CodeSkimmerConstants.DEFAULT_TIME_WINDOW / 2)) {
					endIndex++;
				}

				numOperationsInTimestepWindow[i] = endIndex - startIndex;
				if (numOperationsInTimestepWindow[i] > max) {
					max = numOperationsInTimestepWindow[i];
				}
			}

			int graphHeight = height - 30;
			double scale = (double) graphHeight / max;

			for (int i = 0; i < width; i++) {

				// draw selections
				if (i == firstSelection || i == secondSelection) {
					gc.setForeground(SELECT_COLOR);
					gc.drawLine(i, 0, i, height);
				}

				// draw current position
				if (i == ((int) (currentHighlightTime - operations.get(0)
						.getTime()) / timestep)) {
					gc.setForeground(HIGHLIGHT_COLOR);
					gc.drawLine(i, 0, i, height);
				}

				if (shouldHighlightPoint(i)) {
					gc.setForeground(LINE_COLOR);
				} else {
					gc.setForeground(super.combineColors(LINE_COLOR,
							HIGHLIGHT_FILTER));
				}

				int y = graphHeight
						- ((int) (numOperationsInTimestepWindow[i] * scale))
						+ 15;
				gc.drawPoint(i, y);
			}
		}

		gc.dispose();
	}

	private boolean shouldHighlightPoint(int point) {
		if (firstSelection == -1 || secondSelection == -1
				|| (point > firstSelection && point < secondSelection)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void redraw() {
		drawTimelineImage();
		resizeTimelineCanvasScroll();
	}

	@Override
	public void setCurrentOperation(UserOperation op) {
		if (operations != null && operations.size() > 0
				&& timelineImage != null && op != null
				&& operations.indexOf(op) >= 0) {
			currentHighlightTime = op.getTime();
		} else {
			currentHighlightTime = -1;
		}

		redraw();
	}

	@Override
	public void setMouseClickListener(MouseListener listener) {
		// ensure only 1 active mouse listener
		try {
			timelineCanvas.removeMouseListener(listener);
		} catch (Exception e) {

		}
		timelineCanvas.addMouseListener(listener);
	}

	@Override
	public void handleMouseClickEvent(MouseEvent e) {
		if ((firstSelection != -1 && secondSelection != -1)
				|| (e.x < firstSelection)) {
			firstSelection = -1;
		}
		if (firstSelection == -1) {
			secondSelection = -1;
		}

		if (firstSelection == -1) {
			firstSelection = e.x;
		} else {
			secondSelection = e.x;
		}

		redraw();
	}

	public long getFirstSelection() {
		if (firstSelection == -1) {
			return -1;
		}
		return operations.get(0).getTime() + timestep * firstSelection;
	}

	public long getSecondSelection() {
		if (secondSelection == -1) {
			return -1;
		}
		return operations.get(0).getTime() + timestep * secondSelection;
	}

	@Override
	public void clearSelection() {
		firstSelection = -1;
		secondSelection = -1;
		redraw();
	}
}
