package com.htc.smoonos.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.htc.smoonos.R;
import com.htc.smoonos.entry.AppInfoBean;
import com.htc.smoonos.widget.RLRelativelayout;

import java.util.ArrayList;


/**
 * @author 作�?�：hxd
 * @version 创建时间 2020/9/8 下午3:50:51 类说�?
 */
public class AppFavoritesAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<AppInfoBean> mList = new ArrayList<AppInfoBean>();
    private LayoutInflater inflater;

    public AppFavoritesAdapter(Context context, ArrayList<AppInfoBean> list) {
        this.mContext = context;
        this.mList = list;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {

        return mList.size();
    }

    @Override
    public AppInfoBean getItem(int position) {

        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewholder = null;
        if (convertView == null) {
            viewholder = new ViewHolder();
            convertView = inflater.inflate(R.layout.layout_appfavorites_gridview_item, null);
            viewholder.addapp_icon_iv = (ImageView) convertView.findViewById(R.id.addapp_icon_iv);
            viewholder.addapp_name = convertView.findViewById(R.id.addapp_name);
            viewholder.appapp_iv = (ImageView) convertView.findViewById(R.id.appapp_iv);
			viewholder.rlRelativelayout = (RLRelativelayout)convertView.findViewById(R.id.appsfavor);
//			if (fontFace != null) {
//				viewholder.addapp_name.setTypeface(fontFace);
//			}
            convertView.setTag(viewholder);
        } else {
            viewholder = (ViewHolder) convertView.getTag();
        }
        AppInfoBean bean = getItem(position);
        viewholder.addapp_icon_iv.setBackground(bean.getAppicon());
        viewholder.addapp_name.setText(bean.getAppname());
        if (bean.isCheck()) {
            viewholder.appapp_iv.setVisibility(View.VISIBLE);
        } else {
            viewholder.appapp_iv.setVisibility(View.GONE);
        }
        return convertView;
    }


    static class ViewHolder {
        private ImageView addapp_icon_iv;
        private TextView addapp_name;
        private ImageView appapp_iv;
		private RLRelativelayout rlRelativelayout;
    }

}
