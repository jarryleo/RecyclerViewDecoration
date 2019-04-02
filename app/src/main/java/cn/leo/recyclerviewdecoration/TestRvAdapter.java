package cn.leo.recyclerviewdecoration;

/**
 * @author : Jarry Leo
 * @date : 2019/4/2 10:07
 */
public class TestRvAdapter extends AsyncRVAdapter<String> {
    @Override
    protected boolean areItemsTheSame(String oldItem, String newItem) {
        return false;
    }

    @Override
    protected boolean areContentsTheSame(String oldItem, String newItem) {
        return false;
    }

    @Override
    protected int getItemLayout(int position) {
        return position % 9 == 0 ? R.layout.item_title : R.layout.item_content;
    }

    @Override
    protected void bindData(ItemHelper helper, String data) {
        int layout = helper.getItemLayout();
        if (layout == R.layout.item_title) {
            helper.setText(R.id.tvTitle, data);

        } else if (layout == R.layout.item_content) {
            helper.setText(R.id.tvTitle, data);
        }
    }
}
