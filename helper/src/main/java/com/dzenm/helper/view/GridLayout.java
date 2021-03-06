package com.dzenm.helper.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.IntDef;

import com.dzenm.helper.R;
import com.dzenm.helper.drawable.DrawableHelper;
import com.dzenm.helper.os.OsHelper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * 网格布局
 */
public class GridLayout extends AbsAdapterLayout {

    private static final String TAG = GridLayout.class.getSimpleName();

    public static final int MODE_FILL = 0;          // 填充模式: 表格填充
    public static final int MODE_GRID = 1;          // 网格模式: 一张图自由大小, 4张图2x2布局, 9张图3x3布局

    @IntDef({MODE_FILL, MODE_GRID})
    @Retention(RetentionPolicy.SOURCE)
    @interface Mode {
    }

    private int mHorizontalSpace = OsHelper.dp2px(4);   // Item 水平之间的间距
    private int mVerticalSpace = OsHelper.dp2px(4);     // Item 垂直之间的间距
    private int mMaxCount = 9;                                // 最大的Item数量
    private int mNumber;                                      // 最后一张图片显示剩余其它图片的数量的数量
    private int mColumn = 3;                                  // 列数
    private float mRatio = 1f;                                // 宽高比
    private boolean isSingleWrap = false;                     // 只有一张图片显示的时候是否设置自由大小
    private boolean isShowMore = false;                       // 显示更多
    private OnItemClickListener mOnItemClickListener;
    private @Mode
    int mMode = MODE_FILL;

    private boolean isEditable = true;
    private boolean isDeletable = true;
    private int mDeleteIcon = R.drawable.ic_delete_picture;

    private List<Object> mData;
    private ImageLoader mLoader;
    private ImageAdapter mAdapter;

    public GridLayout(Context context) {
        this(context, null);
    }

    public GridLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 获取自定义属性
        TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.GridLayout);

        mHorizontalSpace = (int) t.getDimension(R.styleable.GridLayout_horizontalSpace, mHorizontalSpace);
        mVerticalSpace = (int) t.getDimension(R.styleable.GridLayout_verticalSpace, mVerticalSpace);
        mMaxCount = t.getInteger(R.styleable.GridLayout_maxCount, mMaxCount);
        mColumn = t.getInteger(R.styleable.GridLayout_column, mColumn);
        mRatio = t.getFloat(R.styleable.GridLayout_ratio, mRatio);
        mMode = t.getInt(R.styleable.GridLayout_mode, mMode);
        isSingleWrap = t.getBoolean(R.styleable.GridLayout_singleWrap, isSingleWrap);
        isEditable = t.getBoolean(R.styleable.GridLayout_isEditable, isEditable);
        isDeletable = t.getBoolean(R.styleable.GridLayout_isDeletable, isDeletable);
        mDeleteIcon = t.getInt(R.styleable.GridLayout_deleteIcon, mDeleteIcon);

        t.recycle();

        // 设置子View的动画
//        setLayoutTransition(LayoutTransitionHelper.scaleViewAnimator(this));

        if (mData == null) {
            mData = new ArrayList<>();
            mAdapter = new ImageAdapter(mData);
            setAdapter(mAdapter);
        }
    }

    /**
     * @param maxCount 设置最大显示个数
     */
    public void setMaxCount(int maxCount) {
        mMaxCount = maxCount;
        resetLayout();
    }

    public int getMaxCount() {
        return mMaxCount;
    }

    /**
     * @param column 设置列数
     */
    public void setColumn(int column) {
        mColumn = column;
        invalidate();
    }

    public int getColumn() {
        return mColumn;
    }

    /**
     * @param ratio 设置宽高比
     */
    public void setRatio(float ratio) {
        this.mRatio = ratio;
        resetLayout();
    }

    /**
     * @param mMode 设置显示的模式
     */
    public void setMode(@Mode int mMode) {
        this.mMode = mMode;
        invalidate();
    }

    public void setShowMore(boolean showMore) {
        isShowMore = showMore;
        invalidate();
    }

    public void setSingleWrap(boolean singleWrap) {
        isSingleWrap = singleWrap;
        invalidate();
    }

    public void setData(List data) {
        mData.clear();
        mData.addAll(data);
        mData.add(R.drawable.ic_add);
        mAdapter.notifyDataSetChanged();
    }

    public void setImageLoader(ImageLoader imageLoader) {
        mLoader = imageLoader;
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
        invalidate();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setDeletable(boolean deletable) {
        isDeletable = deletable;
        invalidate();
    }

    public void setNumber(int number) {
        mNumber = number;
        mAdapter.notifyItemChanged(mData.size() - 1);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void add(List data) {
        mAdapter.add(data);
    }

    public void add(int position, Object elem) {
        mAdapter.add(position, elem);
    }

    public void remove(Object item) {
        mAdapter.remove(item);
    }

    public void remove(int index) {
        mAdapter.remove(index);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d(TAG, "onMeasure");

        int childCount = Math.min(getChildCount(), mMaxCount);          // 计算子View能显示的最大个数
        Log.d(TAG, "child count: " + childCount);

        if (childCount <= 0) {
            setMeasuredDimension(resolveSizeAndState(0, widthMeasureSpec, 0),
                    resolveSizeAndState(0, heightMeasureSpec, 0));
            return;
        }

        int width = MeasureSpec.getSize(widthMeasureSpec);        // 获取父控件的宽度
        int totalWidth = width - getPaddingLeft() - getPaddingRight();
        int height = 0;
        if (childCount == 1 && mMode == MODE_GRID && isSingleWrap && !isEditable) {
            // 单个View不限制大小, 直接使用子View的大小
            View child = getChildAt(0);
            measureChild(child, heightMeasureSpec, heightMeasureSpec);
            height = child.getMeasuredHeight();
            width = child.getMeasuredWidth();
        } else {
            // 多个View按表格显示, 并计算表格的宽高
            height = calculateMeasureMultiLineHeight(childCount, totalWidth, heightMeasureSpec);
        }

        // 指定自己的宽高
        height += getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, 0),
                resolveSizeAndState(height, heightMeasureSpec, 0));
    }

    /**
     * 计算多行的高度
     *
     * @param childCount        子View的个数
     * @param totalWidth        总宽度
     * @param heightMeasureSpec 父View的测量模式
     * @return 高度
     */
    private int calculateMeasureMultiLineHeight(int childCount, int totalWidth, int heightMeasureSpec) {
        int childWidth = (totalWidth - mHorizontalSpace * (mColumn - 1)) / mColumn;
        Log.d(TAG, "child width: " + childWidth);
        int height = 0;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            int childHeight;
            // 指定子View宽度的测量模式(固定宽度)
            int widthSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY);
            if (mRatio == -1f) {
                measureChild(child, widthSpec, heightMeasureSpec);  // 测量子View的大小
                childHeight = child.getMeasuredHeight();            // 获取测量之后子View的高度
            } else {
                childHeight = (int) (childWidth / mRatio);          // 固定子View的高度
                // 指定子View高度的测量模式(固定高度)
                int heightSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY);
                measureChild(child, widthSpec, heightSpec);         // 测量子View的大小
            }
            Log.d(TAG, "child height: " + childHeight);

            // 计算高度
            if (i == 0) {
                // 第一行不高度需要加 mVerticalSpace
                height = childHeight;
            } else if ((i) % mColumn == 0) {
                // 第二行开始加多一个 mVerticalSpace
                height += childHeight + mVerticalSpace;
            }
        }
        return height;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d(TAG, "onLayout");

        int childCount = Math.min(getChildCount(), mMaxCount);
        if (childCount <= 0) {
            return;
        }

        // 计算一下最大的条目数量
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            // 获取计算的子View的宽高
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            // 对子View进行布局
            child.layout(paddingLeft, paddingTop, paddingLeft + width, paddingTop + height);

            paddingLeft += width + mHorizontalSpace;    // 累加宽度

            // 判断显示的方式
            int column = 0;
            if (mMode == MODE_GRID && childCount == 4) {
                column = 2;
            } else {
                column = 3;
            }

            if ((i + 1) % column == 0) {                // 如果是换行
                paddingLeft = getPaddingLeft();         // 重置左边的位置
                paddingTop += height + mVerticalSpace;  // 累加高度
            }
        }
    }

    @Override
    protected void onLayoutItemInserted(int positionStart, int itemCount) {
        if (getItemCount() <= mMaxCount) {
            super.onLayoutItemInserted(positionStart, itemCount);
        }
    }

    @Override
    protected int getTotalCount() {
        if (isShowMore) {
            return getItemCount();
        } else {
            return Math.min(getItemCount(), mMaxCount);
        }
    }

    public class ImageAdapter extends AbsAdapter {

        private int mLayoutId;
        private boolean isLastView = false;

        public ImageAdapter(List<Object> data) {
            this(data, 0);
        }

        public ImageAdapter(List<Object> data, int layoutId) {
            mData = new ArrayList<>();
            mData.addAll(data);
            mLayoutId = layoutId;
        }

        protected Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        protected int layoutId() {
            return mLayoutId;
        }

        @Override
        public View onCreateView(ViewGroup parent) {
            Context context = parent.getContext();
            if (mLayoutId != 0) {
                return LayoutInflater.from(context).inflate(mLayoutId, parent);
            } else {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                RelativeLayout relativeLayout = new RelativeLayout(context);
                relativeLayout.setLayoutParams(layoutParams);
                RatioImageView imageView = new RatioImageView(context);
                imageView.setLayoutParams(layoutParams);
                imageView.setPivotX(0);
                imageView.setPivotY(0);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                relativeLayout.addView(imageView);

                if (isEditable && isDeletable) {
                    RatioImageView deleteView = new RatioImageView(context);
                    int deleteSize = OsHelper.dp2px(16);
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(deleteSize, deleteSize);
                    params.addRule(RelativeLayout.ALIGN_PARENT_END);
                    deleteView.setLayoutParams(params);
                    relativeLayout.addView(deleteView);
                }
                return relativeLayout;
            }
        }

        @Override
        public void onBindView(View view, final int position) {
            if (mLoader == null) {
                throw new NullPointerException("must be use an image loader");
            }
            final RatioImageView imageView = (RatioImageView) ((ViewGroup) view).getChildAt(0);
            if (position == mData.size() - 1 && position < mMaxCount) {
                if (isEditable && position != mMaxCount - 1) {
                    imageView.setImageResource(R.drawable.ic_add);
                    DrawableHelper.ripple(R.color.lightGrayColor, R.color.hintColor).into(imageView);
                    if (mOnItemClickListener != null) {
                        imageView.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mOnItemClickListener.onLoad(ImageAdapter.this);
                            }
                        });
                    }
                } else {
                    imageView.setVisibility(View.GONE);
                }
            } else {
                mLoader.onLoader(imageView, getItem(position));
                if (getItemCount() == mMaxCount && !isEditable) {
                    imageView.setNumber(mNumber);
                }
                if (mOnItemClickListener != null) {
                    imageView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mOnItemClickListener.onItemClick(imageView, getItem(position), position);
                        }
                    });
                }
                if (isEditable && isDeletable) {
                    RatioImageView deleteView = (RatioImageView) ((ViewGroup) view).getChildAt(1);
                    if (mDeleteIcon != 0) {
                        deleteView.setImageResource(mDeleteIcon);
                    }
                    deleteView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    // 设置点击事件, 单击删除
                    deleteView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            remove(position);
                        }
                    });
                }
            }
        }

        public void add(Object elem) {
            mData.add(mData.size() - 1, elem);
            notifyItemInserted(mData.size() - 2, 1);
        }

        public void add(List data) {
            mData.addAll(data);
            notifyItemInserted(mData.size(), data.size());
        }

        public void add(int position, Object elem) {
            if (position <= mData.size() - 1 && position >= 0) {
                mData.add(position, elem);
                notifyItemInserted(position);
            } else {
                throw new ArrayIndexOutOfBoundsException("position out of array index");
            }
        }

        public void remove(Object elem) {
            mData.remove(elem);
            notifyItemRemoved(mData.size() - 2);
        }

        public void remove(int index) {
            mData.remove(index);
            notifyItemRemoved(index, 1);
        }

    }

    public static class OnItemClickListener {

        public void onItemClick(View view, Object data, int position) {
        }

        public void onLoad(ImageAdapter adapter) {
        }
    }

}