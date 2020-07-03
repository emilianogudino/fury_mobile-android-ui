package com.mercadolibre.android.ui.legacy.widgets.atableview.internal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.text.Html;
import android.view.Display;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mercadolibre.android.ui.legacy.R;
import com.mercadolibre.android.ui.legacy.widgets.atableview.foundation.NSIndexPath;
import com.mercadolibre.android.ui.legacy.widgets.atableview.internal.ATableViewCellAccessoryView.ATableViewCellAccessoryType;
import com.mercadolibre.android.ui.legacy.widgets.atableview.internal.ATableViewCellDrawable.ATableViewCellBackgroundStyle;
import com.mercadolibre.android.ui.legacy.widgets.atableview.internal.ATableViewHeaderFooterCell.ATableViewHeaderFooterCellType;
import com.mercadolibre.android.ui.legacy.widgets.atableview.protocol.ATableViewDataSource;
import com.mercadolibre.android.ui.legacy.widgets.atableview.protocol.ATableViewDataSourceExt;
import com.mercadolibre.android.ui.legacy.widgets.atableview.protocol.ATableViewDelegate;
import com.mercadolibre.android.ui.legacy.widgets.atableview.view.ATableView;
import com.mercadolibre.android.ui.legacy.widgets.atableview.view.ATableView.ATableViewStyle;
import com.mercadolibre.android.ui.legacy.widgets.atableview.view.ATableViewCell;
import com.mercadolibre.android.ui.legacy.widgets.atableview.view.ATableViewCell.ATableViewCellSelectionStyle;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class ATableViewAdapter extends BaseAdapter {
    private List<Boolean> mHasHeader;
    private List<Boolean> mHasFooter;
    private List<Integer> mHeadersHeight;
    private List<Integer> mFootersHeight;
    private List<Integer> mRows;
    private List<List<Integer>> mRowsHeight;

    private ATableView mTableView;

    public ATableViewAdapter(ATableView tableView) {
        mTableView = tableView;
        initialize();
    }

    private void initialize() {
        mHasHeader = new ArrayList<>();
        mHasFooter = new ArrayList<>();
        mHeadersHeight = new ArrayList<>();
        mFootersHeight = new ArrayList<>();
        mRows = new ArrayList<>();
        mRowsHeight = new ArrayList<>();

        int sections;

        ATableViewDataSource dataSource = mTableView.getDataSource();
        ATableViewDelegate delegate = mTableView.getDelegate();

        // datasource & delegate setup.
        if (dataSource != null) {
            sections = dataSource.numberOfSectionsInTableView(mTableView);
            for (int s = 0; s < sections; s++) {
                boolean hasHeader = false, hasFooter = false;

                // mark header if overridden in delegate, otherwise try to pull title instead.
                int headerHeight = delegate.heightForHeaderInSection(mTableView, s);
                if (headerHeight != ATableViewCell.LayoutParams.UNDEFINED ||
                        dataSource.titleForHeaderInSection(mTableView, s) != null) {
                    hasHeader = true;
                }
                mHeadersHeight.add(headerHeight);
                mHasHeader.add(hasHeader);

                // mark footer if overridden in delegate, otherwise try to pull title instead.
                int footerHeight = delegate.heightForFooterInSection(mTableView, s);
                if (footerHeight != ATableViewCell.LayoutParams.UNDEFINED ||
                        dataSource.titleForFooterInSection(mTableView, s) != null) {
                    hasFooter = true;
                }
                mFootersHeight.add(footerHeight);
                mHasFooter.add(hasFooter);

                // pull row count from datasource.
                mRows.add(dataSource.numberOfRowsInSection(mTableView, s));

                List<Integer> sectionRowHeights = new ArrayList<Integer>();

                // pull row heights.
                int rows = mRows.get(s);
                for (int r = 0; r < rows; r++) {
                    NSIndexPath indexPath = NSIndexPath.indexPathForRowInSection(r, s);
                    sectionRowHeights.add(delegate.heightForRowAtIndexPath(mTableView, indexPath));
                }
                mRowsHeight.add(sectionRowHeights);
            }
        }
    }

    @Override
    public void notifyDataSetChanged() {
        initialize();
        super.notifyDataSetChanged();
    }

    public NSIndexPath getIndexPath(int position) {
        // closes #18, set offset in one by default only if we've a header in the very first
        // section of the table.
        int offset = hasHeader(0) ? 1 : 0, count = 0, limit = 0;

        int sections = mRows.size();
        for (int s = 0; s < sections; s++) {
            int rows = mRows.get(s);

            limit += rows + getHeaderFooterCountOffset(s);
            if (position < limit) {
                // offset is given by current pos, accumulated headers, footers and rows.
                int positionWithOffset = position - offset - count;
                return NSIndexPath.indexPathForRowInSection(positionWithOffset, s);
            }
            offset += getHeaderFooterCountOffset(s);
            count += rows;
        }

        return null;
    }

    public boolean hasHeader(int section) {
        if (mTableView.getStyle() == ATableViewStyle.Grouped) {
            return true;
        }

        return mHasHeader.get(section);
    }

    public boolean hasFooter(int section) {
        if (mTableView.getStyle() == ATableViewStyle.Grouped) {
            return true;
        }

        return mHasFooter.get(section);
    }

    public boolean isHeaderRow(int position) {
        int sections = mRows.size();
        for (int s = 0; s < sections; s++) {
            if (hasHeader(s) && position == 0) {
                return true;
            }
            position -= mRows.get(s) + getHeaderFooterCountOffset(s);
        }

        return false;
    }

    public boolean isFooterRow(int position) {
        int positionWithOffset = 0;

        int sections = mRows.size();
        for (int s = 0; s < sections; s++) {
            positionWithOffset += mRows.get(s) + getHeaderFooterCountOffset(s);
            if (hasFooter(s) && position - positionWithOffset == -1) {
                return true;
            }
        }

        return false;
    }

    private boolean isSingleRow(NSIndexPath indexPath) {
        return indexPath.getRow() == 0 && mRows.get(indexPath.getSection()) == 1;
    }

    private boolean isTopRow(NSIndexPath indexPath) {
        return indexPath.getRow() == 0 && mRows.get(indexPath.getSection()) > 1;
    }

    private boolean isBottomRow(NSIndexPath indexPath) {
        return indexPath.getRow() == mRows.get(indexPath.getSection()) - 1;
    }

    private int getMeasuredRowHeight(ATableViewCell cell, NSIndexPath indexPath, boolean cache) {

        // We assume that the table is always used with full screen width
        Display display = ((WindowManager) cell.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int tableWidth = size.x;

        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(tableWidth, MeasureSpec.EXACTLY);
        @SuppressLint("Range") int heightMeasureSpec = MeasureSpec.makeMeasureSpec(AbsListView.LayoutParams.WRAP_CONTENT, getHeightMeasureMode());
        cell.measure(widthMeasureSpec, heightMeasureSpec);

        // add measured height to cache, so we don't have to recalculate every time.
        int height = (int) (cell.getMeasuredHeight() / cell.getResources().getDisplayMetrics().density);
        if (cache) {
            mRowsHeight.get(indexPath.getSection()).set(indexPath.getRow(), height);
        }

        return height;
    }

    private int getHeightMeasureMode() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1 ? MeasureSpec.AT_MOST : MeasureSpec.EXACTLY;
    }

    private int getRowHeight(ATableViewCell cell, NSIndexPath indexPath, ATableViewCellBackgroundStyle backgroundStyle) {
        Resources res = mTableView.getContext().getResources();

        // transform height constants into values if we've set so.
        // closes #7. it seems Android ~2.2 requires known row height to draw cell background drawable.
        int rowHeight = mRowsHeight.get(indexPath.getSection()).get(indexPath.getRow());
        if (rowHeight < 0) {
            // cached for performance, it might have some impact if user changes the text after layout.
            rowHeight = getMeasuredRowHeight(cell, indexPath, true);
        }
        rowHeight = (int) Math.ceil(rowHeight * res.getDisplayMetrics().density);

        // add extra height to rows depending on it's style.
        Rect padding = ATableViewCellDrawable.getContentPadding(mTableView, backgroundStyle);
        rowHeight += padding.top + padding.bottom;

        return rowHeight;
    }

    private int getHeaderFooterRowHeight(NSIndexPath indexPath, boolean isFooterRow) {
        Resources res = mTableView.getResources();
        int section = indexPath.getSection();

        // pull height, it will default on delegate to UNDEFINED if not overridden.
        int rowHeight = ATableViewCell.LayoutParams.UNDEFINED;
        if (isFooterRow) {
            rowHeight = mFootersHeight.get(section);
        } else {
            rowHeight = mHeadersHeight.get(section);
        }

        // if undefined, set a valid height depending on table style, otherwise use overridden value.
        if (rowHeight == ATableViewCell.LayoutParams.UNDEFINED) {
            if (mTableView.getStyle() == ATableViewStyle.Plain) {
                rowHeight = (int) res.getDimension(R.dimen.atv_plain_section_header_height);
            } else {
                rowHeight = ListView.LayoutParams.WRAP_CONTENT;
            }
        }

        // convert row height value when an scalar was used.
        if (rowHeight > -1) {
            rowHeight = (int) Math.ceil(rowHeight * res.getDisplayMetrics().density);
        }

        return rowHeight;
    }

    private ATableViewCellBackgroundStyle getRowBackgroundStyle(NSIndexPath indexPath) {
        ATableViewCellBackgroundStyle backgroundStyle = ATableViewCellBackgroundStyle.Middle;
        if (isSingleRow(indexPath)) {
            backgroundStyle = ATableViewCellBackgroundStyle.Single;
        } else if (isTopRow(indexPath)) {
            backgroundStyle = ATableViewCellBackgroundStyle.Top;
        } else if (isBottomRow(indexPath)) {
            backgroundStyle = ATableViewCellBackgroundStyle.Bottom;
        }

        return backgroundStyle;
    }

    private int getRowBackgroundColor(ATableViewCell cell) {
        Resources res = mTableView.getResources();

        // pull cell color, -1 implies color has not being defined so we'll go with the defaults.
        int color = cell.getBackgroundColor();
        if (color == -1) {
            color = res.getColor(R.color.atv_cell_plain_background);
            if (mTableView.getStyle() == ATableViewStyle.Grouped) {
                color = res.getColor(R.color.atv_cell_grouped_background);
            }
        }

        return color;
    }

    private ATableViewHeaderFooterCell getReusableHeaderFooterCell(View convertView, boolean isFooterRow) {
        ATableViewHeaderFooterCell cell = (ATableViewHeaderFooterCell) convertView;
        if (cell == null) {
            ATableViewHeaderFooterCellType type = ATableViewHeaderFooterCellType.Header;
            if (isFooterRow) {
                type = ATableViewHeaderFooterCellType.Footer;
            }
            cell = new ATableViewHeaderFooterCell(type, mTableView);
        }

        return cell;
    }

    private void setupHeaderFooterRowLayout(ATableViewHeaderFooterCell cell, NSIndexPath indexPath, boolean isFooterRow) {
        ATableViewDataSource dataSource = mTableView.getDataSource();
        int section = indexPath.getSection();
        Resources res = mTableView.getResources();
        TextView textLabel = cell.getTextLabel();

        // get text.
        String headerText = null;
        Typeface typeface;
        if (isFooterRow) {
            headerText = dataSource.titleForFooterInSection(mTableView, section);
            typeface = dataSource.typefaceForFooter();
        } else {
            headerText = dataSource.titleForHeaderInSection(mTableView, section);
            typeface = dataSource.typefaceForHeader();
        }

        if (typeface != null) {
            textLabel.setTypeface(typeface);
        }

        if (headerText == null) {
            textLabel.setText(null);
        } else {
            textLabel.setText(Html.fromHtml(headerText));
        }

        // setup layout and background depending on table style.
        Rect padding = new Rect();
        if (mTableView.getStyle() == ATableViewStyle.Grouped) {
            boolean hasText = headerText != null && headerText.length() > 0;

            padding.left = padding.right = (int) res.getDimension(R.dimen.atv_grouped_section_header_footer_padding_left_right);

            // if we're on the very first header of the table and it has text, we've to add an extra padding
            // on top of the cell.
            padding.top = (int) res.getDimension(R.dimen.atv_grouped_section_header_padding_top);
            if (!isFooterRow && section == 0 && hasText) {
                padding.top = (int) res.getDimension(R.dimen.atv_grouped_section_header_first_row_padding_top);
            }

            // if we're on the last footer of the table, extra padding applies here as well.
            padding.bottom = (int) res.getDimension(R.dimen.atv_grouped_section_footer_padding_bottom);
            if (isFooterRow && section == mRows.size() - 1) {
                padding.bottom = (int) res.getDimension(R.dimen.atv_grouped_section_footer_last_row_padding_bottom);
            }

            // hide header or footer text if it's null.
            int visibility = headerText != null && headerText.length() > 0 ? View.VISIBLE : View.GONE;
            textLabel.setVisibility(visibility);
        } else {
            padding.left = (int) res.getDimension(R.dimen.atv_plain_section_header_padding_left);
            padding.right = (int) res.getDimension(R.dimen.atv_plain_section_header_padding_right);

        }
        cell.setPadding(padding.left, padding.top, padding.right, padding.bottom);

        // setup layout height.
        int rowHeight = getHeaderFooterRowHeight(indexPath, isFooterRow);
        ListView.LayoutParams params = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, rowHeight);
        cell.setLayoutParams(params);
    }

    private void setupRowLayout(ATableViewCell cell, NSIndexPath indexPath, int rowHeight) {

        // add extra padding for grouped style.
        if (mTableView.getStyle() == ATableViewStyle.Grouped) {
            int margin = (int) cell.getResources().getDimension(R.dimen.atv_cell_grouped_margins);
            cell.setPadding(margin, 0, margin, 0);
        }

        ListView.LayoutParams params = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, rowHeight);
        cell.setLayoutParams(params);
    }

    private void setupRowBackgroundDrawable(ATableViewCell cell, NSIndexPath indexPath,
                                            ATableViewCellBackgroundStyle backgroundStyle, int rowHeight) {

        // setup background drawables.
        StateListDrawable drawable = new StateListDrawable();

        ATableViewCellSelectionStyle selectionStyle = cell.getSelectionStyle();
        if (selectionStyle != ATableViewCellSelectionStyle.None) {
            Resources res = mTableView.getContext().getResources();

            int selectionColor = res.getColor(R.color.selection_color);

            ShapeDrawable pressed = new ATableViewCellDrawable(mTableView, backgroundStyle, rowHeight, selectionColor);
            pressed.setAlpha(50);
            drawable.addState(new int[]{android.R.attr.state_pressed}, pressed);
            drawable.addState(new int[]{android.R.attr.state_focused}, pressed);
        }

        int color = getRowBackgroundColor(cell);
        ShapeDrawable normal = new ATableViewCellDrawable(mTableView, backgroundStyle, rowHeight, color);
        drawable.addState(new int[]{}, normal);

        // when extending
        ViewGroup backgroundView = (ViewGroup) cell.getBackgroundView();
        if (backgroundView == null) {
            throw new RuntimeException("Cannot find R.id.backgroundView on your cell custom layout, " +
                    "please add it to remove this error.");
        }
        backgroundView.setBackgroundDrawable(drawable);
    }

    private void setupRowContentView(ATableViewCell cell, NSIndexPath indexPath) {
        ATableViewCellBackgroundStyle backgroundStyle = getRowBackgroundStyle(indexPath);

        // set margins accordingly to content view depending on stroke lines thickness.
        View contentView = cell.getContentView();
        Rect padding = ATableViewCellDrawable.getContentPadding(mTableView, backgroundStyle);
        contentView.setPadding(padding.left, padding.top, padding.right, padding.bottom);
    }

    private void setupRowAccessoryButtonDelegateCallback(ATableViewCell cell, final NSIndexPath indexPath) {
        ATableViewCellAccessoryType accessoryType = cell.getAccessoryType();
        if (accessoryType == ATableViewCellAccessoryType.DisclosureButton) {
            View accessoryView = cell.findViewById(R.id.accessoryView);
            accessoryView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ATableViewDelegate delegate = mTableView.getDelegate();
                    delegate.accessoryButtonTappedForRowWithIndexPath(mTableView, indexPath);
                }
            });
        }
    }

    private int getHeaderFooterStyleCount() {
        int count = 1;

        // closes #10. getViewTypeCount() is only called once by the adapter even when notifyDataSetChanged is invoked,
        // so we've to return header & footer styles even we don't have rows to display.
        if (mTableView.getStyle() == ATableViewStyle.Grouped) {
            count = 2;
        }

        return count;
    }

    private int getHeaderFooterCountOffset(int section) {
        return (hasHeader(section) ? 1 : 0) + (hasFooter(section) ? 1 : 0);
    }

    @Override
    public int getCount() {
        int count = 0;

        // count is given by number of rows in section plus its header & footer.
        for (int s = 0; s < mRows.size(); s++) {
            count += mRows.get(s) + getHeaderFooterCountOffset(s);
        }

        return count;
    }

    @Override
    public int getViewTypeCount() {
        int count = getHeaderFooterStyleCount();

        // count must be 1 always, either we don't have any rows, plus the additional count for header & footers.
        ATableViewDataSource dataSource = mTableView.getDataSource();
        if (dataSource instanceof ATableViewDataSourceExt) {
            // TODO should add custom headers & footers to view count here when supported.
            // getViewTypeCount() is called only once, so no effect if this value is changed on runtime by the user.
            count += ((ATableViewDataSourceExt) dataSource).numberOfRowStyles();
        } else {
            count += 1;
        }

        return count;
    }

    @Override
    public int getItemViewType(int position) {
        int viewType = 0;

        int viewTypeCount = getViewTypeCount();
        if (viewTypeCount > 1) {
            if (isHeaderRow(position) && mTableView.getStyle() == ATableViewStyle.Grouped) {
                viewType = viewTypeCount - 2; // additional style for groped headers.
            } else if (isHeaderRow(position) || isFooterRow(position)) {
                viewType = viewTypeCount - 1; // for plain tables, headers and footers share same style.
            } else {
                ATableViewDataSource dataSource = mTableView.getDataSource();
                if (dataSource instanceof ATableViewDataSourceExt) {
                    NSIndexPath indexPath = getIndexPath(position);
                    viewType = ((ATableViewDataSourceExt) dataSource).styleForRowAtIndexPath(indexPath);
                }
            }
        }

        return viewType;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        boolean isHeaderRow = isHeaderRow(position);
        boolean isFooterRow = isFooterRow(position);

        NSIndexPath indexPath = getIndexPath(position);
        if (isHeaderRow || isFooterRow) {
            ATableViewHeaderFooterCell cell = getReusableHeaderFooterCell(convertView, isFooterRow);

            // setup.
            setupHeaderFooterRowLayout(cell, indexPath, isFooterRow);

            convertView = cell;
        } else {
            ATableViewCell cell = (ATableViewCell) convertView;

            ATableViewDataSource dataSource = mTableView.getDataSource();
            dataSource.setReusableCell(cell);

            cell = dataSource.cellForRowAtIndexPath(mTableView, indexPath);

            ATableViewCellBackgroundStyle backgroundStyle = getRowBackgroundStyle(indexPath);
            int rowHeight = getRowHeight(cell, indexPath, backgroundStyle);

            // setup.
            setupRowLayout(cell, indexPath, rowHeight);
            setupRowBackgroundDrawable(cell, indexPath, backgroundStyle, rowHeight);
            setupRowContentView(cell, indexPath);
            setupRowAccessoryButtonDelegateCallback(cell, indexPath);

            // notify delegate we're about drawing the cell, so it can make changes to layout before drawing.
            ATableViewDelegate delegate = mTableView.getDelegate();
            delegate.willDisplayCellForRowAtIndexPath(mTableView, cell, indexPath);

            convertView = cell;
        }

        return convertView;
    }

    @Override
    public boolean isEnabled(int position) {
        // TODO disable sounds for header and footer rows, should be changed when supporting clicks on those views.
        if (isHeaderRow(position) || isFooterRow(position)) {
            return false;
        }

        return super.isEnabled(position);
    }
}
