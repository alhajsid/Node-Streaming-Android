package xyz.tanwb.airship.view.adapter;

import android.content.Context;
import androidx.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import xyz.tanwb.airship.view.adapter.listener.OnItemChildClickListener;
import xyz.tanwb.airship.view.adapter.listener.OnItemChildLongClickListener;

public abstract class BaseListAdapter<M> extends BaseAdapter {

    protected Context mContext;
    protected int mItemLayoutId;
    protected List<M> mDatas;
    protected OnItemChildClickListener mOnItemChildClickListener;
    protected OnItemChildLongClickListener mOnItemChildLongClickListener;

    public BaseListAdapter(Context context) {
        this(context, 0, null);
    }

    public BaseListAdapter(Context context, int itemLayoutId) {
        this(context, itemLayoutId, null);
    }

    public BaseListAdapter(Context context, int itemLayoutId, List<M> datas) {
        mContext = context;
        mItemLayoutId = itemLayoutId;
        mDatas = datas;
    }

    public void setmItemLayoutId(@LayoutRes int mItemLayoutId) {
        this.mItemLayoutId = mItemLayoutId;
    }

    @Override
    public int getCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    @Override
    public M getItem(int position) {
        return mDatas == null ? null : mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final BaseListHolder viewHolder = BaseListHolder.dequeueReusableAdapterViewHolder(mContext, convertView, parent, mItemLayoutId);
        viewHolder.getViewHolderHelper().setPosition(position);
        viewHolder.getViewHolderHelper().setOnItemChildClickListener(mOnItemChildClickListener);
        viewHolder.getViewHolderHelper().setOnItemChildLongClickListener(mOnItemChildLongClickListener);
        setItemChildListener(viewHolder.getViewHolderHelper());
        setItemData(viewHolder.getViewHolderHelper(), position, getItem(position));
        return viewHolder.getConvertView();
    }

    /**
     * ???item????????????????????????????????????????????????????????????????????????item????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     */
    protected void setItemChildListener(ViewHolderHelper viewHolderHelper) {
    }

    /**
     * ??????item??????
     */
    protected abstract void setItemData(ViewHolderHelper viewHolderHelper, int position, M m);

    /**
     * ??????item????????????????????????????????????
     */
    public void setOnItemChildClickListener(OnItemChildClickListener onItemChildClickListener) {
        mOnItemChildClickListener = onItemChildClickListener;
    }

    /**
     * ??????item????????????????????????????????????
     */
    public void setOnItemChildLongClickListener(OnItemChildLongClickListener onItemChildLongClickListener) {
        mOnItemChildLongClickListener = onItemChildLongClickListener;
    }

    public void setDatas(List<M> datas) {
        mDatas = datas;
        notifyDataSetChanged();
    }

    public void addDatas(List<M> datas) {
        if (mDatas == null) {
            mDatas = datas;
        } else {
            mDatas.addAll(datas);
        }
        notifyDataSetChanged();
    }

    public void addData(int position, M model) {
        if (mDatas == null) {
            mDatas = new ArrayList<>();
        }
        mDatas.add(position, model);
        notifyDataSetChanged();
    }

    public void clearDatas() {
        if (mDatas != null) {
            mDatas.clear();
        }
        mDatas = null;
    }

    public void setItem(M oldModel, M newModel) {
        setItem(mDatas.indexOf(oldModel), newModel);
    }

    public void setItem(int location, M newModel) {
        mDatas.set(location, newModel);
        notifyDataSetChanged();
    }

    public void removeItem(M model) {
        removeItem(mDatas.indexOf(model));
    }

    public void removeItem(int position) {
        mDatas.remove(position);
        notifyDataSetChanged();
    }

    /**
     * ????????????ListView?????????(ScorllView ??????????????????scrollView ???????????????????????????????????????????????????)
     */
    public void changeListHeight(ListView listView) {
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = getListContentHeight(listView);
        listView.setLayoutParams(params);
    }

    /**
     * ??????ListView????????????
     */
    public int getListContentHeight(ListView listView) {
        int totalHeight = 0;
        for (int i = 0; i < getCount(); i++) {
            View listItem = getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        return totalHeight + (listView.getDividerHeight() * (getCount() - 1)) + listView.getPaddingBottom() + listView.getPaddingTop();
    }
}